package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.GameState;
import java.io.Serializable;

public interface SettlementService extends Serializable {

    SettlementResult settle(GameState gameState, GameEndReason endReason);
}
