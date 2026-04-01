package com.kotva.application.preview;
import com.kotva.domain.Position;
import com.kotva.policy.WordType;
import java.util.List;

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
