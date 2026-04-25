package com.kotva.application.session;

import com.kotva.application.preview.HighlightType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Serializable highlight information for a previewed board cell.
 */
public class PreviewHighlightSnapshot implements Serializable {
    private final int row;
    private final int col;
    private final HighlightType highlightType;

    /**
     * Creates a preview highlight snapshot.
     *
     * @param row board row
     * @param col board column
     * @param highlightType highlight type
     */
    public PreviewHighlightSnapshot(int row, int col, HighlightType highlightType) {
        this.row = row;
        this.col = col;
        this.highlightType =
        Objects.requireNonNull(highlightType, "highlightType cannot be null.");
    }

    /**
     * Gets the board row.
     *
     * @return row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets the board column.
     *
     * @return column index
     */
    public int getCol() {
        return col;
    }

    /**
     * Gets the highlight type.
     *
     * @return highlight type
     */
    public HighlightType getHighlightType() {
        return highlightType;
    }
}
