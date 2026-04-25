package com.kotva.application.draft;

import com.kotva.domain.model.Position;
import java.io.Serializable;

public class DraftPlacement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tileId;
    private Position position;
    private Character assignedLetter;

    public DraftPlacement(String tileId, Position position) {
        this(tileId, position, null);
    }

    public DraftPlacement(String tileId, Position position, Character assignedLetter) {
        this.tileId = tileId;
        this.position = position;
        this.assignedLetter = assignedLetter == null ? null : Character.toUpperCase(assignedLetter);
    }

    public String getTileId() {
        return tileId;
    }

    public Position getPosition() {
        return position;
    }

    public Character getAssignedLetter() {
        return assignedLetter;
    }

    public void setTileId(String tileId) {
        this.tileId = tileId;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setAssignedLetter(Character assignedLetter) {
        this.assignedLetter = assignedLetter == null ? null : Character.toUpperCase(assignedLetter);
    }
}
