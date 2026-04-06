package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;

public class TurnTransitionResult {
    private final boolean success;
    private final String message;
    private final String nextPlayerId;
    private final boolean gameEnded;
    private final SettlementResult settlementResult;

    public TurnTransitionResult(
            boolean success,
            String message,
            String nextPlayerId,
            boolean gameEnded,
            SettlementResult settlementResult) {
        this.success = success;
        this.message = message;
        this.nextPlayerId = nextPlayerId;
        this.gameEnded = gameEnded;
        this.settlementResult = settlementResult;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
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
