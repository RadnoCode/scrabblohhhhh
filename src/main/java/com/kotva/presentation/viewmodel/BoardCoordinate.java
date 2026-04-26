package com.kotva.presentation.viewmodel;

import com.kotva.domain.model.Position;

/**
 * Stores a board row and column.
 *
 * @param row board row
 * @param col board column
 */
public record BoardCoordinate(int row, int col) {

    public BoardCoordinate {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("row and col must be non-negative.");
        }
    }

    /**
     * Converts this coordinate to a position.
     *
     * @return the position
     */
    public Position toPosition() {
        return new Position(row, col);
    }
}
