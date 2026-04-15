package com.kotva.application.draft;

import com.kotva.domain.model.Position;

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