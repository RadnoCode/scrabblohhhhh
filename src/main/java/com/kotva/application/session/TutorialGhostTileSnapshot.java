package com.kotva.application.session;

import java.util.Objects;

import java.io.Serializable;

/**
 * Snapshot of a tutorial tile drawn on the board as guidance.
 */
public final class TutorialGhostTileSnapshot implements Serializable {
    private final int row;
    private final int col;
    private final String letter;
    private final int score;

    /**
     * Creates a tutorial ghost tile snapshot.
     *
     * @param row board row
     * @param col board column
     * @param letter letter shown on the ghost tile
     * @param score score shown on the ghost tile
     */
    public TutorialGhostTileSnapshot(int row, int col, String letter, int score) {
        this.row = row;
        this.col = col;
        this.letter = Objects.requireNonNull(letter, "letter cannot be null.");
        this.score = score;
    }

    /**
     * Gets the board row.
     *
     * @return row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets the board column.
     *
     * @return column index
     */
    public int getCol() {
        return col;
    }

    /**
     * Gets the displayed letter.
     *
     * @return tile letter
     */
    public String getLetter() {
        return letter;
    }

    /**
     * Gets the displayed score.
     *
     * @return tile score
     */
    public int getScore() {
        return score;
    }
}
