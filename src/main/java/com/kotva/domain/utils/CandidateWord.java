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
        // 内存地址正确直接过
        if (this == other) {
            return true;
        }
        // 类型不正确直接错
        if (!(other instanceof CandidateWord)) {
            return false;
        }

        // 类型正确，地址不对，看是不是同一个
        CandidateWord that = (CandidateWord) other;

        boolean isWordSame = Objects.equals(this.word, that.word);

        // 【核心修改点】：手动对比起点的行和列
        boolean isStartSame = (this.startPosition.getRow() == that.startPosition.getRow())
                && (this.startPosition.getCol() == that.startPosition.getCol());

        // 【核心修改点】：手动对比终点的行和列
        boolean isEndSame = (this.endPosition.getRow() == that.endPosition.getRow())
                && (this.endPosition.getCol() == that.endPosition.getCol());

        return isWordSame && isStartSame && isEndSame;
    }

    @Override
    public int hashCode() {
        // 【核心修改点】：因为没用底层的 Position，这里也要手动把行列数字抠出来算哈希
        return Objects.hash(
                word,
                startPosition.getRow(), startPosition.getCol(),
                endPosition.getRow(), endPosition.getCol()
        );
    }
}