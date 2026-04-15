package com.kotva.application.session;

import com.kotva.application.preview.HighlightType;
import java.util.Objects;

public class PreviewHighlightSnapshot {
    private final int row;
    private final int col;
    private final HighlightType highlightType;

    public PreviewHighlightSnapshot(int row, int col, HighlightType highlightType) {
        this.row = row;
        this.col = col;
        this.highlightType =
        Objects.requireNonNull(highlightType, "highlightType cannot be null.");
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public HighlightType getHighlightType() {
        return highlightType;
    }
}