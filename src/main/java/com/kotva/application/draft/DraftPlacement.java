package com.kotva.application.draft;

import com.kotva.domain.model.Position;

/**
 * DraftPlacement represents a single tile placement proposed by the player during the drafting phase of their turn. 
 * It contains the identifier of the tile being placed and the position on the board where the player intends to place it. 
 * This class is used within the TurnDraft to keep track of all proposed placements for the current turn.
 */
public class DraftPlacement {
    private String tileId;
    private Position position;

    public DraftPlacement(String tileId, Position position) {
        this.tileId = tileId;
        this.position = position;
    }

    public String getTileId() {
        return tileId;
    }

    public Position getPosition() {
        return position;
    }

    public void setTileId(String tileId) {
        this.tileId = tileId;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
