package com.kotva.application.draft;

import com.kotva.domain.model.Position;

/**
 * DraftManager is responsible for managing the player's current turn draft, 
 * including placing, moving, and removing tiles from the draft, as well as recalling all tiles.
 */

public class DraftManager {

    /**
     * Places a tile in the player's current turn draft. If the tile is already in the draft, its position will be updated, if not, it will be added as a new placement.
     * @param turnDraft current turn draft to modify
     * @param tileId the ID of the tile being placed
     * @param position the position on the board where the tile is being placed
     */
    public void placeTile(TurnDraft turnDraft, String tileId, Position position) {
        //validation
        if(turnDraft == null || tileId == null || position == null) {
            throw new IllegalArgumentException("TurnDraft, tileId, and position must not be null");
        }

        // Check if the tile is already in the draft
        DraftPlacement existingPlacement = findPlacementByTileId(turnDraft, tileId);
        if (existingPlacement != null) {
            existingPlacement.setPosition(position);
        }
        else{

            turnDraft.getPlacements().add(new DraftPlacement(tileId, position));
        }
    }

    /**
     * Moves a tile that is already in the player's current turn draft to a new position. If the tile is not in the draft, an exception will be thrown.
     * @param turnDraft current turn draft to modify
     * @param tileId the ID of the tile being moved
     * @param newPosition the new position on the board where the tile is being moved to
     */
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

    /**
     * Removes a tile from the player's current turn draft. 
     * @param turnDraft current turn draft to modify
     * @param tileId the ID of the tile being removed
     */
    public void removeTile(TurnDraft turnDraft, String tileId) {
        if(turnDraft == null || tileId == null) {
            throw new IllegalArgumentException("TurnDraft and tileId must not be null");
        }
        turnDraft.getPlacements().removeIf(placement -> tileId.equals(placement.getTileId()));

    }

    /**
     * Recalls all tiles from the player's current turn draft.
     * @param turnDraft current turn draft to modify
     */
    public void recallAllTiles(TurnDraft turnDraft) {
        if(turnDraft == null) {
            throw new IllegalArgumentException("TurnDraft must not be null");
        }
        turnDraft.getPlacements().clear();
        turnDraft.setDraggingTileId(null);
        turnDraft.setPreviewResult(null);
    }

    /**
     * (helper method) Finds a DraftPlacement in the current turn draft by tile ID.
     * @param turnDraft current turn draft to search
     * @param tileId the ID of the tile to find
     * @return the DraftPlacement with the specified tileId
     */
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
