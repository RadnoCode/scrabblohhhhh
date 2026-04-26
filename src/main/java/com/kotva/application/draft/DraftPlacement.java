package com.kotva.application.draft;

import com.kotva.domain.model.Position;
import java.io.Serializable;

/**
 * Describes one tile placement staged in a {@link TurnDraft}.
 *
 * <p>The placement stores the tile identity, the target board position, and an
 * optional letter assignment for blank tiles. Assigned letters are normalized to
 * uppercase so that preview and submit logic receive a consistent value.</p>
 */
public class DraftPlacement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tileId;
    private Position position;
    private Character assignedLetter;

    /**
     * Creates a draft placement for a normal tile.
     *
     * @param tileId id of the tile being placed
     * @param position target board position
     */
    public DraftPlacement(String tileId, Position position) {
        this(tileId, position, null);
    }

    /**
     * Creates a draft placement with an optional blank-tile letter assignment.
     *
     * @param tileId id of the tile being placed
     * @param position target board position
     * @param assignedLetter selected letter for a blank tile, or {@code null}
     */
    public DraftPlacement(String tileId, Position position, Character assignedLetter) {
        this.tileId = tileId;
        this.position = position;
        this.assignedLetter = assignedLetter == null ? null : Character.toUpperCase(assignedLetter);
    }

    /**
     * Returns the id of the staged tile.
     *
     * @return tile id
     */
    public String getTileId() {
        return tileId;
    }

    /**
     * Returns the target board position for this placement.
     *
     * @return target board position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the letter assigned to a blank tile.
     *
     * @return assigned uppercase letter, or {@code null} for a normal tile or
     *         an unassigned blank tile
     */
    public Character getAssignedLetter() {
        return assignedLetter;
    }

    /**
     * Updates the id of the staged tile.
     *
     * @param tileId tile id
     */
    public void setTileId(String tileId) {
        this.tileId = tileId;
    }

    /**
     * Updates the target board position.
     *
     * @param position target board position
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Updates the letter assigned to a blank tile.
     *
     * @param assignedLetter selected letter, or {@code null} to clear it
     */
    public void setAssignedLetter(Character assignedLetter) {
        this.assignedLetter = assignedLetter == null ? null : Character.toUpperCase(assignedLetter);
    }
}
