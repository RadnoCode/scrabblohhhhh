package com.kotva.application.draft;

import com.kotva.domain.model.Position;

/**
 * Provides mutation operations for a {@link TurnDraft}.
 *
 * <p>This class centralizes draft editing rules used by both host-side and
 * client-side application services. It only changes the temporary draft state;
 * final rule validation and board updates happen later when the draft is
 * converted into a domain {@code PlayerAction} and submitted.</p>
 */
public class DraftManager {

    /**
     * Places a tile into the draft or updates the position if the tile is already staged.
     *
     * @param turnDraft draft to mutate
     * @param tileId id of the tile being placed
     * @param position target board position
     * @throws IllegalArgumentException if any argument is {@code null}
     */
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

    /**
     * Moves an already staged tile to a new board position.
     *
     * @param turnDraft draft to mutate
     * @param tileId id of the staged tile
     * @param newPosition new target board position
     * @throws IllegalArgumentException if an argument is {@code null} or the tile
     *         is not currently in the draft
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
     * Removes a staged tile from the draft.
     *
     * @param turnDraft draft to mutate
     * @param tileId id of the tile to remove
     * @throws IllegalArgumentException if {@code turnDraft} or {@code tileId} is {@code null}
     */
    public void removeTile(TurnDraft turnDraft, String tileId) {
        if(turnDraft == null || tileId == null) {
            throw new IllegalArgumentException("TurnDraft and tileId must not be null");
        }
        turnDraft.getPlacements().removeIf(placement -> tileId.equals(placement.getTileId()));
    }

    /**
     * Clears all staged placements and transient UI-related draft state.
     *
     * @param turnDraft draft to clear
     * @throws IllegalArgumentException if {@code turnDraft} is {@code null}
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
     * Finds a staged placement by tile id.
     *
     * @param turnDraft draft to search
     * @param tileId id of the tile to find
     * @return matching placement, or {@code null} when the tile is not staged
     * @throws IllegalArgumentException if {@code turnDraft} or {@code tileId} is {@code null}
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
        return null;
    }
}
