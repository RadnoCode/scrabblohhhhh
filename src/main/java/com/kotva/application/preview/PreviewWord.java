package com.kotva.application.preview;
import java.util.List;
import java.util.Objects;

import com.kotva.domain.model.Position;
import com.kotva.policy.WordType;

public class PreviewWord {
    private final String word;
    private final boolean valid;
    private final int scoreContribution;
    private final List<Position> coveredPositions;
    private final WordType wordType;

    public PreviewWord(
            String word,
            boolean valid,
            int scoreContribution,
            List<Position> coveredPositions,
            WordType wordType) {
        this.word = word;
        this.valid = valid;
        this.scoreContribution = scoreContribution;
        this.coveredPositions =
                List.copyOf(
                        Objects.requireNonNull(
                                coveredPositions, "coveredPositions cannot be null."));
        this.wordType = Objects.requireNonNull(wordType, "wordType cannot be null.");
    }

    public WordType getWordType() {
        return wordType;
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
    public List<Position> getCoveredPositions() {
        return coveredPositions;
    }
}
