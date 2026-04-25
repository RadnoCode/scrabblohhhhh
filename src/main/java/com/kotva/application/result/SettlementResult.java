package com.kotva.application.result;

import com.kotva.domain.endgame.GameEndReason;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Result shown when the game ends.
 */
public class SettlementResult implements Serializable {
    private final GameEndReason endReason;
    private final List<PlayerSettlement> rankings;
    private final List<String> summaryMessages;
    private final BoardSnapshot boardSnapshot;

    /**
     * Creates a settlement result.
     *
     * @param endReason reason why the game ended
     * @param rankings final player rankings
     * @param summaryMessages short messages for the settlement screen
     * @param boardSnapshot final board state
     */
    public SettlementResult(
        GameEndReason endReason,
        List<PlayerSettlement> rankings,
        List<String> summaryMessages,
        BoardSnapshot boardSnapshot) {
        this.endReason = Objects.requireNonNull(endReason, "endReason cannot be null.");
        this.rankings = List.copyOf(Objects.requireNonNull(rankings, "rankings cannot be null."));
        this.summaryMessages =
        List.copyOf(Objects.requireNonNull(summaryMessages, "summaryMessages cannot be null."));
        this.boardSnapshot =
        Objects.requireNonNull(boardSnapshot, "boardSnapshot cannot be null.");
    }

    /**
     * Gets the reason why the game ended.
     *
     * @return end reason
     */
    public GameEndReason getEndReason() {
        return endReason;
    }

    /**
     * Gets the final rankings.
     *
     * @return ranking list
     */
    public List<PlayerSettlement> getRankings() {
        return rankings;
    }

    /**
     * Gets settlement summary messages.
     *
     * @return summary messages
     */
    public List<String> getSummaryMessages() {
        return summaryMessages;
    }

    /**
     * Gets the final board snapshot.
     *
     * @return board snapshot
     */
    public BoardSnapshot getBoardSnapshot() {
        return boardSnapshot;
    }
}
