package com.kotva.application.turn;

import java.io.Serializable;

/**
 * Tracks progress inside one round of turns.
 */
public class RoundTracker implements Serializable {
    private static final long serialVersionUID = 1L;

    private int activePlayerCountInRound;
    private int finishedTurnCount;
    private int passCount;

    /**
     * Creates an empty round tracker.
     */
    public RoundTracker() {
        this.activePlayerCountInRound = 0;
        this.finishedTurnCount = 0;
        this.passCount = 0;
    }

    /**
     * Starts a new round and resets all counters.
     *
     * @param activePlayerCount number of active players in this round
     */
    public void startNewRound(int activePlayerCount) {
        if (activePlayerCount < 0) {
            throw new IllegalArgumentException("activePlayerCount cannot be negative.");
        }
        this.activePlayerCountInRound = activePlayerCount;
        this.finishedTurnCount = 0;
        this.passCount = 0;
    }

    /**
     * Records that one player has finished a turn.
     *
     * @param passedTurn whether the player passed
     */
    public void recordTurn(boolean passedTurn) {
        if (activePlayerCountInRound <= 0) {
            throw new IllegalStateException("Cannot record turn without active players in round.");
        }
        this.finishedTurnCount++;
        if (passedTurn) {
            this.passCount++;
        }
    }

    /**
     * Checks whether every active player has played in this round.
     *
     * @return {@code true} if the round is complete
     */
    public boolean isRoundComplete() {
        return activePlayerCountInRound > 0 && finishedTurnCount >= activePlayerCountInRound;
    }

    /**
     * Checks whether every active player passed in this round.
     *
     * @return {@code true} if all players passed
     */
    public boolean isAllPassedInRound() {
        return activePlayerCountInRound > 0 && passCount >= activePlayerCountInRound;
    }

    /**
     * Gets the number of finished turns in the current round.
     *
     * @return finished turn count
     */
    public int getFinishedTurnCount() {
        return finishedTurnCount;
    }

    /**
     * Gets the number of passes in the current round.
     *
     * @return pass count
     */
    public int getPassCount() {
        return passCount;
    }

    /**
     * Gets the active player count for the current round.
     *
     * @return active player count
     */
    public int getActivePlayerCountInRound() {
        return activePlayerCountInRound;
    }
}
