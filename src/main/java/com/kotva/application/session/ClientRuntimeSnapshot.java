package com.kotva.application.session;

import java.io.Serializable;
import java.util.Objects;

/**
 * Snapshot of LAN client runtime status shown by the UI.
 *
 * @param interactionLocked whether user input should be blocked
 * @param pendingCommandId id of a command waiting for host result
 * @param summary short status message
 * @param details detailed status message
 */
public record ClientRuntimeSnapshot(
        boolean interactionLocked,
        String pendingCommandId,
        String summary,
        String details) implements Serializable {
    /**
     * Validates required message fields.
     */
    public ClientRuntimeSnapshot {
        summary = Objects.requireNonNull(summary, "summary cannot be null.");
        details = Objects.requireNonNull(details, "details cannot be null.");
    }
}
