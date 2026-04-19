package com.kotva.application.result;

import com.kotva.domain.endgame.GameEndReason;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class SettlementResult implements Serializable {
    private final GameEndReason endReason;
    private final List<PlayerSettlement> rankings;
    private final List<String> summaryMessages;
    private final BoardSnapshot boardSnapshot;

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

    public GameEndReason getEndReason() {
        return endReason;
    }

    public List<PlayerSettlement> getRankings() {
        return rankings;
    }

    public List<String> getSummaryMessages() {
        return summaryMessages;
    }

    public BoardSnapshot getBoardSnapshot() {
        return boardSnapshot;
    }
}
