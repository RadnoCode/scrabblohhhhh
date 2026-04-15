package com.kotva.ai;

import java.util.List;
import java.util.Objects;

public record AiMoveOptionSet(List<AiMove> moves) {

    public AiMoveOptionSet {
        moves = List.copyOf(Objects.requireNonNull(moves, "moves cannot be null."));
    }

    public static AiMoveOptionSet ofSingle(AiMove move) {
        return new AiMoveOptionSet(List.of(Objects.requireNonNull(move, "move cannot be null.")));
    }

    public boolean isEmpty() {
        return moves.isEmpty();
    }

    public int size() {
        return moves.size();
    }
}