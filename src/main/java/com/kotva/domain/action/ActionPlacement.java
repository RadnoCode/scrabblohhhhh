package com.kotva.domain.action;

import com.kotva.domain.model.Position;
import java.util.Objects;

public record ActionPlacement(String tileId, Position position) {

    public ActionPlacement {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
    }
}