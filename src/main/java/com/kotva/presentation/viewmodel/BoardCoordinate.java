package com.kotva.presentation.viewmodel;

import com.kotva.domain.model.Position;

/**
 * BoardCoordinate is the UI-facing immutable coordinate used by board renderers
 * and drag logic.
 */
public record BoardCoordinate(int row, int col) {
    public BoardCoordinate {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("row and col must be non-negative.");
        }
    }

    public Position toPosition() {
        return new Position(row, col);
    }
}
