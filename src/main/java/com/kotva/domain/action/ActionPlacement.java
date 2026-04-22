package com.kotva.domain.action;

import com.kotva.domain.model.Position;
import java.io.Serializable;
import java.util.Objects;

public record ActionPlacement(String tileId, Position position, Character assignedLetter)
        implements Serializable {
    private static final long serialVersionUID = 1L;

    public ActionPlacement(String tileId, Position position) {
        this(tileId, position, null);
    }

    public ActionPlacement {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        assignedLetter = assignedLetter == null ? null : Character.toUpperCase(assignedLetter);
    }
}
