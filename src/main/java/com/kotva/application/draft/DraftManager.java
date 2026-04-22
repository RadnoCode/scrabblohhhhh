package com.kotva.application.draft;

import com.kotva.domain.model.Position;

public class DraftManager {

    public void placeTile(TurnDraft turnDraft, String tileId, Position position) {
        if(turnDraft == null || tileId == null || position == null) {
            throw new IllegalArgumentException("TurnDraft, tileId, and position must not be null");
        }

        DraftPlacement existingPlacement = findPlacementByTileId(turnDraft, tileId);
        if (existingPlacement != null) {
            existingPlacement.setPosition(position);
        }
        else{
            turnDraft.getPlacements().add(
                new DraftPlacement(
                    tileId,
                    position,
                    turnDraft.getAssignedLettersByTileId().get(tileId)));
        }
    }

    public void moveTile(TurnDraft turnDraft, String tileId, Position newPosition) {
        if(turnDraft == null || tileId == null || newPosition == null) {
            throw new IllegalArgumentException("TurnDraft, tileId, and newPosition must not be null");
        }

        DraftPlacement existing = findPlacementByTileId(turnDraft, tileId);
        if (existing == null) {
            throw new IllegalArgumentException("tileId is not in current draft: " + tileId);
        }

        existing.setPosition(newPosition);
    }

    public void removeTile(TurnDraft turnDraft, String tileId) {
        if(turnDraft == null || tileId == null) {
            throw new IllegalArgumentException("TurnDraft and tileId must not be null");
        }
        turnDraft.getPlacements().removeIf(placement -> tileId.equals(placement.getTileId()));
    }

    public void recallAllTiles(TurnDraft turnDraft) {
        if(turnDraft == null) {
            throw new IllegalArgumentException("TurnDraft must not be null");
        }
        turnDraft.getPlacements().clear();
        turnDraft.setDraggingTileId(null);
        turnDraft.setPreviewResult(null);
    }

    public DraftPlacement findPlacementByTileId(TurnDraft turnDraft, String tileId) {
        if (turnDraft == null || tileId == null) {
            throw new IllegalArgumentException("TurnDraft and tileId must not be null");
        }

        for (DraftPlacement placement : turnDraft.getPlacements()) {
            if (tileId.equals(placement.getTileId())) {
                return placement;
            }
        }
        return null; // Not found
    }
}
