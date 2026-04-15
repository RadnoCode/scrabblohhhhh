package com.kotva.application.draft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kotva.application.preview.PreviewResult;

public class TurnDraft
{

    private List<DraftPlacement> placements;  // List of tile placements for the current turn (in this turn, we put how many tiles on the board and where they are placed)
    private Map<String, Integer> originalRackSlots;  //record the original rack slots of the tiles being moved, so that we can restore them if the draft is cancelled or invalid

    private String draggingTileId; // The ID of the tile currently being dragged by the player, if any. This helps the UI to provide visual feedback and manage tile movements during the drafting phase.
    private PreviewResult previewResult; //analyze and tell the player the result of the current draft, including estimated score, formed words, and any rule violations or messages. This allows the player to see the potential outcome of their proposed move before finalizing it.

    public TurnDraft() {
        this.placements = new ArrayList<>();
        this.originalRackSlots = new HashMap<>();
        this.draggingTileId = null;
        this.previewResult = null;
    }

    public List<DraftPlacement> getPlacements() {
        return placements;
    }

    public PreviewResult getPreviewResult() {
        return previewResult;
    }

    public Map<String, Integer> getOriginalRackSlots() {
        return originalRackSlots;
    }

    public String getDraggingTileId() {
        return draggingTileId;
    }

    public void setDraggingTileId(String draggingTileId) {
        this.draggingTileId = draggingTileId;
    }

    public void setPreviewResult(PreviewResult previewResult) {
        this.previewResult = previewResult;
    }

}