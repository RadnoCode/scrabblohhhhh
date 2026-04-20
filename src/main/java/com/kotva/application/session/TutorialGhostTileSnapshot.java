package com.kotva.application.session;

import java.util.Objects;

import java.io.Serializable;

public final class TutorialGhostTileSnapshot implements Serializable {
    private final int row;
    private final int col;
    private final String letter;
    private final int score;

    public TutorialGhostTileSnapshot(int row, int col, String letter, int score) {
        this.row = row;
        this.col = col;
        this.letter = Objects.requireNonNull(letter, "letter cannot be null.");
        this.score = score;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getLetter() {
        return letter;
    }

    public int getScore() {
        return score;
    }
}
