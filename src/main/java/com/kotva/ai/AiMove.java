package com.kotva.ai;

import java.util.List;
import java.util.Objects;

public record AiMove(Action action, List<Placement> placements, int score, double equity, double win) {
    public AiMove {
        action = Objects.requireNonNull(action, "action cannot be null.");
        placements = List.copyOf(Objects.requireNonNull(placements, "placements cannot be null."));
        if (action == Action.PASS && !placements.isEmpty()) {
            throw new IllegalArgumentException("PASS action cannot contain placements.");
        }
    }

    public enum Action {
        PLACE,
        PASS
    }

    public record Placement(int row, int col, char letter, boolean blank, Character assignedLetter) {
        public Placement {
            if (row < 0 || row >= AiPositionSnapshot.BOARD_SIDE) {
                throw new IllegalArgumentException("row out of bounds: " + row);
            }
            if (col < 0 || col >= AiPositionSnapshot.BOARD_SIDE) {
                throw new IllegalArgumentException("col out of bounds: " + col);
            }

            letter = Character.toUpperCase(letter);
            if (!Character.isAlphabetic(letter)) {
                throw new IllegalArgumentException("letter must be alphabetic.");
            }

            if (blank) {
                if (assignedLetter == null) {
                    assignedLetter = letter;
                } else {
                    assignedLetter = Character.toUpperCase(assignedLetter);
                }
            } else if (assignedLetter != null) {
                assignedLetter = Character.toUpperCase(assignedLetter);
            }
        }
    }
}
