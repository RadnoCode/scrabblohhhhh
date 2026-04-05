package com.kotva.application.coordinator;

import com.kotva.application.PlayerAction;
import com.kotva.application.RoundTracker;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import com.kotva.policy.ActionType;
import java.util.List;
import java.util.Objects;

/// Manages turn order, player actions, and round progression for a
/// game session.
public class TurnCoordinator {
    private final int playerNumber;
    private int activePlayerCount;
    private int turnNumber;
    private int currentPlayerIndex;
    private final List<Player> players;
    private final RoundTracker roundTracker;
    private boolean gameEnded;

    public TurnCoordinator(List<Player> players) {
        this.players = players;
        this.playerNumber = players.size();
        this.turnNumber = 0;
        this.currentPlayerIndex = 0;
        this.activePlayerCount = playerNumber;
        this.roundTracker = new RoundTracker();
        this.roundTracker.startNewRound(activePlayerCount);
        this.gameEnded = activePlayerCount <= 0;
    }

    public PlayerAction startTurn() {
        if (gameEnded) {
            throw new IllegalStateException("Game already ended.");
        }

        Player currentPlayer = getNextPlayer();
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
        finalizeRound();
        return action;
    }

    public Player getNextPlayer() {
        if (activePlayerCount <= 0) {
            throw new IllegalStateException("No active players left.");
        }

        for (int checked = 0; checked < playerNumber; checked++) {
            int index = currentPlayerIndex % playerNumber;
            currentPlayerIndex++;

            Player candidate = players.get(index);
            if (candidate.getActive()) {
                return candidate;
            }
        }

        throw new IllegalStateException("No active player found in player list.");
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public boolean isGameEnded() {
        return gameEnded;
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
                    activePlayerCount = Math.max(0, activePlayerCount - 1);
                }
            }
        }
    }

    private void finalizeRound() {
        if (!roundTracker.isRoundComplete()) {
            return;
        }

        boolean allPassedInRound = roundTracker.isAllPassedInRound();
        if (allPassedInRound || activePlayerCount <= 0) {
            gameEnded = true;
            return;
        }

        roundTracker.startNewRound(activePlayerCount);
    }

}
