package com.kotva.application.service;

import com.kotva.application.result.GameEndReason;
import com.kotva.application.result.SettlementResult;
import com.kotva.domain.model.GameState;

public interface SettlementService {
    SettlementResult settle(GameState gameState, GameEndReason endReason);
}