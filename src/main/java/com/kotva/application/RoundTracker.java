package com.kotva.application;

public class RoundTracker {
    private int activePlayerCountInRound;
    private int finishedTurnCount;
    private int passCount;

    public RoundTracker() {
        this.activePlayerCountInRound = 0;
        this.finishedTurnCount = 0;
        this.passCount = 0;
    }

    public void startNewRound(int activePlayerCount) {
        if (activePlayerCount < 0) {
            throw new IllegalArgumentException("activePlayerCount cannot be negative.");
        }
        this.activePlayerCountInRound = activePlayerCount;
        this.finishedTurnCount = 0;
        this.passCount = 0;
    }

    public void recordTurn(boolean passedTurn) {
        if (activePlayerCountInRound <= 0) {
            throw new IllegalStateException("Cannot record turn without active players in round.");
        }
        this.finishedTurnCount++;
        if (passedTurn) {
            this.passCount++;
        }
    }

    public boolean isRoundComplete() {
        return activePlayerCountInRound > 0 && finishedTurnCount >= activePlayerCountInRound;
    }

    public boolean isAllPassedInRound() {
        return activePlayerCountInRound > 0 && passCount >= activePlayerCountInRound;
    }

    public int getFinishedTurnCount() {
        return finishedTurnCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public int getActivePlayerCountInRound() {
        return activePlayerCountInRound;
    }
}