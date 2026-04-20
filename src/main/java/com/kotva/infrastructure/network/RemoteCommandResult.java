package com.kotva.infrastructure.network;

import com.kotva.application.result.SettlementResult;
import com.kotva.application.session.GameSessionSnapshot;
import java.io.Serializable;

public record RemoteCommandResult(
        String commandId,
        boolean success,
        String message,
        int awardedScore,
        String nextPlayerId,
        boolean gameEnded,
        SettlementResult settlementResult,
        GameSessionSnapshot snapshot) implements Serializable {
}
