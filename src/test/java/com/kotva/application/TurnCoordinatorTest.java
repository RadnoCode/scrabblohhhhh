package com.kotva.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.draft.TurnDraft;
import com.kotva.application.result.GameEndReason;
import com.kotva.application.result.SettlementResult;
import com.kotva.application.service.SettlementService;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Tile;
import com.kotva.mode.PlayerController;
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

        first.getController().onLose();
        coordinator.startTurn();

        assertFalse(coordinator.isGameEnded());
        assertFalse(gameState.isGameOver());
        assertEquals(0, settlementService.callCount);
    }

    @Test
    public void loseEndsGameWhenOnlyOneActivePlayerRemains() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);

        first.getController().onLose();
        coordinator.startTurn();

        assertTrue(coordinator.isGameEnded());
        assertTrue(gameState.isGameOver());
        assertEquals(GameEndReason.ONLY_ONE_PLAYER_REMAINING, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
    }

    @Test
    public void allPlayersPassingEndsGameAtRoundBoundary() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);

        first.getController().onPass();
        second.getController().onPass();

        coordinator.startTurn();
        assertFalse(coordinator.isGameEnded());

        coordinator.startTurn();
        assertTrue(coordinator.isGameEnded());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
    }

    @Test
    public void emptyTileBagAndEmptyRackEndsGameAfterPlaceTileAction() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);

        while (!gameState.getTileBag().isEmpty()) {
            gameState.getTileBag().drawTile();
        }

        first.getController().onSubmit(new TurnDraft());
        coordinator.startTurn();

        assertTrue(coordinator.isGameEnded());
        assertEquals(
                GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
        assertNotNull(coordinator.getSettlementResult());
    }

    private Player createPlayer(String playerId, String playerName) {
        Player player = new Player(playerId, playerName, PlayerType.LOCAL);
        player.setController(new PlayerController(playerId, PlayerType.LOCAL));
        return player;
    }

    private static class RecordingSettlementService implements SettlementService {
        private int callCount;

        @Override
        public SettlementResult settle(GameState gameState, GameEndReason endReason) {
            callCount++;
            return new SettlementResult(endReason, List.of(), List.of(), new com.kotva.application.result.BoardSnapshot(List.of()));
        }
    }
}
