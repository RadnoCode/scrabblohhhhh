package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.GameState;
import java.io.Serializable;

/**
 * Calculates settlement information when a game ends.
 */
public interface SettlementService extends Serializable {

    /**
     * Settles a finished game.
     *
     * @param gameState final game state
     * @param endReason reason why the game ended
     * @return settlement result
     */
    SettlementResult settle(GameState gameState, GameEndReason endReason);
}
