package com.kotva.application.service;

import com.kotva.domain.action.ActionType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Public result returned after a game action is executed.
 */
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

    /**
     * Creates a game action result.
     *
     * @param actionId server-side action id
     * @param clientActionId client command id, or {@code null}
     * @param playerId acting player id
     * @param actionType action type
     * @param success whether the action succeeded
     * @param message result message
     * @param awardedScore score awarded by the action
     * @param nextPlayerId next player id
     * @param gameEnded whether the game ended
     */
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

    /**
     * Gets the server-side action id.
     *
     * @return action id
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Gets the client command id.
     *
     * @return client action id, or {@code null}
     */
    public String getClientActionId() {
        return clientActionId;
    }

    /**
     * Gets the acting player id.
     *
     * @return player id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets the action type.
     *
     * @return action type
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Checks whether the action succeeded.
     *
     * @return {@code true} if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the result message.
     *
     * @return result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets score awarded by the action.
     *
     * @return awarded score
     */
    public int getAwardedScore() {
        return awardedScore;
    }

    /**
     * Gets the next player id.
     *
     * @return next player id
     */
    public String getNextPlayerId() {
        return nextPlayerId;
    }

    /**
     * Checks whether the game ended.
     *
     * @return {@code true} if ended
     */
    public boolean isGameEnded() {
        return gameEnded;
    }
}
