package com.kotva.domain.action;

import com.kotva.domain.model.Position;
import java.io.Serializable;
import java.util.Objects;

/**
 * Stores one tile placement from a player action.
 *
 * @param tileId id of the tile to place
 * @param position board position for the tile
 * @param assignedLetter letter chosen for a blank tile
 */
public record ActionPlacement(String tileId, Position position, Character assignedLetter)
        implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a placement without a blank-tile letter.
     *
     * @param tileId id of the tile to place
     * @param position board position for the tile
     */
    public ActionPlacement(String tileId, Position position) {
        this(tileId, position, null);
    }

    /**
     * Validates and normalizes the placement.
     */
    public ActionPlacement {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        assignedLetter = assignedLetter == null ? null : Character.toUpperCase(assignedLetter);
    }
}
