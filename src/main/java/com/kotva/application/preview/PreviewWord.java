package com.kotva.application.preview;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.kotva.domain.model.Position;
import com.kotva.policy.WordType;

/**
 * Describes one word found during move preview.
 */
public class PreviewWord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String word;
    private final boolean valid;
    private final int scoreContribution;
    private final List<Position> coveredPositions;
    private final WordType wordType;

    /**
     * Creates a preview word.
     *
     * @param word text of the word
     * @param valid whether the word is accepted by the dictionary
     * @param scoreContribution score from this word
     * @param coveredPositions board positions covered by the word
     * @param wordType whether this is the main word or a cross word
     */
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

    /**
     * Gets the type of preview word.
     *
     * @return word type
     */
    public WordType getWordType() {
        return wordType;
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
     * @return {@code true} if the word is valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the score contributed by this word.
     *
     * @return score contribution
     */
    public int getScoreContribution() {
        return scoreContribution;
    }

    /**
     * Gets the board positions covered by this word.
     *
     * @return covered positions
     */
    public List<Position> getCoveredPositions() {
        return coveredPositions;
    }
}
