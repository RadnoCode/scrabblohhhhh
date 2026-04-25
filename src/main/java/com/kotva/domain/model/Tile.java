package com.kotva.domain.model;

import java.io.Serializable;

public class Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String tileID;
    private char letter;
    private final int score;
    private final boolean blank;
    private boolean fixed;

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

    public boolean isFixed() {
        return fixed;
    }

    public Character getAssignedLetter() {
        return assignedLetter;
    }

    public void clearAssignedLetter() {
        this.assignedLetter = null;
    }

    public void markFixed() {
        this.fixed = true;
    }

    public void setAssignedLetter(Character assignedLetter) {
        if (!this.blank) {
            throw new IllegalStateException("Cannot assign a letter to a non-blank tile.");
        }

        if (this.fixed) {
            throw new IllegalStateException("This blank tile has already been assigned a letter.");
        }

        this.assignedLetter = assignedLetter;
    }
}
