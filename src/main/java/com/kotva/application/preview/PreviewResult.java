package com.kotva.application.preview;

import java.util.List;

public class PreviewResult {
    private boolean valid;
    private int estimatedScore;
    private List<PreviewWord> words;
    private List<BoardHighlight> highlights;
    private List<String> messages;
    public boolean isValid() {
        return valid;
    }
    public int getEstimatedScore() {
        return estimatedScore;
    }
    public List<PreviewWord> getWordList() {
        return words;
    }
    public List<BoardHighlight> getHighlights() {
        return highlights;
    }
    public List<String> getMessages() {
        return messages;
    }
}
