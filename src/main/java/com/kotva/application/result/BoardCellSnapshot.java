package com.kotva.application.result;

import com.kotva.policy.BonusType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Snapshot of one board cell used in the final settlement view.
 */
public class BoardCellSnapshot implements Serializable {
    private final int row;
    private final int col;
    private final BonusType bonusType;
    private final Character letter;
    private final boolean blank;

    /**
     * Creates a cell snapshot.
     *
     * @param row row index on the board
     * @param col column index on the board
     * @param bonusType bonus type of this cell
     * @param letter placed letter, or {@code null} if the cell is empty
     * @param blank whether the placed tile is a blank tile
     */
    public BoardCellSnapshot(int row, int col, BonusType bonusType, Character letter, boolean blank) {
        this.row = row;
        this.col = col;
        this.bonusType = Objects.requireNonNull(bonusType, "bonusType cannot be null.");
        this.letter = letter;
        this.blank = blank;
    }

    /**
     * Gets the row index.
     *
     * @return row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets the column index.
     *
     * @return column index
     */
    public int getCol() {
        return col;
    }

    /**
     * Gets the cell bonus type.
     *
     * @return bonus type
     */
    public BonusType getBonusType() {
        return bonusType;
    }

    /**
     * Gets the placed letter.
     *
     * @return placed letter, or {@code null} when empty
     */
    public Character getLetter() {
        return letter;
    }

    /**
     * Checks whether the placed tile is blank.
     *
     * @return {@code true} if the tile is blank
     */
    public boolean isBlank() {
        return blank;
    }
}
