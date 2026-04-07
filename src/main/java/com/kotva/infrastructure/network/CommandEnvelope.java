package com.kotva.infrastructure.network;

import com.kotva.domain.action.PlayerAction;

public class CommandEnvelope {
    private String commandId;
    private String sessionId;
    private String playerId;
    private int expectTurnNumber;
    private PlayerAction action;

    public CommandEnvelope(String commandId, String sessionId, String playerId, int expectTurnNumber, PlayerAction action) {
        this.commandId = commandId;
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.expectTurnNumber = expectTurnNumber;
        this.action = action;
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
}

