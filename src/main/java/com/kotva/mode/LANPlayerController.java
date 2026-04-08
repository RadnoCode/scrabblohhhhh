package com.kotva.mode;

import com.kotva.application.session.GameSession;
import com.kotva.domain.action.PlayerAction;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.policy.PlayerType;
import java.util.Objects;
import java.util.UUID;

final class LANPlayerController extends PlayerController {
    LANPlayerController(String playerId) {
        super(playerId, PlayerType.LAN);
    }

    CommandEnvelope buildCommand(PlayerAction action, GameSession session) {
        Objects.requireNonNull(action, "action cannot be null.");
        Objects.requireNonNull(session, "session cannot be null.");
        return new CommandEnvelope(
                UUID.randomUUID().toString(),
                session.getSessionId(),
                getPlayerId(),
                session.getTurnCoordinator().getTurnNumber(),
                action);
    }
}
