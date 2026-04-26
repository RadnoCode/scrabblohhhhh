package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;

/**
 * Internal result used after dispatching a player action.
 */
public final class ActionDispatchResult {
    private final boolean success;
    private final String message;
    private final int awardedScore;
    private final String nextPlayerId;
    private final boolean gameEnded;
    private final SettlementResult settlementResult;

    /**
     * Creates an action dispatch result.
     *
     * @param success whether the action succeeded
     * @param message result message
     * @param awardedScore score awarded by the action
     * @param nextPlayerId next player id
     * @param gameEnded whether the game ended
     * @param settlementResult settlement result, or {@code null}
     */
    private ActionDispatchResult(
            boolean success,
            String message,
            int awardedScore,
            String nextPlayerId,
            boolean gameEnded,
            SettlementResult settlementResult) {
        this.success = success;
        this.message = message;
        this.awardedScore = awardedScore;
        this.nextPlayerId = nextPlayerId;
        this.gameEnded = gameEnded;
        this.settlementResult = settlementResult;
    }

    /**
     * Creates a failed dispatch result.
     *
     * @param message failure message
     * @param nextPlayerId current or next player id
     * @return failure result
     */
    public static ActionDispatchResult failure(String message, String nextPlayerId) {
        return new ActionDispatchResult(false, message, 0, nextPlayerId, false, null);
    }

    /**
     * Creates a successful dispatch result.
     *
     * @param message success message
     * @param awardedScore score awarded by the action
     * @param nextPlayerId next player id
     * @param gameEnded whether the game ended
     * @param settlementResult settlement result, or {@code null}
     * @return success result
     */
    public static ActionDispatchResult success(
            String message,
            int awardedScore,
            String nextPlayerId,
            boolean gameEnded,
            SettlementResult settlementResult) {
        return new ActionDispatchResult(
                true,
                message,
                awardedScore,
                nextPlayerId,
                gameEnded,
                settlementResult);
    }

    /**
     * Checks whether dispatch succeeded.
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

    /**
     * Gets the settlement result.
     *
     * @return settlement result, or {@code null}
     */
    public SettlementResult getSettlementResult() {
        return settlementResult;
    }
}
