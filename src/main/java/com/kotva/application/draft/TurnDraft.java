package com.kotva.application.draft;
import com.kotva.application.preview.PreviewResult;
import java.util.List;
import java.util.Map;

public class TurnDraft {
    private List<DraftPlacement> placements;
    private Map<String, Integer> originalRackSlots;
    private String draggingTileId;
    private PreviewResult previewResult;
    
    public List<DraftPlacement> getPlacements() {
        return placements;
    }
    public PreviewResult getPreviewResult() {
        return previewResult;
    }

}
