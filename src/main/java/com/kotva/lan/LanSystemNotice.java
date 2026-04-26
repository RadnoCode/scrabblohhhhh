package com.kotva.lan;

import java.util.Objects;

/**
 * Notice shown to the LAN UI for connection or system state changes.
 *
 * @param summary short message
 * @param details detailed message
 * @param interactionLocked whether the UI should block interaction
 */
public record LanSystemNotice(
        String summary,
        String details,
        boolean interactionLocked) {
    /**
     * Normalizes null text to empty strings.
     */
    public LanSystemNotice {
        summary = Objects.requireNonNullElse(summary, "");
        details = Objects.requireNonNullElse(details, "");
    }
}
