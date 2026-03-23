
package com.kotva.domain.model;

import com.kotva.policy.BonusType;

/**
 * Represents a single square on the Scrabble board.
 */
public class Cell {

    private final Position position;

    private final BonusType bonusType;

    private Tile placedTile;


    public Cell(Position position, BonusType bonusType) {
        this.position = position;
        this.bonusType = bonusType;
    }


    public Position getPosition() {
        return position;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public Tile getPlacedTile() {
        return placedTile;
    }


    /**
     * Checks if the cell is empty (has no tile placed on it).
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return placedTile == null;
    }

    /**
     * Places a tile on this cell.
     *
     * @param tile The tile to be placed.
     * @throws IllegalStateException If the cell already has a tile.
     */
    public void setPlacedTile(Tile tile) {
        if (!this.isEmpty()) {
            throw new IllegalStateException("This cell is already occupied");
        }
        this.placedTile = tile;
    }
}