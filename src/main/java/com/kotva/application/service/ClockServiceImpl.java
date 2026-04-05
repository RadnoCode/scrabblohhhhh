package com.kotva.application.service;

import com.kotva.application.result.GameEndReason;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.SessionStatus;
import java.util.Objects;

public class ClockServiceImpl implements ClockService {
    @Override
    public void startTurnClock(GameSession session) {
        PlayerClock clock = requireCurrentPlayer(session).getClock();
        if (!clock.isEnabled()) {
            return;
        }

        if (clock.getMainTimeRemainingMillis() > 0) {
            clock.setPhase(ClockPhase.MAIN_TIME);
            return;
        }

        clock.setPhase(ClockPhase.BYO_YOMI);
        clock.resetByoYomiTurn();
    }

    @Override
    public void stopTurnClock(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
    }

    @Override
    public void tick(GameSession session, long elapsedMillis) {
        if (elapsedMillis < 0) {
            throw new IllegalArgumentException("elapsedMillis cannot be negative.");
        }
        if (elapsedMillis == 0) {
            return;
        }

        Player currentPlayer = requireCurrentPlayer(session);
        PlayerClock clock = currentPlayer.getClock();
        if (!clock.isEnabled() || clock.getPhase() == ClockPhase.TIMEOUT) {
            return;
        }

        long remainingElapsed = elapsedMillis;
        if (clock.getPhase() == ClockPhase.MAIN_TIME) {
            long mainTimeRemaining = clock.getMainTimeRemainingMillis();
            long mainTimeConsumed = Math.min(mainTimeRemaining, remainingElapsed);
            clock.setMainTimeRemainingMillis(mainTimeRemaining - mainTimeConsumed);
            remainingElapsed -= mainTimeConsumed;
            if (clock.getMainTimeRemainingMillis() == 0) {
                clock.setPhase(ClockPhase.BYO_YOMI);
                clock.resetByoYomiTurn();
            } else {
                return;
            }
        }

        if (clock.getPhase() == ClockPhase.BYO_YOMI) {
            long byoYomiRemaining = clock.getByoYomiRemainingMillis() - remainingElapsed;
            clock.setByoYomiRemainingMillis(Math.max(0L, byoYomiRemaining));
            if (byoYomiRemaining <= 0L) {
                handleTimeout(session);
            }
        }
    }

    @Override
    public void handleTimeout(GameSession session) {
        Player currentPlayer = requireCurrentPlayer(session);
        PlayerClock clock = currentPlayer.getClock();
        if (!clock.isEnabled()) {
            return;
        }

        GameState gameState = session.getGameState();
        clock.setMainTimeRemainingMillis(0L);
        clock.setByoYomiRemainingMillis(0L);
        clock.setPhase(ClockPhase.TIMEOUT);
        currentPlayer.setActive(false);
        session.resetTurnDraft();// Avoid potential commit of stale draft after player is eliminated.

        if (gameState.getActivePlayerCount() <= 1) {
            gameState.markGameOver(GameEndReason.ONLY_ONE_PLAYER_REMAINING);
            session.getSettlementService()
                    .settle(gameState, GameEndReason.ONLY_ONE_PLAYER_REMAINING);
            session.setSessionStatus(SessionStatus.COMPLETED);
            return;
        }

        gameState.advanceToNextActivePlayer();
        startTurnClock(session);
    }

    private Player requireCurrentPlayer(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        return session.getGameState().requireCurrentActivePlayer();
    }
}

