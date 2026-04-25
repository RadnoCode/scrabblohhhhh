package com.kotva.application.preview;

import com.kotva.domain.model.Position;
import java.io.Serializable;

public class BoardHighlight implements Serializable {
    private static final long serialVersionUID = 1L;

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
