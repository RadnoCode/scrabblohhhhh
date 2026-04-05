package com.kotva.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.kotva.application.result.BoardCellSnapshot;
import com.kotva.application.result.GameEndReason;
import com.kotva.application.result.PlayerSettlement;
import com.kotva.application.result.SettlementResult;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Tile;
import com.kotva.mode.PlayerController;
import com.kotva.policy.BonusType;
import com.kotva.policy.PlayerType;
import java.util.List;
import org.junit.Test;

public class SettlementServiceImplTest {
    @Test
    public void settlementIncludesRankingNamesReasonAndBoardSnapshot() {
        Player alice = createPlayer("p1", "Alice");
        Player bob = createPlayer("p2", "Bob");
        Player carol = createPlayer("p3", "Carol");
        alice.addScore(20);
        bob.addScore(20);
        carol.addScore(15);

        GameState gameState = new GameState(List.of(alice, bob, carol));
        gameState.getBoard()
                .getCell(new com.kotva.domain.model.Position(7, 7))
                .setPlacedTile(new Tile("tile-1", 'A', 1, false));

        RecordingNavigationPort navigationPort = new RecordingNavigationPort();
        SettlementServiceImpl settlementService = new SettlementServiceImpl(navigationPort);

        SettlementResult result =
                settlementService.settle(gameState, GameEndReason.ALL_PLAYERS_PASSED);

        assertSame(result, navigationPort.lastResult);
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, result.getEndReason());
        assertEquals(3, result.getRankings().size());
        assertEquals("Alice", result.getRankings().get(0).getPlayerName());
        assertEquals("Bob", result.getRankings().get(1).getPlayerName());
        assertEquals(1, result.getRankings().get(0).getRank());
        assertEquals(1, result.getRankings().get(1).getRank());
        assertEquals(3, result.getRankings().get(2).getRank());
        assertEquals(225, result.getBoardSnapshot().getCells().size());
        BoardCellSnapshot centerCell =
                result.getBoardSnapshot().getCells().stream()
                        .filter(cell -> cell.getRow() == 7 && cell.getCol() == 7)
                        .findFirst()
                        .orElseThrow();
        assertEquals(Character.valueOf('A'), centerCell.getLetter());
        assertEquals(BonusType.DOUBLE_WORD, centerCell.getBonusType());
        assertFalseOrMissingBlank(centerCell.isBlank());
        assertNotNull(result.getSummaryMessages());
        assertTrue(
                result.getSummaryMessages().stream()
                        .anyMatch(message -> message.contains("Shared first place")));
    }

    private Player createPlayer(String playerId, String playerName) {
        Player player = new Player(playerId, playerName, PlayerType.LOCAL);
        player.setController(new PlayerController(playerId, PlayerType.LOCAL));
        return player;
    }

    private void assertFalseOrMissingBlank(boolean blank) {
        assertTrue(!blank);
    }

    private static class RecordingNavigationPort implements SettlementNavigationPort {
        private SettlementResult lastResult;

        @Override
        public void showSettlement(SettlementResult result) {
            this.lastResult = result;
        }
    }
}
