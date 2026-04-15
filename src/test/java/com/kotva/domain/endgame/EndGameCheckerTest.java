package com.kotva.domain.endgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.policy.PlayerType;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class EndGameCheckerTest {
    private final EndGameChecker checker = new EndGameChecker();

    @Test
    public void placedActionEndsGameWhenBagIsEmptyAndRackIsEmpty() {
        Player first = new Player("p1", "Alice", PlayerType.LOCAL);
        Player second = new Player("p2", "Bob", PlayerType.LOCAL);
        GameState gameState = new GameState(List.of(first, second));

        while (!gameState.getTileBag().isEmpty()) {
            gameState.getTileBag().drawTile();
        }

        Optional<GameEndReason> result =
                checker.evaluate(gameState, first, PlayerAction.place("p1", List.of()), false, false);

        assertTrue(result.isPresent());
        assertEquals(GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED, result.get());
    }

    @Test
    public void loseEndsGameWhenOnlyOneActivePlayerRemains() {
        Player first = new Player("p1", "Alice", PlayerType.LOCAL);
        Player second = new Player("p2", "Bob", PlayerType.LOCAL);
        GameState gameState = new GameState(List.of(first, second));
        first.setActive(false);

        Optional<GameEndReason> result =
                checker.evaluate(gameState, first, PlayerAction.lose("p1"), false, false);

        assertTrue(result.isPresent());
        assertEquals(GameEndReason.ONLY_ONE_PLAYER_REMAINING, result.get());
    }

    @Test
    public void allPassedEndsGameAtRoundBoundary() {
        Player first = new Player("p1", "Alice", PlayerType.LOCAL);
        Player second = new Player("p2", "Bob", PlayerType.LOCAL);
        GameState gameState = new GameState(List.of(first, second));

        Optional<GameEndReason> result =
                checker.evaluate(gameState, first, PlayerAction.pass("p1"), true, true);

        assertTrue(result.isPresent());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, result.get());
    }

    @Test
    public void returnsEmptyWhenNoEndConditionMatches() {
        Player first = new Player("p1", "Alice", PlayerType.LOCAL);
        Player second = new Player("p2", "Bob", PlayerType.LOCAL);
        GameState gameState = new GameState(List.of(first, second));

        Optional<GameEndReason> result =
                checker.evaluate(gameState, first, PlayerAction.pass("p1"), false, false);

        assertFalse(result.isPresent());
    }
}
