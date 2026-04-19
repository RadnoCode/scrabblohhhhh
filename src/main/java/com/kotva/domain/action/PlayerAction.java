package com.kotva.domain.action;

import java.util.List;
import java.util.Objects;

public record PlayerAction(String playerId, ActionType type, List<ActionPlacement> placements) {

    public PlayerAction {
        Objects.requireNonNull(playerId, "playerId cannot be null.");
        Objects.requireNonNull(type, "type cannot be null.");
        placements = List.copyOf(Objects.requireNonNull(placements, "placements cannot be null."));
    }

    public static PlayerAction place(String playerId, List<ActionPlacement> placements) {
        return new PlayerAction(playerId, ActionType.PLACE_TILE, placements);
    }

    public static PlayerAction pass(String playerId) {
        return new PlayerAction(playerId, ActionType.PASS_TURN, List.of());
    }

    public static PlayerAction lose(String playerId) {
        return new PlayerAction(playerId, ActionType.LOSE, List.of());
    }
}
