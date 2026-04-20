package com.kotva.infrastructure.network;

import java.util.Objects;

public record LanCommandResultMessage(RemoteCommandResult result) implements LanInboundMessage {
    public LanCommandResultMessage {
        Objects.requireNonNull(result, "result cannot be null.");
    }
}
