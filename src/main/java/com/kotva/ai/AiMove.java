package com.kotva.ai;

import java.util.List;
import java.util.Objects;

/**
 * Describes one move returned by the AI engine.
 *
 * @param action move action type
 * @param placements tile placements for a place move
 * @param score expected move score
 * @param equity estimated move equity
 * @param win estimated win value
 */
public record AiMove(Action action, List<Placement> placements, int score, double equity, double win) {

    /**
     * Validates the AI move.
     */
    public AiMove {
        action = Objects.requireNonNull(action, "action cannot be null.");
        placements = List.copyOf(Objects.requireNonNull(placements, "placements cannot be null."));
        if (action == Action.PASS && !placements.isEmpty()) {
            throw new IllegalArgumentException("PASS action cannot contain placements.");
        }
    }

    /**
     * Types of moves the AI can return.
     */
    public enum Action {
        /**
         * Place tiles on the board.
         */
        PLACE,
        /**
         * Pass the turn.
         */
        PASS
    }

    /**
     * Describes one tile placement chosen by the AI.
     *
     * @param row target board row
     * @param col target board column
     * @param letter tile letter
     * @param blank whether the tile is blank
     * @param assignedLetter letter assigned to a blank tile
     */
    public record Placement(int row, int col, char letter, boolean blank, Character assignedLetter) {

        /**
         * Validates and normalizes the placement.
         */
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
