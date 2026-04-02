package com.kotva.application.preview;

import java.util.List;

public class PreviewResult {
    private boolean valid; // Indicates if the move is valid
    private int estimatedScore; // Estimated score for the move
    private List<PreviewWord> words; // List of words formed by the move
    private List<BoardHighlight> highlights; // Highlights for the board (e.g., new tiles, affected tiles)
    private List<String> messages; // Any messages or rule violations related to the move (e.g., "Tile must be placed adjacent to existing tiles")

    public PreviewResult(boolean valid, int estimatedScore, List<PreviewWord> words, List<BoardHighlight> highlights, List<String> messages) {
        this.valid = valid;
        this.estimatedScore = estimatedScore;
        this.words = words;
        this.highlights = highlights;
        this.messages = messages;
    }

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
