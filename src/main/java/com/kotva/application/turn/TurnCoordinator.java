package com.kotva.application.turn;

import com.kotva.application.result.SettlementResult;
import com.kotva.application.service.SettlementService;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.endgame.EndGameChecker;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import java.util.Objects;
import java.util.Optional;

public class TurnCoordinator {
    private final GameState gameState;
    private final SettlementService settlementService;
    private final RoundTracker roundTracker;
    private final EndGameChecker endGameChecker;
    private int turnNumber;
    private boolean gameEnded;
    private SettlementResult settlementResult;

    public TurnCoordinator(GameState gameState, SettlementService settlementService) {
        this.gameState = Objects.requireNonNull(gameState, "gameState cannot be null.");
        this.settlementService =
        Objects.requireNonNull(settlementService, "settlementService cannot be null.");
        this.roundTracker = new RoundTracker();
        this.endGameChecker = new EndGameChecker();
        this.turnNumber = 0;

        int activePlayerCount = gameState.getActivePlayerCount();
        this.roundTracker.startNewRound(activePlayerCount);
        this.gameEnded = activePlayerCount <= 0 || gameState.isGameOver();
    }

    public SettlementResult onActionApplied(PlayerAction action) {
        Objects.requireNonNull(action, "action cannot be null.");
        ensureGameNotEnded();

        Player actingPlayer = gameState.getCurrentPlayer();
        turnNumber++;
        roundTracker.recordTurn(action.type() == ActionType.PASS_TURN);

        boolean roundComplete = roundTracker.isRoundComplete();
        boolean allPassedInRound = roundTracker.isAllPassedInRound();
        Optional<GameEndReason> endReason =
        endGameChecker.evaluate(
            gameState, actingPlayer, action, roundComplete, allPassedInRound);
        if (endReason.isPresent()) {
            endGame(endReason.get());
            return settlementResult;
        }

        if (roundComplete) {
            roundTracker.startNewRound(gameState.getActivePlayerCount());
        }
        if (!gameEnded) {
            gameState.advanceToNextActivePlayer();
        }
        return settlementResult;
    }

    public Player getNextPlayer() {
        return gameState.requireCurrentActivePlayer();
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public SettlementResult getSettlementResult() {
        return settlementResult;
    }

    private void ensureGameNotEnded() {
        if (gameEnded) {
            throw new IllegalStateException("Game already ended.");
        }
    }

    private void endGame(GameEndReason reason) {
        if (gameEnded) {
            return;
        }

        gameEnded = true;
        gameState.markGameOver(reason);
        settlementResult = settlementService.settle(gameState, reason);
    }
}
