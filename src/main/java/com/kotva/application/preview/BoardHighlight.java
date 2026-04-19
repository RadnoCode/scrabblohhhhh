package com.kotva.application.preview;

import com.kotva.domain.model.Position;


public class BoardHighlight {
    private final Position position;
    private final HighlightType highlightType;
    public BoardHighlight(Position position, HighlightType highlightType) {
        this.position = position;
        this.highlightType = highlightType;
    }

    public Position getPosition() {
        return position;
    }

    public HighlightType getHighlightType() {
        return highlightType;
    }

}
