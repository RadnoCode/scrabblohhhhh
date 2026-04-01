package com.kotva.domain.model;

/**
 * Represents a single letter tile in the game.
 */
public class Tile {
    private final String tileID;
    private final char letter;
    private final int score;
    private final boolean blank;

    private Character assignedLetter;

    public Tile(String tileID, char letter, int score, boolean blank) {
        this.tileID = tileID;
        this.letter = letter;
        this.score = score;
        this.blank = blank;
    }

    public String getTileID() {
        return tileID;
    }
    public char getLetter() {
        return letter;
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

    /**
     * Assigns a specific letter to a blank tile.
     *
     * @param assignedLetter The letter chosen by the player for this blank tile.
     * @throws IllegalStateException If the tile is not a blank tile, or if it has already been assigned.
     */
    public void setAssignedLetter(Character assignedLetter) {
        if (!this.blank) {
            throw new IllegalStateException("Cannot assign a letter to a non-blank tile.");
        }

        if (this.assignedLetter != null) {
            throw new IllegalStateException("This blank tile has already been assigned a letter.");
        }

        this.assignedLetter = assignedLetter;
    }
}