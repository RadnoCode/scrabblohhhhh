package com.kotva.application.draft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kotva.application.preview.PreviewResult;

/**
 * Stores the editable placement state for one player's current turn.
 *
 * <p>A turn draft is an application-layer object used before a move is
 * submitted to the domain model. It records which rack tiles have been placed
 * on the board, remembers their original rack slots for UI recovery, keeps
 * blank-tile letter choices, and stores the latest preview result.</p>
 */
public class TurnDraft implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<DraftPlacement> placements;
    private Map<String, Integer> originalRackSlots;
    private Map<String, Character> assignedLettersByTileId;

    private String draggingTileId;
    private PreviewResult previewResult;

    /**
     * Creates an empty draft with no placements, no assigned blank letters, and
     * no cached preview result.
     */
    public TurnDraft() {
        this.placements = new ArrayList<>();
        this.originalRackSlots = new HashMap<>();
        this.assignedLettersByTileId = new HashMap<>();
        this.draggingTileId = null;
        this.previewResult = null;
    }

    /**
     * Returns the mutable list of tile placements currently staged on the board.
     *
     * @return staged tile placements for this turn
     */
    public List<DraftPlacement> getPlacements() {
        return placements;
    }

    /**
     * Returns the latest preview calculated for this draft.
     *
     * @return current preview result, or {@code null} if no preview is available
     */
    public PreviewResult getPreviewResult() {
        return previewResult;
    }

    /**
     * Returns the original rack indexes for tiles moved into the draft.
     *
     * @return mapping from tile id to its original rack slot index
     */
    public Map<String, Integer> getOriginalRackSlots() {
        return originalRackSlots;
    }

    /**
     * Returns the selected letters for blank tiles in this draft.
     *
     * @return mapping from blank tile id to assigned uppercase letter
     */
    public Map<String, Character> getAssignedLettersByTileId() {
        return assignedLettersByTileId;
    }

    /**
     * Returns the tile currently being dragged by the UI.
     *
     * @return dragging tile id, or {@code null} when no tile is being dragged
     */
    public String getDraggingTileId() {
        return draggingTileId;
    }

    /**
     * Updates the tile currently being dragged by the UI.
     *
     * @param draggingTileId tile id being dragged, or {@code null} to clear it
     */
    public void setDraggingTileId(String draggingTileId) {
        this.draggingTileId = draggingTileId;
    }

    /**
     * Stores the latest preview calculated for this draft.
     *
     * @param previewResult preview result to cache, or {@code null} to clear it
     */
    public void setPreviewResult(PreviewResult previewResult) {
        this.previewResult = previewResult;
    }

}
