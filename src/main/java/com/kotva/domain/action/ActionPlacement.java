package com.kotva.domain.action;

import com.kotva.domain.model.Position;
import java.util.Objects;

public record ActionPlacement(String tileId, Position position, Character assignedLetter) {
    public ActionPlacement(String tileId, Position position) {
        this(tileId, position, null);
    }

    public ActionPlacement {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        assignedLetter = assignedLetter == null ? null : Character.toUpperCase(assignedLetter);
    }
}
