package com.kotva.application.session;

public class RackTileSnapshot {
    private final int slotIndex;
    private final String tileId;
    private final Character letter;
    private final Character displayLetter;
    private final int score;
    private final boolean blank;
    private final Character assignedLetter;

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

    public int getSlotIndex() {
        return slotIndex;
    }

    public String getTileId() {
        return tileId;
    }

    public Character getLetter() {
        return letter;
    }

    public Character getDisplayLetter() {
        return displayLetter;
    }

    public int getScore() {
        return score;
    }

    public boolean isBlank() {
        return blank;
    }

    public Character getAssignedLetter() {
        return assignedLetter;
    }
}