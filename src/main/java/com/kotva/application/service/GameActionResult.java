package com.kotva.application.service;

import com.kotva.domain.action.ActionType;
import java.io.Serializable;
import java.util.Objects;

public final class GameActionResult implements Serializable {
    private final String actionId;
    private final String clientActionId;
    private final String playerId;
    private final ActionType actionType;
    private final boolean success;
    private final String message;
    private final int awardedScore;
    private final String nextPlayerId;
    private final boolean gameEnded;

    public GameActionResult(
        String actionId,
        String clientActionId,
        String playerId,
        ActionType actionType,
        boolean success,
        String message,
        int awardedScore,
        String nextPlayerId,
        boolean gameEnded) {
        this.actionId = Objects.requireNonNull(actionId, "actionId cannot be null.");
        this.clientActionId = clientActionId;
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
        this.actionType = Objects.requireNonNull(actionType, "actionType cannot be null.");
        this.success = success;
        this.message = Objects.requireNonNull(message, "message cannot be null.");
        this.awardedScore = awardedScore;
        this.nextPlayerId = nextPlayerId;
        this.gameEnded = gameEnded;
    }

    public String getActionId() {
        return actionId;
    }

    public String getClientActionId() {
        return clientActionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getAwardedScore() {
        return awardedScore;
    }

    public String getNextPlayerId() {
        return nextPlayerId;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }
}
