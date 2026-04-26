package com.kotva.application.session;

import java.io.Serializable;

/**
 * Snapshot of a board position used by preview data.
 */
public class PreviewPositionSnapshot implements Serializable {
    private final int row;
    private final int col;

    /**
     * Creates a preview position snapshot.
     *
     * @param row board row
     * @param col board column
     */
    public PreviewPositionSnapshot(int row, int col) {
        this.row = row;
        this.col = col;
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
}
