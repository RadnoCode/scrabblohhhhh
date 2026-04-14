package com.kotva.application.session;

import java.util.Objects;

public record ClientRuntimeSnapshot(
        boolean interactionLocked,
        String pendingCommandId,
        String summary,
        String details) {
    public ClientRuntimeSnapshot {
        summary = Objects.requireNonNull(summary, "summary cannot be null.");
        details = Objects.requireNonNull(details, "details cannot be null.");
    }
}
