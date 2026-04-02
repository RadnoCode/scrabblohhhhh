package com.kotva.application.service;

import com.kotva.application.result.GameEndReason;
import com.kotva.application.result.SettlementResult;
import com.kotva.application.session.GameSession;

public interface SettlementService {
    SettlementResult settle(GameSession session, GameEndReason endReason);
}
