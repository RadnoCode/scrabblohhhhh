package com.kotva.application.turn;

import com.kotva.application.result.SettlementResult;
import com.kotva.application.service.SettlementService;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.endgame.EndGameChecker;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordinates turn changes and end-game checks after an action is applied.
 */
public class TurnCoordinator implements Serializable {
    private static final long serialVersionUID = 1L;

    private final GameState gameState;
    private final SettlementService settlementService;
    private final RoundTracker roundTracker;
    private final EndGameChecker endGameChecker;
    private int turnNumber;
    private boolean gameEnded;
    private SettlementResult settlementResult;

    /**
     * Creates a coordinator for one running game.
     *
     * @param gameState game state to update
     * @param settlementService service used when the game ends
     */
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

    /**
     * Handles turn transition after a player action has been executed.
     *
     * @param action action that was just applied
     * @return settlement result if the game ended, otherwise {@code null}
     */
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

    /**
     * Gets the player who should act next.
     *
     * @return next active player
     */
    public Player getNextPlayer() {
        return gameState.requireCurrentActivePlayer();
    }

    /**
     * Gets the number of actions already applied.
     *
     * @return turn number
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * Checks whether the game has ended.
     *
     * @return {@code true} if the game ended
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * Gets the settlement result after the game ends.
     *
     * @return settlement result, or {@code null} while the game is running
     */
    public SettlementResult getSettlementResult() {
        return settlementResult;
    }

    /**
     * Ensures no extra action is processed after the game has ended.
     */
    private void ensureGameNotEnded() {
        if (gameEnded) {
            throw new IllegalStateException("Game already ended.");
        }
    }

    /**
     * Marks the game as ended and builds the settlement result.
     *
     * @param reason reason why the game ended
     */
    private void endGame(GameEndReason reason) {
        if (gameEnded) {
            return;
        }

        gameEnded = true;
        gameState.markGameOver(reason);
        settlementResult = settlementService.settle(gameState, reason);
    }
}
