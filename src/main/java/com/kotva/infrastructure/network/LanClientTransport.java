package com.kotva.infrastructure.network;

import java.util.List;

public interface LanClientTransport {
    void sendCommand(CommandEnvelope commandEnvelope);

    List<LanInboundMessage> drainInboundMessages();

    default void close() {
    }
}
