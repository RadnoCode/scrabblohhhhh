package com.kotva.domain.utils;

import java.util.Objects;
import com.kotva.domain.model.Position;

public class CandidateWord {
    private final String word;
    private final Position startPosition;
    private final Position endPosition;

    public CandidateWord(String word, Position startPosition, Position endPosition) {
        this.word = word;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public String getWord() {
        return word;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getEndPosition() {
        return endPosition;
    }

        @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CandidateWord)) {
            return false;
        }

        CandidateWord that = (CandidateWord) other;

        boolean isWordSame = Objects.equals(this.word, that.word);

        boolean isStartSame = (this.startPosition.getRow() == that.startPosition.getRow())
        && (this.startPosition.getCol() == that.startPosition.getCol());

        boolean isEndSame = (this.endPosition.getRow() == that.endPosition.getRow())
        && (this.endPosition.getCol() == that.endPosition.getCol());

        return isWordSame && isStartSame && isEndSame;
    }

        @Override
    public int hashCode() {
        return Objects.hash(
            word,
            startPosition.getRow(), startPosition.getCol(),
            endPosition.getRow(), endPosition.getCol()
        );
    }
}