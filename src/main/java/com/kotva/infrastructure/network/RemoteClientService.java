package com.kotva.infrastructure.network;

import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.action.PlayerAction;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Small helper for client-side command creation in the network layer.
 *
 * Draft and preview state stay in application.service.client; this class only
 * wraps already-built actions for transport.
 */
public class RemoteClientService {
    public CommandEnvelope buildCommand(
            String sessionId, String playerId, int expectedTurnNumber, PlayerAction action) {
        return new CommandEnvelope(
                UUID.randomUUID().toString(),
                Objects.requireNonNull(sessionId, "sessionId cannot be null."),
                Objects.requireNonNull(playerId, "playerId cannot be null."),
                expectedTurnNumber,
                Objects.requireNonNull(action, "action cannot be null."));
    }

    public PlayerAction createAction(
            String playerId, ActionType type, List<ActionPlacement> placements) {
        Objects.requireNonNull(type, "type cannot be null.");
        return switch (type) {
            case PLACE_TILE -> PlayerAction.place(playerId, placements);
            case PASS_TURN -> PlayerAction.pass(playerId);
            case LOSE -> PlayerAction.lose(playerId);
        };
    }
}
