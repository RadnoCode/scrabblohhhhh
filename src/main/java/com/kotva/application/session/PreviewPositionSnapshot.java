package com.kotva.application.session;

import java.io.Serializable;

public class PreviewPositionSnapshot implements Serializable {
    private final int row;
    private final int col;

    public PreviewPositionSnapshot(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
