package com.kotva.application.session;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Serializable preview data included in a game session snapshot.
 */
public class PreviewSnapshot implements Serializable {
    private final boolean valid;
    private final int estimatedScore;
    private final List<PreviewWordSnapshot> words;
    private final List<PreviewHighlightSnapshot> highlights;
    private final List<String> messages;

    /**
     * Creates a preview snapshot.
     *
     * @param valid whether the previewed move is valid
     * @param estimatedScore estimated score for the move
     * @param words words formed by the move
     * @param highlights board highlights
     * @param messages preview messages for the player
     */
    public PreviewSnapshot(
        boolean valid,
        int estimatedScore,
        List<PreviewWordSnapshot> words,
        List<PreviewHighlightSnapshot> highlights,
        List<String> messages) {
        this.valid = valid;
        this.estimatedScore = estimatedScore;
        this.words = List.copyOf(Objects.requireNonNull(words, "words cannot be null."));
        this.highlights =
        List.copyOf(Objects.requireNonNull(highlights, "highlights cannot be null."));
        this.messages = List.copyOf(Objects.requireNonNull(messages, "messages cannot be null."));
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
     * Gets the estimated score.
     *
     * @return estimated score
     */
    public int getEstimatedScore() {
        return estimatedScore;
    }

    /**
     * Gets previewed words.
     *
     * @return word snapshots
     */
    public List<PreviewWordSnapshot> getWords() {
        return words;
    }

    /**
     * Gets preview highlights.
     *
     * @return highlight snapshots
     */
    public List<PreviewHighlightSnapshot> getHighlights() {
        return highlights;
    }

    /**
     * Gets preview messages.
     *
     * @return message list
     */
    public List<String> getMessages() {
        return messages;
    }
}
