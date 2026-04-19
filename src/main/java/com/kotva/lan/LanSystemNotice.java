package com.kotva.lan;

import java.util.Objects;

public record LanSystemNotice(
        String summary,
        String details,
        boolean interactionLocked) {
    public LanSystemNotice {
        summary = Objects.requireNonNullElse(summary, "");
        details = Objects.requireNonNullElse(details, "");
    }
}
