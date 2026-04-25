package com.kotva.application.session;

import java.io.Serializable;
import java.util.Objects;

/**
 * Snapshot of one tile currently placed in a turn draft.
 */
public class DraftPlacementSnapshot implements Serializable {
    private final String tileId;
    private final int row;
    private final int col;

    /**
     * Creates a draft placement snapshot.
     *
     * @param tileId id of the tile
     * @param row board row
     * @param col board column
     */
    public DraftPlacementSnapshot(String tileId, int row, int col) {
        this.tileId = Objects.requireNonNull(tileId, "tileId cannot be null.");
        this.row = row;
        this.col = col;
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
