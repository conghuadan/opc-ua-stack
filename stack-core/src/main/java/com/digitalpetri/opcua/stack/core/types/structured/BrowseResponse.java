package com.digitalpetri.opcua.stack.core.types.structured;

import com.digitalpetri.opcua.stack.core.serialization.DelegateRegistry;
import com.digitalpetri.opcua.stack.core.serialization.UaDecoder;
import com.digitalpetri.opcua.stack.core.serialization.UaEncoder;
import com.digitalpetri.opcua.stack.core.serialization.UaResponseMessage;
import com.digitalpetri.opcua.stack.core.types.UaDataType;
import com.digitalpetri.opcua.stack.core.types.builtin.DiagnosticInfo;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.Identifiers;

@UaDataType("BrowseResponse")
public class BrowseResponse implements UaResponseMessage {

    public static final NodeId TypeId = Identifiers.BrowseResponse;
    public static final NodeId BinaryEncodingId = Identifiers.BrowseResponse_Encoding_DefaultBinary;
    public static final NodeId XmlEncodingId = Identifiers.BrowseResponse_Encoding_DefaultXml;

    protected final ResponseHeader _responseHeader;
    protected final BrowseResult[] _results;
    protected final DiagnosticInfo[] _diagnosticInfos;

    public BrowseResponse() {
        this._responseHeader = null;
        this._results = null;
        this._diagnosticInfos = null;
    }

    public BrowseResponse(ResponseHeader _responseHeader, BrowseResult[] _results, DiagnosticInfo[] _diagnosticInfos) {
        this._responseHeader = _responseHeader;
        this._results = _results;
        this._diagnosticInfos = _diagnosticInfos;
    }

    public ResponseHeader getResponseHeader() {
        return _responseHeader;
    }

    public BrowseResult[] getResults() {
        return _results;
    }

    public DiagnosticInfo[] getDiagnosticInfos() {
        return _diagnosticInfos;
    }

    @Override
    public NodeId getTypeId() {
        return TypeId;
    }

    @Override
    public NodeId getBinaryEncodingId() {
        return BinaryEncodingId;
    }

    @Override
    public NodeId getXmlEncodingId() {
        return XmlEncodingId;
    }


    public static void encode(BrowseResponse browseResponse, UaEncoder encoder) {
        encoder.encodeSerializable("ResponseHeader", browseResponse._responseHeader != null ? browseResponse._responseHeader : new ResponseHeader());
        encoder.encodeArray("Results", browseResponse._results, encoder::encodeSerializable);
        encoder.encodeArray("DiagnosticInfos", browseResponse._diagnosticInfos, encoder::encodeDiagnosticInfo);
    }

    public static BrowseResponse decode(UaDecoder decoder) {
        ResponseHeader _responseHeader = decoder.decodeSerializable("ResponseHeader", ResponseHeader.class);
        BrowseResult[] _results = decoder.decodeArray("Results", decoder::decodeSerializable, BrowseResult.class);
        DiagnosticInfo[] _diagnosticInfos = decoder.decodeArray("DiagnosticInfos", decoder::decodeDiagnosticInfo, DiagnosticInfo.class);

        return new BrowseResponse(_responseHeader, _results, _diagnosticInfos);
    }

    static {
        DelegateRegistry.registerEncoder(BrowseResponse::encode, BrowseResponse.class, BinaryEncodingId, XmlEncodingId);
        DelegateRegistry.registerDecoder(BrowseResponse::decode, BrowseResponse.class, BinaryEncodingId, XmlEncodingId);
    }

}
