package com.kotva.application.preview;
import java.util.List;

import com.kotva.domain.model.Position;
import com.kotva.policy.WordType;

public class PreviewWord {
    private String word;
    private boolean valid;
    private int scoreContribution;
    private List<Position> coveredPositions;
    private WordType wordType;
    public WordType getWordType() {
        return wordType;
    }
    public String getWord() {
        return word;
    }
    public int getScoreContribution() {
        return scoreContribution;
    }
    public List<Position> getCoveredPositions() {
        return coveredPositions;
    }
}
