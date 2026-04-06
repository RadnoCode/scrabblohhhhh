package com.kotva.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.result.BoardSnapshot;
import com.kotva.application.result.SettlementResult;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.PlayerConfig;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
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

public class GameApplicationServiceImplSubmitDraftTest {
    @Test
    public void emptyDraftReturnsFailureWithoutChangingBoardOrTurn() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        TurnDraft originalDraft = session.getTurnDraft();

        SubmitDraftResult result = service.submitDraft(session);

        assertFalse(result.isSuccess());
        assertEquals("No tiles placed", result.getMessage());
        assertEquals(0, result.getAwardedScore());
        assertEquals("p1", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertNull(result.getSettlementResult());
        assertSame(originalDraft, session.getTurnDraft());
        assertTrue(session.getGameState().getBoard().getCell(new Position(7, 7)).isEmpty());
        assertEquals("p1", session.getGameState().requireCurrentActivePlayer().getPlayerId());
        assertEquals(0, settlementService.callCount);
    }

    @Test
    public void gappedDraftReturnsFailureWithoutChangingBoardOrTurn() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        TileBag tileBag = session.getGameState().getTileBag();
        Tile tileA = drawTileWithLetter(tileBag, 'A');
        Tile tileT = drawTileWithLetter(tileBag, 'T');
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, tileT);
        TurnDraft originalDraft = session.getTurnDraft();

        originalDraft
                .getPlacements()
                .add(new DraftPlacement(tileA.getTileID(), new Position(7, 7)));
        originalDraft
                .getPlacements()
                .add(new DraftPlacement(tileT.getTileID(), new Position(7, 9)));

        SubmitDraftResult result = service.submitDraft(session);

        assertFalse(result.isSuccess());
        assertEquals("Letters shall be contiguous", result.getMessage());
        assertEquals(0, result.getAwardedScore());
        assertEquals("p1", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertNull(result.getSettlementResult());
        assertSame(originalDraft, session.getTurnDraft());
        assertTrue(session.getGameState().getBoard().getCell(new Position(7, 7)).isEmpty());
        assertTrue(session.getGameState().getBoard().getCell(new Position(7, 8)).isEmpty());
        assertTrue(session.getGameState().getBoard().getCell(new Position(7, 9)).isEmpty());
        assertEquals("p1", session.getGameState().requireCurrentActivePlayer().getPlayerId());
        assertEquals(0, settlementService.callCount);
    }

    @Test
    public void validDraftUpdatesBoardScoreRackAndNextPlayer() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        TileBag tileBag = session.getGameState().getTileBag();
        Tile tileA = drawTileWithLetter(tileBag, 'A');
        Tile tileT = drawTileWithLetter(tileBag, 'T');
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, tileT);

        TurnDraft originalDraft = session.getTurnDraft();
        originalDraft
                .getPlacements()
                .add(new DraftPlacement(tileA.getTileID(), new Position(7, 7)));
        originalDraft
                .getPlacements()
                .add(new DraftPlacement(tileT.getTileID(), new Position(7, 8)));

        SubmitDraftResult result = service.submitDraft(session);

        assertTrue(result.isSuccess());
        assertEquals("Draft submitted.", result.getMessage());
        assertEquals(4, result.getAwardedScore());
        assertEquals("p2", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertNull(result.getSettlementResult());
        assertEquals(4, currentPlayer.getScore());
        assertEquals(tileA.getTileID(), session.getGameState().getBoard().getCell(new Position(7, 7)).getPlacedTile().getTileID());
        assertEquals(tileT.getTileID(), session.getGameState().getBoard().getCell(new Position(7, 8)).getPlacedTile().getTileID());
        assertEquals(7, countRackTiles(currentPlayer));
        assertNotSame(originalDraft, session.getTurnDraft());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());
        assertEquals("p2", session.getGameState().requireCurrentActivePlayer().getPlayerId());
        assertEquals(SessionStatus.IN_PROGRESS, session.getSessionStatus());
        assertEquals(0, settlementService.callCount);
    }

    @Test
    public void validDraftEndsGameWhenBagIsEmptyAndRackIsCleared() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        TileBag tileBag = session.getGameState().getTileBag();
        Tile tileA = drawTileWithLetter(tileBag, 'A');
        Tile tileT = drawTileWithLetter(tileBag, 'T');
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, tileT);
        while (!tileBag.isEmpty()) {
            tileBag.drawTile();
        }

        session.getTurnDraft()
                .getPlacements()
                .add(new DraftPlacement(tileA.getTileID(), new Position(7, 7)));
        session.getTurnDraft()
                .getPlacements()
                .add(new DraftPlacement(tileT.getTileID(), new Position(7, 8)));

        SubmitDraftResult result = service.submitDraft(session);

        assertTrue(result.isSuccess());
        assertTrue(result.isGameEnded());
        assertNull(result.getNextPlayerId());
        assertNotNull(result.getSettlementResult());
        assertEquals(
                GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED,
                result.getSettlementResult().getEndReason());
        assertEquals(SessionStatus.COMPLETED, session.getSessionStatus());
        assertTrue(session.getGameState().isGameOver());
        assertEquals(
                GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED,
                session.getGameState().getGameEndReason());
        assertEquals(1, settlementService.callCount);
    }

    @Test
    public void draftEditingUpdatesOnlyDraftAndPreview() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Tile tile = drawTileWithLetter(session.getGameState().getTileBag(), 'A');

        PreviewResult placed = service.placeDraftTile(session, tile.getTileID(), new Position(7, 7));
        assertTrue(placed.isValid());
        assertEquals(0, placed.getEstimatedScore());
        assertEquals(1, session.getTurnDraft().getPlacements().size());
        assertNotNull(session.getTurnDraft().getPreviewResult());

        PreviewResult moved = service.moveDraftTile(session, tile.getTileID(), new Position(0, 0));
        assertFalse(moved.isValid());
        assertEquals("First word shall sit on the center", moved.getMessages().get(0));
        assertEquals(1, session.getTurnDraft().getPlacements().size());

        PreviewResult removed = service.removeDraftTile(session, tile.getTileID());
        assertFalse(removed.isValid());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());

        session.getTurnDraft().setDraggingTileId("tile-1");
        PreviewResult recalled = service.recallAllDraftTiles(session);
        assertFalse(recalled.isValid());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());
        assertNull(session.getTurnDraft().getDraggingTileId());
        assertTrue(session.getGameState().getBoard().getCell(new Position(7, 7)).isEmpty());
        assertTrue(session.getGameState().getBoard().getCell(new Position(0, 0)).isEmpty());
        assertEquals("p1", session.getGameState().requireCurrentActivePlayer().getPlayerId());
        assertEquals(0, session.getTurnCoordinator().getTurnNumber());
        assertEquals(0, settlementService.callCount);
    }

    @Test
    public void passTurnClearsDraftAndMovesToNextPlayer() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Tile tile = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        session.getTurnDraft()
                .getPlacements()
                .add(new DraftPlacement(tile.getTileID(), new Position(7, 7)));

        TurnTransitionResult result = service.passTurn(session);

        assertTrue(result.isSuccess());
        assertEquals("Turn passed.", result.getMessage());
        assertEquals("p2", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertNull(result.getSettlementResult());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());
        assertTrue(session.getGameState().getBoard().getCell(new Position(7, 7)).isEmpty());
        assertEquals("p2", session.getGameState().requireCurrentActivePlayer().getPlayerId());
        assertEquals(1, session.getTurnCoordinator().getTurnNumber());
        assertEquals(SessionStatus.IN_PROGRESS, session.getSessionStatus());
        assertEquals(0, settlementService.callCount);
    }

    private GameSession createInProgressSession(SettlementService settlementService) {
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
        GameSession session = new GameSession("session-1", config, gameState, settlementService);
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

    private int countRackTiles(Player player) {
        int count = 0;
        for (RackSlot slot : player.getRack().getSlots()) {
            if (!slot.isEmpty()) {
                count++;
            }
        }
        return count;
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

    private static class RecordingSettlementService implements SettlementService {
        private int callCount;

        @Override
        public SettlementResult settle(GameState gameState, GameEndReason endReason) {
            callCount++;
            return new SettlementResult(endReason, List.of(), List.of(), new BoardSnapshot(List.of()));
        }
    }
}
