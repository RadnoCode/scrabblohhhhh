package com.kotva.application.session;

import java.io.Serializable;

/**
 * Snapshot of one tile shown in a player's rack.
 */
public class RackTileSnapshot implements Serializable {
    private final int slotIndex;
    private final String tileId;
    private final Character letter;
    private final Character displayLetter;
    private final int score;
    private final boolean blank;
    private final Character assignedLetter;

    /**
     * Creates a rack tile snapshot.
     *
     * @param slotIndex rack slot index
     * @param tileId tile id
     * @param letter real tile letter
     * @param displayLetter letter displayed to the player
     * @param score tile score
     * @param blank whether this tile is blank
     * @param assignedLetter assigned blank letter, or {@code null}
     */
    public RackTileSnapshot(
        int slotIndex,
        String tileId,
        Character letter,
        Character displayLetter,
        int score,
        boolean blank,
        Character assignedLetter) {
        this.slotIndex = slotIndex;
        this.tileId = tileId;
        this.letter = letter;
        this.displayLetter = displayLetter;
        this.score = score;
        this.blank = blank;
        this.assignedLetter = assignedLetter;
    }

    /**
     * Gets the rack slot index.
     *
     * @return slot index
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * Gets the tile id.
     *
     * @return tile id
     */
    public String getTileId() {
        return tileId;
    }

    /**
     * Gets the real tile letter.
     *
     * @return tile letter
     */
    public Character getLetter() {
        return letter;
    }

    /**
     * Gets the letter displayed in the UI.
     *
     * @return display letter
     */
    public Character getDisplayLetter() {
        return displayLetter;
    }

    /**
     * Gets the tile score.
     *
     * @return tile score
     */
    public int getScore() {
        return score;
    }

    /**
     * Checks whether this tile is blank.
     *
     * @return {@code true} if blank
     */
    public boolean isBlank() {
        return blank;
    }

    /**
     * Gets the assigned letter for a blank tile.
     *
     * @return assigned letter, or {@code null}
     */
    public Character getAssignedLetter() {
        return assignedLetter;
    }
}
