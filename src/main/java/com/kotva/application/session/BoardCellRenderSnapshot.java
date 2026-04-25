package com.kotva.application.session;

import com.kotva.policy.BonusType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Snapshot of one board cell as it should be rendered by the UI.
 */
public class BoardCellRenderSnapshot implements Serializable {
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

    /**
     * Creates a render snapshot for one board cell.
     *
     * @param row board row
     * @param col board column
     * @param bonusType cell bonus type
     * @param tileId placed tile id, or empty string when no tile is present
     * @param displayLetter letter shown on the tile
     * @param score tile score
     * @param blank whether the tile is blank
     * @param draft whether the tile belongs to the current draft
     * @param previewValid whether preview marks this cell as valid
     * @param previewInvalid whether preview marks this cell as invalid
     * @param mainWordHighlighted whether this cell is in the main word
     * @param crossWordHighlighted whether this cell is in a cross word
     */
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
     * Gets the cell bonus type.
     *
     * @return bonus type
     */
    public BonusType getBonusType() {
        return bonusType;
    }

    /**
     * Gets the tile id on this cell.
     *
     * @return tile id, or empty string when no tile exists
     */
    public String getTileId() {
        return tileId;
    }

    /**
     * Gets the displayed letter.
     *
     * @return display letter, or {@code null}
     */
    public Character getDisplayLetter() {
        return displayLetter;
    }

    /**
     * Gets the tile score.
     *
     * @return tile score
     */
    public int getScore() {
        return score;
    }

    /**
     * Checks whether this tile is blank.
     *
     * @return {@code true} if blank
     */
    public boolean isBlank() {
        return blank;
    }

    /**
     * Checks whether this tile is from the current draft.
     *
     * @return {@code true} if draft tile
     */
    public boolean isDraft() {
        return draft;
    }

    /**
     * Checks whether preview marks this cell as valid.
     *
     * @return {@code true} if valid in preview
     */
    public boolean isPreviewValid() {
        return previewValid;
    }

    /**
     * Checks whether preview marks this cell as invalid.
     *
     * @return {@code true} if invalid in preview
     */
    public boolean isPreviewInvalid() {
        return previewInvalid;
    }

    /**
     * Checks whether this cell is part of the main word.
     *
     * @return {@code true} if highlighted as main word
     */
    public boolean isMainWordHighlighted() {
        return mainWordHighlighted;
    }

    /**
     * Checks whether this cell is part of a cross word.
     *
     * @return {@code true} if highlighted as cross word
     */
    public boolean isCrossWordHighlighted() {
        return crossWordHighlighted;
    }
}
