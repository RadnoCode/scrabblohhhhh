package com.kotva.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.result.BoardSnapshot;
import com.kotva.application.result.SettlementResult;
import com.kotva.application.service.SettlementService;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.policy.PlayerType;
import java.util.List;
import org.junit.Test;

public class TurnCoordinatorTest {
    @Test
    public void loseDoesNotEndGameWhenMultipleActivePlayersRemain() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        Player third = createPlayer("p3", "Cleo");
        GameState gameState = new GameState(List.of(first, second, third));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);
        first.setActive(false);

        coordinator.onActionApplied(PlayerAction.lose("p1"));

        assertFalse(coordinator.isGameEnded());
        assertFalse(gameState.isGameOver());
        assertFalse(first.getActive());
        assertEquals("p2", gameState.requireCurrentActivePlayer().getPlayerId());
        assertEquals(1, coordinator.getTurnNumber());
        assertEquals(0, settlementService.callCount);
    }

    @Test
    public void loseEndsGameWhenOnlyOneActivePlayerRemains() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);
        first.setActive(false);

        SettlementResult settlementResult = coordinator.onActionApplied(PlayerAction.lose("p1"));

        assertTrue(coordinator.isGameEnded());
        assertTrue(gameState.isGameOver());
        assertEquals(GameEndReason.ONLY_ONE_PLAYER_REMAINING, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
        assertNotNull(settlementResult);
    }

    @Test
    public void allPlayersPassingEndsGameAtRoundBoundary() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);

        coordinator.onActionApplied(PlayerAction.pass("p1"));
        assertFalse(coordinator.isGameEnded());
        assertEquals("p2", gameState.requireCurrentActivePlayer().getPlayerId());

        SettlementResult settlementResult = coordinator.onActionApplied(PlayerAction.pass("p2"));

        assertTrue(coordinator.isGameEnded());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
        assertNotNull(settlementResult);
    }

    @Test
    public void emptyTileBagAndEmptyRackEndsGameAfterPlacedAction() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);

        while (!gameState.getTileBag().isEmpty()) {
            gameState.getTileBag().drawTile();
        }

        SettlementResult settlementResult = coordinator.onActionApplied(PlayerAction.place("p1", List.of()));

        assertTrue(coordinator.isGameEnded());
        assertEquals(
                GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
        assertNotNull(settlementResult);
    }

    private Player createPlayer(String playerId, String playerName) {
        return new Player(playerId, playerName, PlayerType.LOCAL);
    }

    private static class RecordingSettlementService implements SettlementService {
        private int callCount;

        @Override
        public SettlementResult settle(GameState gameState, GameEndReason endReason) {
            callCount++;
            return new SettlementResult(endReason, List.of(), List.of(), new BoardSnapshot(List.of()));
        }
    }
}
