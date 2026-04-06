package com.kotva.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GamePlayerSnapshot;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.PlayerConfig;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class GameSessionSnapshotTest {
    @Test
    public void sessionSnapshotIncludesBoardPlayersRackDraftAndPreview() {
        GameSession session = createInProgressSession();
        GameApplicationServiceImpl service =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        currentPlayer.getRack().setTileAt(0, tileA);

        PreviewResult preview = service.placeDraftTile(session, tileA.getTileID(), new Position(7, 7));
        assertTrue(preview.isValid());

        GameSessionSnapshot snapshot = service.getSessionSnapshot(session);

        assertEquals("session-1", snapshot.getSessionId());
        assertEquals(GameMode.HOT_SEAT, snapshot.getGameMode());
        assertEquals(SessionStatus.IN_PROGRESS, snapshot.getSessionStatus());
        assertFalse(snapshot.isGameEnded());
        assertNull(snapshot.getGameEndReason());
        assertEquals("p1", snapshot.getCurrentPlayerId());
        assertEquals("Alice", snapshot.getCurrentPlayerName());
        assertEquals(2, snapshot.getPlayers().size());
        assertEquals(225, snapshot.getBoardSnapshot().getCells().size());
        assertEquals(7, snapshot.getCurrentRackTiles().size());
        assertEquals(1, snapshot.getDraftPlacements().size());
        assertNotNull(snapshot.getPreview());
        assertTrue(snapshot.getPreview().isValid());
        assertEquals(0, snapshot.getPreview().getEstimatedScore());
        assertNull(snapshot.getSettlementResult());

        GamePlayerSnapshot firstPlayer =
                snapshot.getPlayers().stream()
                        .filter(player -> "p1".equals(player.getPlayerId()))
                        .findFirst()
                        .orElseThrow();
        assertTrue(firstPlayer.isCurrentTurn());
        assertEquals(1, firstPlayer.getRackTileCount());
        assertEquals(tileA.getTileID(), snapshot.getCurrentRackTiles().get(0).getTileId());
        assertTrue(
                snapshot.getBoardSnapshot().getCells().stream()
                        .filter(cell -> cell.getRow() == 7 && cell.getCol() == 7)
                        .findFirst()
                        .orElseThrow()
                        .getLetter()
                        == null);
    }

    @Test
    public void sessionSnapshotIncludesSettlementAfterGameEnds() {
        GameSession session = createInProgressSession();
        GameApplicationServiceImpl service =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());

        service.passTurn(session);
        service.passTurn(session);

        GameSessionSnapshot snapshot = service.getSessionSnapshot(session);

        assertTrue(snapshot.isGameEnded());
        assertEquals(SessionStatus.COMPLETED, snapshot.getSessionStatus());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, snapshot.getGameEndReason());
        assertNotNull(snapshot.getSettlementResult());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, snapshot.getSettlementResult().getEndReason());
    }

    private GameSession createInProgressSession() {
        Player first = new Player("p1", "Alice", PlayerType.LOCAL);
        Player second = new Player("p2", "Bob", PlayerType.LOCAL);
        GameState gameState = new GameState(List.of(first, second));
        GameConfig config =
                new GameConfig(
                        GameMode.HOT_SEAT,
                        List.of(
                                new PlayerConfig("Alice", PlayerType.LOCAL),
                                new PlayerConfig("Bob", PlayerType.LOCAL)),
                        DictionaryType.AM,
                        null);
        GameSession session = new GameSession("session-1", config, gameState);
        session.setSessionStatus(SessionStatus.IN_PROGRESS);
        return session;
    }

    private Tile drawTileWithLetter(TileBag tileBag, char letter) {
        while (!tileBag.isEmpty()) {
            Tile tile = tileBag.drawTile();
            if (tile.getLetter() == letter && !tile.isBlank()) {
                return tile;
            }
        }
        throw new AssertionError("Expected tile with letter " + letter + " to be available.");
    }

    private static class StubDictionaryRepository extends DictionaryRepository {
        @Override
        public void loadDictionary(DictionaryType dictionaryType) {
        }

        @Override
        public Set<String> getDictionary() {
            return Set.of("AT");
        }

        @Override
        public DictionaryType getLoadedDictionaryType() {
            return DictionaryType.AM;
        }

        @Override
        public boolean isAccepted(String word) {
            return "AT".equalsIgnoreCase(word);
        }
    }
}
