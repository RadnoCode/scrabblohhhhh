package com.kotva.application.draft;

import com.kotva.domain.Position;

public class DraftManager {
    public void placeTile(TurnDraft turnDraft, String tileId, Position position) {
        //validation
        if(turnDraft == null || tileId == null || position == null) {
            throw new IllegalArgumentException("TurnDraft, tileId, and position must not be null");
        }

        
    }

    
}
