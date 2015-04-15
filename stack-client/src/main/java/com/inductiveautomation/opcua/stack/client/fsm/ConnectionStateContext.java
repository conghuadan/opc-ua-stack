package com.inductiveautomation.opcua.stack.client.fsm;

import java.util.concurrent.atomic.AtomicReference;

import com.inductiveautomation.opcua.stack.client.UaTcpStackClient;
import com.inductiveautomation.opcua.stack.client.fsm.states.ConnectionState;
import com.inductiveautomation.opcua.stack.client.fsm.states.DisconnectedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionStateContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicReference<ConnectionState> state = new AtomicReference<>(new DisconnectedState());

    private final UaTcpStackClient client;

    public ConnectionStateContext(UaTcpStackClient client) {
        this.client = client;
    }

    public synchronized ConnectionState handleEvent(ConnectionStateEvent event) {
        ConnectionState currState = state.get();
        ConnectionState nextState = currState.transition(event, this);

        state.set(nextState);

        logger.debug("S({}) x E({}) = S'({})", currState, event, nextState);

        return nextState;
    }

    public UaTcpStackClient getClient() {
        return client;
    }

    public ConnectionState getState() {
        return state.get();
    }

}
