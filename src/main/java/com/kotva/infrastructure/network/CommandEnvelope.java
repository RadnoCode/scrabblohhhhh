package com.kotva.infrastructure.network;

import com.kotva.domain.action.PlayerAction;

public class CommandEnvelope {
    private String commandId;
    private String sessionId;
    private String playerId;
    private int turnNumber;
    private PlayerAction action;

    public CommandEnvelope(String commandId, String sessionId, String playerId, int turnNumber, PlayerAction action) {
        this.commandId = commandId;
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.turnNumber = turnNumber;
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

    public int getTurnNumber() {
        return turnNumber;
    }
}

