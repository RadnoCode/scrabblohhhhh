package com.kotva.application.session;

import java.util.List;
import java.util.Objects;

public class PreviewSnapshot {
    private final boolean valid;
    private final int estimatedScore;
    private final List<String> messages;

    public PreviewSnapshot(boolean valid, int estimatedScore, List<String> messages) {
        this.valid = valid;
        this.estimatedScore = estimatedScore;
        this.messages = List.copyOf(Objects.requireNonNull(messages, "messages cannot be null."));
    }

    public boolean isValid() {
        return valid;
    }

    public int getEstimatedScore() {
        return estimatedScore;
    }

    public List<String> getMessages() {
        return messages;
    }
}
