package com.kotva.application.result;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class BoardSnapshot implements Serializable {
    private final List<BoardCellSnapshot> cells;

    public BoardSnapshot(List<BoardCellSnapshot> cells) {
        this.cells = List.copyOf(Objects.requireNonNull(cells, "cells cannot be null."));
    }

    public List<BoardCellSnapshot> getCells() {
        return cells;
    }
}
