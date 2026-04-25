package com.kotva.application.preview;

import com.kotva.domain.model.Position;
import java.io.Serializable;

/**
 * Stores one highlighted board position for move preview.
 */
public class BoardHighlight implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Position position;
    private final HighlightType highlightType;

    /**
     * Creates a board highlight.
     *
     * @param position board position to highlight
     * @param highlightType visual meaning of the highlight
     */
    public BoardHighlight(Position position, HighlightType highlightType) {
        this.position = position;
        this.highlightType = highlightType;
    }

    /**
     * Gets the highlighted board position.
     *
     * @return highlighted position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets the type of highlight to show.
     *
     * @return highlight type
     */
    public HighlightType getHighlightType() {
        return highlightType;
    }

}
