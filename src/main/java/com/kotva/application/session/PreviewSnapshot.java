package com.kotva.application.session;

import java.util.List;
import java.util.Objects;

public class PreviewSnapshot {
    private final boolean valid;
    private final int estimatedScore;
    private final List<PreviewWordSnapshot> words;
    private final List<PreviewHighlightSnapshot> highlights;
    private final List<String> messages;

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

    public boolean isValid() {
        return valid;
    }

    public int getEstimatedScore() {
        return estimatedScore;
    }

    public List<PreviewWordSnapshot> getWords() {
        return words;
    }

    public List<PreviewHighlightSnapshot> getHighlights() {
        return highlights;
    }

    public List<String> getMessages() {
        return messages;
    }
}