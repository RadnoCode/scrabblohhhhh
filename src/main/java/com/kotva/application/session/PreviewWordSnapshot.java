package com.kotva.application.session;

import com.kotva.policy.WordType;
import java.util.List;
import java.util.Objects;

public class PreviewWordSnapshot {
    private final String word;
    private final boolean valid;
    private final int scoreContribution;
    private final List<PreviewPositionSnapshot> coveredPositions;
    private final WordType wordType;

    public PreviewWordSnapshot(
        String word,
        boolean valid,
        int scoreContribution,
        List<PreviewPositionSnapshot> coveredPositions,
        WordType wordType) {
        this.word = word;
        this.valid = valid;
        this.scoreContribution = scoreContribution;
        this.coveredPositions =
        List.copyOf(
            Objects.requireNonNull(
            coveredPositions, "coveredPositions cannot be null."));
        this.wordType = wordType;
    }

    public String getWord() {
        return word;
    }

    public boolean isValid() {
        return valid;
    }

    public int getScoreContribution() {
        return scoreContribution;
    }

    public List<PreviewPositionSnapshot> getCoveredPositions() {
        return coveredPositions;
    }

    public WordType getWordType() {
        return wordType;
    }
}