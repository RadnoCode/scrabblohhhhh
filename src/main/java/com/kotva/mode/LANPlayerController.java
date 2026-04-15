package com.kotva.mode;

import com.kotva.domain.action.PlayerAction;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.policy.PlayerType;
import java.util.Objects;
import java.util.UUID;

final class LANPlayerController extends PlayerController {
    LANPlayerController(String playerId) {
        super(playerId, PlayerType.LAN);
    }

    @Override
    public CommandEnvelope buildLanCommand(
            String sessionId, int expectedTurnNumber, PlayerAction action) {
        Objects.requireNonNull(action, "action cannot be null.");
        Objects.requireNonNull(sessionId, "sessionId cannot be null.");
        return new CommandEnvelope(
                UUID.randomUUID().toString(),
                sessionId,
                getPlayerId(),
                expectedTurnNumber,
                action);
    }
}
