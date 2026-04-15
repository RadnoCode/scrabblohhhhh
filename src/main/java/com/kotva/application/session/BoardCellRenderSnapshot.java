package com.kotva.application.session;

import com.kotva.policy.BonusType;
import java.util.Objects;

public class BoardCellRenderSnapshot {
    private final int row;
    private final int col;
    private final BonusType bonusType;
    private final String tileId;
    private final Character displayLetter;
    private final int score;
    private final boolean blank;
    private final boolean draft;
    private final boolean previewValid;
    private final boolean previewInvalid;
    private final boolean mainWordHighlighted;
    private final boolean crossWordHighlighted;

    public BoardCellRenderSnapshot(
            int row,
            int col,
            BonusType bonusType,
            String tileId,
            Character displayLetter,
            int score,
            boolean blank,
            boolean draft,
            boolean previewValid,
            boolean previewInvalid,
            boolean mainWordHighlighted,
            boolean crossWordHighlighted) {
        this.row = row;
        this.col = col;
        this.bonusType = Objects.requireNonNull(bonusType, "bonusType cannot be null.");
        this.tileId = Objects.requireNonNull(tileId, "tileId cannot be null.");
        this.displayLetter = displayLetter;
        this.score = score;
        this.blank = blank;
        this.draft = draft;
        this.previewValid = previewValid;
        this.previewInvalid = previewInvalid;
        this.mainWordHighlighted = mainWordHighlighted;
        this.crossWordHighlighted = crossWordHighlighted;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public String getTileId() {
        return tileId;
    }

    public Character getDisplayLetter() {
        return displayLetter;
    }

    public int getScore() {
        return score;
    }

    public boolean isBlank() {
        return blank;
    }

    public boolean isDraft() {
        return draft;
    }

    public boolean isPreviewValid() {
        return previewValid;
    }

    public boolean isPreviewInvalid() {
        return previewInvalid;
    }

    public boolean isMainWordHighlighted() {
        return mainWordHighlighted;
    }

    public boolean isCrossWordHighlighted() {
        return crossWordHighlighted;
    }
}
