package com.kotva.application.preview;

import java.io.Serializable;
import java.util.List;

/**
 * Contains the result shown to the player while previewing a move.
 */
public class PreviewResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean valid;
    private int estimatedScore;
    private List<PreviewWord> words;
    private List<BoardHighlight> highlights;
    private List<String> messages;

    /**
     * Creates a preview result.
     *
     * @param valid whether the draft move is valid
     * @param estimatedScore estimated score if the move is valid
     * @param words words formed by the draft move
     * @param highlights board highlights for the UI
     * @param messages messages or validation errors for the player
     */
    public PreviewResult(boolean valid, int estimatedScore, List<PreviewWord> words, List<BoardHighlight> highlights, List<String> messages) {
        this.valid = valid;
        this.estimatedScore = estimatedScore;
        this.words = words;
        this.highlights = highlights;
        this.messages = messages;
    }

    /**
     * Checks whether the previewed move is valid.
     *
     * @return {@code true} if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the estimated score for the previewed move.
     *
     * @return estimated score
     */
    public int getEstimatedScore() {
        return estimatedScore;
    }

    /**
     * Gets the words formed by the move.
     *
     * @return preview word list
     */
    public List<PreviewWord> getWordList() {
        return words;
    }

    /**
     * Gets the board highlights for the move.
     *
     * @return highlight list
     */
    public List<BoardHighlight> getHighlights() {
        return highlights;
    }

    /**
     * Gets user-facing preview messages.
     *
     * @return message list
     */
    public List<String> getMessages() {
        return messages;
    }

}
