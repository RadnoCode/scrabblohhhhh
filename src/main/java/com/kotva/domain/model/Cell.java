
package com.kotva.domain.model;

import com.kotva.policy.BonusType;

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

    public boolean isEmpty() {
        return placedTile == null;
    }

    public void setPlacedTile(Tile tile) {
        if (!this.isEmpty()) {
            throw new IllegalStateException("This cell is already occupied");
        }
        this.placedTile = tile;
    }
}