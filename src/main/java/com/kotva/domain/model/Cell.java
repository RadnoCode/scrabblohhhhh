
package com.kotva.domain.model;

import com.kotva.policy.BonusType;

/**
 * A single square on the Scrabble board.
 * * This class holds the square's location (Position) and its special
 * scoring rule (BonusType, like Double Word). It also keeps track of
 * which letter tile (if any) is currently sitting on it.
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