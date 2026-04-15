package com.kotva.domain.endgame;

import com.kotva.domain.action.ActionType;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import java.util.Objects;
import java.util.Optional;

public class EndGameChecker {

    public Optional<GameEndReason> evaluate(
        GameState gameState,
        Player actingPlayer,
        PlayerAction action,
        boolean roundComplete,
        boolean allPassedInRound) {
        Objects.requireNonNull(gameState, "gameState cannot be null.");
        Objects.requireNonNull(actingPlayer, "actingPlayer cannot be null.");
        Objects.requireNonNull(action, "action cannot be null.");

        if (action.type() == ActionType.LOSE && gameState.getActivePlayerCount() <= 1) {
            return Optional.of(GameEndReason.ONLY_ONE_PLAYER_REMAINING);
        }

        if (action.type() == ActionType.PLACE_TILE
            && gameState.getTileBag().isEmpty()
            && actingPlayer.getRack().isEmpty()) {
            return Optional.of(GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED);
        }

        if (roundComplete && allPassedInRound) {
            return Optional.of(GameEndReason.ALL_PLAYERS_PASSED);
        }

        return Optional.empty();
    }
}