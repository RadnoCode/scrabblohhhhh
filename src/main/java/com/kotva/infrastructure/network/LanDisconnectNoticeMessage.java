package com.kotva.infrastructure.network;

import java.util.Objects;

public record LanDisconnectNoticeMessage(
        String summary,
        String details) implements LanInboundMessage {
    public LanDisconnectNoticeMessage {
        summary = Objects.requireNonNullElse(summary, "");
        details = Objects.requireNonNullElse(details, "");
    }
}
