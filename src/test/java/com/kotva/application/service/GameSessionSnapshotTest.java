package com.kotva.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.BoardCellRenderSnapshot;
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
import com.kotva.policy.WordType;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class GameSessionSnapshotTest {

        @Test
    public void sessionSnapshotIncludesBoardPlayersRackDraftAndPreview() {
        GameSession session = createInProgressSession();
        GameApplicationServiceImpl service = createService(Set.of("AT"));
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        Tile tileT = drawTileWithLetter(session.getGameState().getTileBag(), 'T');
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, tileT);

        service.placeDraftTile(session, tileA.getTileID(), new Position(7, 7));
        PreviewResult preview = service.placeDraftTile(session, tileT.getTileID(), new Position(7, 8));
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
        assertEquals(2, snapshot.getBoardCells().size());
        assertEquals(7, snapshot.getCurrentRackTiles().size());
        assertEquals(2, snapshot.getDraftPlacements().size());
        assertNotNull(snapshot.getPreview());
        assertTrue(snapshot.getPreview().isValid());
        assertEquals(4, snapshot.getPreview().getEstimatedScore());
        assertEquals(1, snapshot.getPreview().getWords().size());
        assertEquals("AT", snapshot.getPreview().getWords().get(0).getWord());
        assertEquals(WordType.MAIN_WORD, snapshot.getPreview().getWords().get(0).getWordType());
        assertEquals(2, snapshot.getPreview().getWords().get(0).getCoveredPositions().size());
        assertEquals(4, snapshot.getPreview().getHighlights().size());
        assertTrue(
            snapshot.getPreview().getHighlights().stream()
            .allMatch(highlight -> highlight.getRow() == 7 && highlight.getCol() >= 7 && highlight.getCol() <= 8));
        assertNull(snapshot.getLatestActionResult());
        assertNull(snapshot.getSettlementResult());
        assertNull(snapshot.getAiRuntimeSnapshot());

        GamePlayerSnapshot firstPlayer =
        snapshot.getPlayers().stream()
            .filter(player -> "p1".equals(player.getPlayerId()))
            .findFirst()
            .orElseThrow();
        assertTrue(firstPlayer.isCurrentTurn());
        assertEquals(2, firstPlayer.getRackTileCount());
        assertEquals(tileA.getTileID(), snapshot.getCurrentRackTiles().get(0).getTileId());
        assertEquals(Character.valueOf('A'), snapshot.getCurrentRackTiles().get(0).getDisplayLetter());
        assertTrue(
            snapshot.getBoardSnapshot().getCells().stream()
            .filter(cell -> cell.getRow() == 7 && cell.getCol() == 7)
            .findFirst()
            .orElseThrow()
            .getLetter()
            == null);

        BoardCellRenderSnapshot firstDraftCell = findBoardCell(snapshot, 7, 7);
        assertEquals(tileA.getTileID(), firstDraftCell.getTileId());
        assertEquals(Character.valueOf('A'), firstDraftCell.getDisplayLetter());
        assertTrue(firstDraftCell.isDraft());
        assertTrue(firstDraftCell.isPreviewValid());
        assertFalse(firstDraftCell.isPreviewInvalid());
        assertTrue(firstDraftCell.isMainWordHighlighted());
        assertFalse(firstDraftCell.isCrossWordHighlighted());
    }

        @Test
    public void sessionSnapshotMarksMainAndCrossWordPositionsOnBoardCells() {
        GameSession session = createInProgressSession();
        GameApplicationServiceImpl service = createService(Set.of("AT"));
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();

        Tile committedA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        session.getGameState().getBoard().getCell(new Position(6, 8)).setPlacedTile(committedA);

        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        Tile tileT = drawTileWithLetter(session.getGameState().getTileBag(), 'T');
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, tileT);

        service.placeDraftTile(session, tileA.getTileID(), new Position(7, 7));
        PreviewResult preview = service.placeDraftTile(session, tileT.getTileID(), new Position(7, 8));
        assertTrue(preview.isValid());

        GameSessionSnapshot snapshot = service.getSessionSnapshot(session);

        assertEquals(2, snapshot.getPreview().getWords().size());
        assertTrue(snapshot.getPreview().getWords().stream().anyMatch(word -> word.getWordType() == WordType.MAIN_WORD));
        assertTrue(snapshot.getPreview().getWords().stream().anyMatch(word -> word.getWordType() == WordType.CROSS_WORD));

        BoardCellRenderSnapshot committedCell = findBoardCell(snapshot, 6, 8);
        assertFalse(committedCell.isDraft());
        assertFalse(committedCell.isPreviewValid());
        assertFalse(committedCell.isMainWordHighlighted());
        assertTrue(committedCell.isCrossWordHighlighted());

        BoardCellRenderSnapshot sharedDraftCell = findBoardCell(snapshot, 7, 8);
        assertTrue(sharedDraftCell.isDraft());
        assertTrue(sharedDraftCell.isPreviewValid());
        assertTrue(sharedDraftCell.isMainWordHighlighted());
        assertTrue(sharedDraftCell.isCrossWordHighlighted());
    }

        @Test
    public void sessionSnapshotMarksInvalidPreviewOnDraftBoardCells() {
        GameSession session = createInProgressSession();
        GameApplicationServiceImpl service = createService(Set.of("AT"));
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        Tile tileB = drawTileWithLetter(session.getGameState().getTileBag(), 'B');
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, tileB);

        service.placeDraftTile(session, tileA.getTileID(), new Position(7, 7));
        PreviewResult preview = service.placeDraftTile(session, tileB.getTileID(), new Position(7, 8));
        assertFalse(preview.isValid());

        GameSessionSnapshot snapshot = service.getSessionSnapshot(session);

        assertFalse(snapshot.getPreview().isValid());
        BoardCellRenderSnapshot firstDraftCell = findBoardCell(snapshot, 7, 7);
        BoardCellRenderSnapshot secondDraftCell = findBoardCell(snapshot, 7, 8);
        assertTrue(firstDraftCell.isPreviewInvalid());
        assertTrue(secondDraftCell.isPreviewInvalid());
        assertFalse(firstDraftCell.isPreviewValid());
        assertFalse(secondDraftCell.isPreviewValid());
    }

        @Test
    public void sessionSnapshotIncludesSettlementAfterGameEnds() {
        GameSession session = createInProgressSession();
        GameApplicationServiceImpl service = createService(Set.of("AT"));

        GameActionResult firstResult = service.passTurn(session, "ui-pass-1");
        GameActionResult secondResult = service.passTurn(session, "ui-pass-2");

        GameSessionSnapshot snapshot = service.getSessionSnapshot(session);

        assertNotNull(firstResult.getActionId());
        assertNotNull(secondResult.getActionId());
        assertFalse(firstResult.getActionId().equals(secondResult.getActionId()));
        assertTrue(snapshot.isGameEnded());
        assertEquals(SessionStatus.COMPLETED, snapshot.getSessionStatus());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, snapshot.getGameEndReason());
        assertNotNull(snapshot.getLatestActionResult());
        assertEquals(secondResult.getActionId(), snapshot.getLatestActionResult().getActionId());
        assertEquals("ui-pass-2", snapshot.getLatestActionResult().getClientActionId());
        assertNotNull(snapshot.getSettlementResult());
        assertNull(snapshot.getAiRuntimeSnapshot());
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

    private GameApplicationServiceImpl createService(Set<String> acceptedWords) {
        return new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository(acceptedWords));
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

    private BoardCellRenderSnapshot findBoardCell(GameSessionSnapshot snapshot, int row, int col) {
        return snapshot.getBoardCells().stream()
            .filter(cell -> cell.getRow() == row && cell.getCol() == col)
            .findFirst()
            .orElseThrow();
    }

    private static class StubDictionaryRepository extends DictionaryRepository {
        private final Set<String> acceptedWords;

        private StubDictionaryRepository(Set<String> acceptedWords) {
            this.acceptedWords = acceptedWords;
        }

            @Override
        public void loadDictionary(DictionaryType dictionaryType) {
        }

            @Override
        public Set<String> getDictionary() {
            return acceptedWords;
        }

            @Override
        public DictionaryType getLoadedDictionaryType() {
            return DictionaryType.AM;
        }

            @Override
        public boolean isAccepted(String word) {
            return acceptedWords.contains(word.toUpperCase());
        }
    }
}