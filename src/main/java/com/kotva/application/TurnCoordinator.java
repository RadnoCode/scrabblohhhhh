package com.kotva.application;

import com.kotva.application.result.GameEndReason;
import com.kotva.application.result.SettlementResult;
import com.kotva.application.service.SettlementService;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import com.kotva.policy.ActionType;
import java.util.Objects;

/// Manages turn order, player actions, and round progression for a
/// game session.
public class TurnCoordinator {
    private final GameState gameState;
    private final SettlementService settlementService;
    private int turnNumber;
    private final RoundTracker roundTracker;
    private boolean gameEnded;
    private SettlementResult settlementResult;

    public TurnCoordinator(GameState gameState, SettlementService settlementService) {
        this.gameState = Objects.requireNonNull(gameState, "gameState cannot be null.");
        this.settlementService =
                Objects.requireNonNull(settlementService, "settlementService cannot be null.");
        this.turnNumber = 0;
        this.roundTracker = new RoundTracker();
        int activePlayerCount = gameState.getActivePlayerCount();
        this.roundTracker.startNewRound(activePlayerCount);
        this.gameEnded = activePlayerCount <= 0 || gameState.isGameOver();
    }

    public PlayerAction startTurn() {
        if (gameEnded) {
            throw new IllegalStateException("Game already ended.");
        }

        Player currentPlayer = gameState.requireCurrentActivePlayer();
        PlayerController controller = currentPlayer.getController();
        if (controller == null) {
            throw new IllegalStateException(
                    "No PlayerController set for playerId=" + currentPlayer.getPlayerId());
        }

        turnNumber++;

        PlayerAction action = controller.requestAction();
        validateActionOwner(currentPlayer, action);
        applyAction(currentPlayer, action);
        roundTracker.recordTurn(action.type() == ActionType.PASS_TURN);
        evaluateImmediateGameEnd(currentPlayer, action);
        if (!gameEnded) {
            finalizeRound();
        }
        if (!gameEnded) {
            gameState.advanceToNextActivePlayer();
        }
        return action;
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

    private void validateActionOwner(Player currentPlayer, PlayerAction action) {
        if (action == null) {
            throw new IllegalArgumentException("PlayerAction cannot be null.");
        }

        if (!Objects.equals(currentPlayer.getPlayerId(), action.playerId())) {
            throw new IllegalArgumentException(
                    "Action playerId=" + action.playerId()
                            + " does not match current playerId=" + currentPlayer.getPlayerId());
        }
    }

    private void applyAction(Player currentPlayer, PlayerAction action) {
        switch (action.type()) {
            case PLACE_TILE -> {
                if (action.draft() == null) {
                    throw new IllegalArgumentException("PLACE_TILE action requires non-null draft.");
                }
                // TODO: submit the draft to Engine.
            }
            case PASS_TURN -> {
                // TODO: maybe there should be animate;
                // pass count is handled by RoundTracker.recordTurn at turn end.
            }
            case LOSE -> {
                if (currentPlayer.getActive()) {
                    currentPlayer.setActive(false);
                }
            }
        }
    }

    private void evaluateImmediateGameEnd(Player currentPlayer, PlayerAction action) {
        if (action.type() == ActionType.LOSE && gameState.getActivePlayerCount() <= 1) {
            endGame(GameEndReason.ONLY_ONE_PLAYER_REMAINING);
            return;
        }

        if (action.type() == ActionType.PLACE_TILE
                && gameState.getTileBag().isEmpty()
                && currentPlayer.getRack().isEmpty()) {
            endGame(GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED);
            return;
        }

        GameEndReason reservedReason = detectReservedGameEndReason();
        if (reservedReason != null) {
            endGame(reservedReason);
        }
    }

    private void finalizeRound() {
        if (!roundTracker.isRoundComplete()) {
            return;
        }

        boolean allPassedInRound = roundTracker.isAllPassedInRound();
        if (allPassedInRound) {
            endGame(GameEndReason.ALL_PLAYERS_PASSED);
            return;
        }

        GameEndReason reservedReason = detectReservedGameEndReason();
        if (reservedReason != null) {
            endGame(reservedReason);
            return;
        }

        roundTracker.startNewRound(gameState.getActivePlayerCount());
    }

    private GameEndReason detectReservedGameEndReason() {
        // TODO: detect when the board is full and no further tile placement is possible.
        // TODO: detect when no legal placement can be formed from the relevant players' racks.
        return null;
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
