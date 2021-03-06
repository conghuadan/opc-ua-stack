package com.digitalpetri.opcua.stack.client.handlers;

import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.channel.ChannelSecurity;
import com.digitalpetri.opcua.stack.core.channel.ClientSecureChannel;
import com.digitalpetri.opcua.stack.core.channel.SerializationQueue;
import com.digitalpetri.opcua.stack.core.channel.headers.HeaderDecoder;
import com.digitalpetri.opcua.stack.core.channel.headers.SymmetricSecurityHeader;
import com.digitalpetri.opcua.stack.core.channel.messages.ErrorMessage;
import com.digitalpetri.opcua.stack.core.channel.messages.MessageType;
import com.digitalpetri.opcua.stack.core.channel.messages.TcpMessageDecoder;
import com.digitalpetri.opcua.stack.core.serialization.UaMessage;
import com.digitalpetri.opcua.stack.core.serialization.UaRequestMessage;
import com.digitalpetri.opcua.stack.core.serialization.UaResponseMessage;
import com.digitalpetri.opcua.stack.core.util.BufferUtil;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UaTcpClientSymmetricHandler extends ByteToMessageCodec<UaRequestMessage> implements HeaderDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<ByteBuf> chunkBuffers;

    private final ClientSecureChannel secureChannel;

    private final int maxChunkCount;
    private final int maxChunkSize;

    private final UaTcpStackClient client;
    private final SerializationQueue serializationQueue;
    private final CompletableFuture<Channel> handshakeFuture;

    public UaTcpClientSymmetricHandler(UaTcpStackClient client,
                                       SerializationQueue serializationQueue,
                                       CompletableFuture<Channel> handshakeFuture) {
        this.client = client;
        this.serializationQueue = serializationQueue;
        this.handshakeFuture = handshakeFuture;

        secureChannel = client.getSecureChannel();

        maxChunkCount = serializationQueue.getParameters().getLocalMaxChunkCount();
        maxChunkSize = serializationQueue.getParameters().getLocalReceiveBufferSize();

        chunkBuffers = Lists.newArrayListWithCapacity(maxChunkCount);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        List<UaMessage> awaitingHandshake =
                ctx.channel().attr(UaTcpClientAcknowledgeHandler.KEY_AWAITING_HANDSHAKE).get();

        if (awaitingHandshake != null) {
            logger.debug("{} message(s) queued before handshake completed; sending now.", awaitingHandshake.size());
            awaitingHandshake.forEach(m -> ctx.pipeline().write(m));
            ctx.flush();

            ctx.channel().attr(UaTcpClientAcknowledgeHandler.KEY_AWAITING_HANDSHAKE).remove();
        }

        client.getExecutorService().execute(() -> handshakeFuture.complete(ctx.channel()));
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, UaRequestMessage message, ByteBuf out) throws Exception {
        serializationQueue.encode((binaryEncoder, chunkEncoder) -> {
            ByteBuf messageBuffer = BufferUtil.buffer();

            try {
                binaryEncoder.setBuffer(messageBuffer);
                binaryEncoder.encodeMessage(null, message);

                List<ByteBuf> chunks = chunkEncoder.encodeSymmetric(
                        secureChannel,
                        MessageType.SecureMessage,
                        messageBuffer
                );

                ctx.executor().execute(() -> {
                    chunks.forEach(c -> ctx.write(c, ctx.voidPromise()));
                    ctx.flush();
                });
            } catch (UaException e) {
                logger.error("Error encoding {}: {}", message.getClass(), e.getMessage(), e);
                ctx.close();
            } finally {
                messageBuffer.release();
            }
        });
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.readableBytes() >= HeaderLength &&
                buffer.readableBytes() >= getMessageLength(buffer)) {

            int messageLength = getMessageLength(buffer);
            MessageType messageType = MessageType.fromMediumInt(buffer.getMedium(buffer.readerIndex()));

            switch (messageType) {
                case SecureMessage:
                    onSecureMessage(ctx, buffer.readSlice(messageLength), out);
                    break;

                case Error:
                    onError(ctx, buffer.readSlice(messageLength));
                    break;

                default:
                    out.add(buffer.readSlice(messageLength).retain());
            }
        }
    }

    private void onSecureMessage(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws UaException {
        buffer.skipBytes(3); // Skip messageType

        char chunkType = (char) buffer.readByte();

        if (chunkType == 'A') {
            chunkBuffers.forEach(ByteBuf::release);
            chunkBuffers.clear();
        } else {
            buffer.skipBytes(4);

            long secureChannelId = buffer.readUnsignedInt();
            if (secureChannelId != secureChannel.getChannelId()) {
                throw new UaException(StatusCodes.Bad_SecureChannelIdInvalid,
                        "invalid secure channel id: " + secureChannelId);
            }

            SymmetricSecurityHeader securityHeader = SymmetricSecurityHeader.decode(buffer);

            ChannelSecurity channelSecurity = secureChannel.getChannelSecurity();
            long currentTokenId = channelSecurity.getCurrentToken().getTokenId().longValue();

            if (securityHeader.getTokenId() != currentTokenId) {
                long previousTokenId = channelSecurity.getPreviousToken()
                        .map(t -> t.getTokenId().longValue())
                        .orElse(-1L);

                if (securityHeader.getTokenId() != previousTokenId) {
                    throw new UaException(StatusCodes.Bad_SecureChannelTokenUnknown,
                            "unknown secure channel token: " + securityHeader.getTokenId());
                }
            }

            int chunkSize = buffer.readerIndex(0).readableBytes();

            if (chunkSize > maxChunkSize) {
                throw new UaException(StatusCodes.Bad_TcpMessageTooLarge,
                        String.format("max chunk size exceeded (%s)", maxChunkSize));
            }

            chunkBuffers.add(buffer.retain());

            if (chunkBuffers.size() > maxChunkCount) {
                throw new UaException(StatusCodes.Bad_TcpMessageTooLarge,
                        String.format("max chunk count exceeded (%s)", maxChunkCount));
            }

            if (chunkType == 'F') {
                final List<ByteBuf> buffersToDecode = chunkBuffers;
                chunkBuffers = Lists.newArrayListWithCapacity(maxChunkCount);

                serializationQueue.decode((binaryDecoder, chunkDecoder) -> {
                    try {
                        ByteBuf messageBuffer = chunkDecoder.decodeSymmetric(
                                secureChannel,
                                MessageType.SecureMessage,
                                buffersToDecode
                        );

                        binaryDecoder.setBuffer(messageBuffer);
                        UaResponseMessage response = binaryDecoder.decodeMessage(null);

                        client.getExecutorService().execute(
                                () -> client.receiveResponse(response));

                        messageBuffer.release();
                        buffersToDecode.clear();
                    } catch (Throwable t) {
                        logger.error("Error decoding symmetric message: {}", t.getMessage(), t);
                        ctx.close();
                        serializationQueue.pause();
                    }
                });
            }
        }
    }

    private void onError(ChannelHandlerContext ctx, ByteBuf buffer) {
        try {
            ErrorMessage error = TcpMessageDecoder.decodeError(buffer);

            logger.error("Received error message: " + error);
        } catch (UaException e) {
            logger.error("An exception occurred while decoding an error message: {}", e.getMessage(), e);
        } finally {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture.completeExceptionally(
                new UaException(StatusCodes.Bad_ConnectionClosed, "connection closed"));

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught: {}", cause.getMessage(), cause);
        ctx.close();
    }

}
