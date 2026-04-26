package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;

/**
 * Describes the result of dispatching one game action.
 */
public final class ActionDispatchResult {
    private final boolean success;
    private final String message;
    private final int awardedScore;
    private final String nextPlayerId;
    private final boolean gameEnded;
    private final SettlementResult settlementResult;

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
     * Creates a failed action result.
     *
     * @param message message shown to the caller
     * @param nextPlayerId player id that should act next
     * @return a failed dispatch result
     */
    public static ActionDispatchResult failure(String message, String nextPlayerId) {
        return new ActionDispatchResult(false, message, 0, nextPlayerId, false, null);
    }

    /**
     * Creates a successful action result.
     *
     * @param message message shown to the caller
     * @param awardedScore score earned by the action
     * @param nextPlayerId player id that should act next
     * @param gameEnded whether the action ended the game
     * @param settlementResult final settlement, if the game ended
     * @return a successful dispatch result
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
     * Returns whether the action was accepted.
     *
     * @return true when the action succeeded
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the result message.
     *
     * @return result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the score awarded by the action.
     *
     * @return awarded score
     */
    public int getAwardedScore() {
        return awardedScore;
    }

    /**
     * Returns the next player id.
     *
     * @return next player id
     */
    public String getNextPlayerId() {
        return nextPlayerId;
    }

    /**
     * Returns whether the game has ended.
     *
     * @return true when the game ended
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * Returns the settlement result.
     *
     * @return settlement result, or null when the game is not ended
     */
    public SettlementResult getSettlementResult() {
        return settlementResult;
    }
}
