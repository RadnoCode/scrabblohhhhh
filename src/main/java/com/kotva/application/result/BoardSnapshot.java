package com.kotva.application.result;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of all board cells.
 */
public class BoardSnapshot implements Serializable {
    private final List<BoardCellSnapshot> cells;

    /**
     * Creates a board snapshot.
     *
     * @param cells cell snapshots in board order
     */
    public BoardSnapshot(List<BoardCellSnapshot> cells) {
        this.cells = List.copyOf(Objects.requireNonNull(cells, "cells cannot be null."));
    }

    /**
     * Gets all cell snapshots.
     *
     * @return immutable list of cells
     */
    public List<BoardCellSnapshot> getCells() {
        return cells;
    }
}
