package com.kotva.ai;

import java.util.List;
import java.util.Objects;

/**
 * Holds the candidate moves returned by the AI engine.
 *
 * @param moves candidate AI moves
 */
public record AiMoveOptionSet(List<AiMove> moves) {

    /**
     * Copies and validates the move list.
     */
    public AiMoveOptionSet {
        moves = List.copyOf(Objects.requireNonNull(moves, "moves cannot be null."));
    }

    /**
     * Creates a set with one move.
     *
     * @param move move to include
     * @return option set with one move
     */
    public static AiMoveOptionSet ofSingle(AiMove move) {
        return new AiMoveOptionSet(List.of(Objects.requireNonNull(move, "move cannot be null.")));
    }

    /**
     * Returns whether there are no moves.
     *
     * @return true when empty
     */
    public boolean isEmpty() {
        return moves.isEmpty();
    }

    /**
     * Returns the number of candidate moves.
     *
     * @return move count
     */
    public int size() {
        return moves.size();
    }
}
