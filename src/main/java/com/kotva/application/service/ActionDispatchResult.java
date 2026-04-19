package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;

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

    public static ActionDispatchResult failure(String message, String nextPlayerId) {
        return new ActionDispatchResult(false, message, 0, nextPlayerId, false, null);
    }

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

    public SettlementResult getSettlementResult() {
        return settlementResult;
    }
}
