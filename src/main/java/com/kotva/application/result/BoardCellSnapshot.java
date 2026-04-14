package com.kotva.application.result;

import com.kotva.policy.BonusType;
import java.util.Objects;

public class BoardCellSnapshot {
    private final int row;
    private final int col;
    private final BonusType bonusType;
    private final Character letter;
    private final int score;
    private final boolean blank;

    public BoardCellSnapshot(
            int row,
            int col,
            BonusType bonusType,
            Character letter,
            int score,
            boolean blank) {
        this.row = row;
        this.col = col;
        this.bonusType = Objects.requireNonNull(bonusType, "bonusType cannot be null.");
        this.letter = letter;
        this.score = score;
        this.blank = blank;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public Character getLetter() {
        return letter;
    }

    public int getScore() {
        return score;
    }

    public boolean isBlank() {
        return blank;
    }
}
