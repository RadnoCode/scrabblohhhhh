package com.kotva.application.session;

import java.io.Serializable;
import java.util.Objects;

public class DraftPlacementSnapshot implements Serializable {
    private final String tileId;
    private final int row;
    private final int col;

    public DraftPlacementSnapshot(String tileId, int row, int col) {
        this.tileId = Objects.requireNonNull(tileId, "tileId cannot be null.");
        this.row = row;
        this.col = col;
    }

    public String getTileId() {
        return tileId;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
