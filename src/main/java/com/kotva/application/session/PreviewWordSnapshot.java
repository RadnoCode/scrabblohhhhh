package com.kotva.application.session;

import com.kotva.policy.WordType;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Serializable snapshot of one word in a move preview.
 */
public class PreviewWordSnapshot implements Serializable {
    private final String word;
    private final boolean valid;
    private final int scoreContribution;
    private final List<PreviewPositionSnapshot> coveredPositions;
    private final WordType wordType;

    /**
     * Creates a preview word snapshot.
     *
     * @param word word text
     * @param valid whether the word is valid
     * @param scoreContribution score from this word
     * @param coveredPositions board positions covered by the word
     * @param wordType main word or cross word type
     */
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

    /**
     * Gets the word text.
     *
     * @return word text
     */
    public String getWord() {
        return word;
    }

    /**
     * Checks whether this word is valid.
     *
     * @return {@code true} if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the score from this word.
     *
     * @return score contribution
     */
    public int getScoreContribution() {
        return scoreContribution;
    }

    /**
     * Gets covered board positions.
     *
     * @return covered positions
     */
    public List<PreviewPositionSnapshot> getCoveredPositions() {
        return coveredPositions;
    }

    /**
     * Gets the word type.
     *
     * @return word type
     */
    public WordType getWordType() {
        return wordType;
    }
}
