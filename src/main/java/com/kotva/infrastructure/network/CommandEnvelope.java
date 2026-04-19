package com.kotva.infrastructure.network;

import com.kotva.domain.action.PlayerAction;
import java.io.Serializable;
import java.util.Objects;

public class CommandEnvelope implements Serializable {
    private final String commandId;
    private final String sessionId;
    private final String playerId;
    private final int expectedTurnNumber;
    private final PlayerAction action;

    public CommandEnvelope(
            String commandId,
            String sessionId,
            String playerId,
            int expectedTurnNumber,
            PlayerAction action) {
        this.commandId = Objects.requireNonNull(commandId, "commandId cannot be null.");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null.");
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
        this.expectedTurnNumber = expectedTurnNumber;
        this.action = Objects.requireNonNull(action, "action cannot be null.");
    }

    public String getCommandId() {
        return commandId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getExpectedTurnNumber() {
        return expectedTurnNumber;
    }

    public PlayerAction getAction() {
        return action;
    }
}
