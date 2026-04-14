package com.kotva.infrastructure.network;

import com.kotva.domain.action.PlayerAction;
import java.util.Objects;

public class CommandEnvelope {
    private final String commandId;
    private final String sessionId;
    private final String playerId;
    private final int expectTurnNumber;
    private final PlayerAction action;

    public CommandEnvelope(
            String commandId,
            String sessionId,
            String playerId,
            int expectTurnNumber,
            PlayerAction action) {
        this.commandId = Objects.requireNonNull(commandId, "commandId cannot be null.");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null.");
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
        this.expectTurnNumber = expectTurnNumber;
        this.action = Objects.requireNonNull(action, "action cannot be null.");
    }

    public PlayerAction getAction() {
        return action;
    }

    public String getCommandId() {
        return commandId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getExpectTurnNumber() {
        return expectTurnNumber;
    }

    public int getExpectedTurnNumber() {
        return expectTurnNumber;
    }
}

