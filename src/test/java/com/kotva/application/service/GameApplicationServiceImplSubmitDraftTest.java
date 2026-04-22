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
import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.PlayerAction;
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
import com.kotva.policy.WordType;
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

        GameActionResult result = service.submitDraft(session);

        assertFalse(result.isSuccess());
        assertEquals("No tiles placed", result.getMessage());
        assertEquals(0, result.getAwardedScore());
        assertEquals("p1", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertEquals("p1", result.getPlayerId());
        assertSame(originalDraft, session.getTurnDraft());
        assertSame(result, session.getLatestActionResult());
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

        GameActionResult result = service.submitDraft(session);

        assertFalse(result.isSuccess());
        assertEquals("Letters shall be contiguous", result.getMessage());
        assertEquals(0, result.getAwardedScore());
        assertEquals("p1", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertSame(originalDraft, session.getTurnDraft());
        assertSame(result, session.getLatestActionResult());
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

        GameActionResult result = service.submitDraft(session);

        assertTrue(result.isSuccess());
        assertEquals("Draft submitted.", result.getMessage());
        assertEquals(4, result.getAwardedScore());
        assertEquals("p2", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertEquals("p1", result.getPlayerId());
        assertNotNull(result.getActionId());
        assertEquals(4, currentPlayer.getScore());
        assertEquals(tileA.getTileID(), session.getGameState().getBoard().getCell(new Position(7, 7)).getPlacedTile().getTileID());
        assertEquals(tileT.getTileID(), session.getGameState().getBoard().getCell(new Position(7, 8)).getPlacedTile().getTileID());
        assertEquals(7, countRackTiles(currentPlayer));
        assertNotSame(originalDraft, session.getTurnDraft());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());
        assertSame(result, session.getLatestActionResult());
        assertEquals("p2", session.getGameState().requireCurrentActivePlayer().getPlayerId());
        assertEquals(SessionStatus.IN_PROGRESS, session.getSessionStatus());
        assertEquals(0, settlementService.callCount);
    }

        @Test
    public void submitDraftEchoesClientActionIdIntoActionResultAndSnapshot() {
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

        session.getTurnDraft()
            .getPlacements()
            .add(new DraftPlacement(tileA.getTileID(), new Position(7, 7)));
        session.getTurnDraft()
            .getPlacements()
            .add(new DraftPlacement(tileT.getTileID(), new Position(7, 8)));

        GameActionResult result = service.submitDraft(session, "ui-submit-1");

        assertEquals("ui-submit-1", result.getClientActionId());
        assertSame(result, session.getLatestActionResult());
        assertEquals("ui-submit-1", session.getLatestActionResult().getClientActionId());
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

        GameActionResult result = service.submitDraft(session);

        assertTrue(result.isSuccess());
        assertTrue(result.isGameEnded());
        assertNull(result.getNextPlayerId());
        assertSame(result, session.getLatestActionResult());
        assertNotNull(session.getTurnCoordinator().getSettlementResult());
        assertEquals(
            GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED,
            session.getTurnCoordinator().getSettlementResult().getEndReason());
        assertEquals(SessionStatus.COMPLETED, session.getSessionStatus());
        assertTrue(session.getGameState().isGameOver());
        assertEquals(
            GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED,
            session.getGameState().getGameEndReason());
        assertEquals(1, settlementService.callCount);
    }

        @Test
    public void submitDraftClearsRemainingRackBlankAssignmentsAndMarksPlacedBlankFixed() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
        new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        TileBag tileBag = session.getGameState().getTileBag();
        Tile tileA = drawTileWithLetter(tileBag, 'A');
        Tile placedBlank = drawBlankTile(tileBag);
        Tile remainingBlank = drawBlankTile(tileBag);
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, placedBlank);
        currentPlayer.getRack().setTileAt(2, remainingBlank);
        placedBlank.setAssignedLetter('T');
        remainingBlank.setAssignedLetter('Z');

        session.getTurnDraft()
            .getPlacements()
            .add(new DraftPlacement(tileA.getTileID(), new Position(7, 7)));
        session.getTurnDraft()
            .getPlacements()
            .add(new DraftPlacement(placedBlank.getTileID(), new Position(7, 8)));

        GameActionResult result = service.submitDraft(session);

        assertTrue(result.isSuccess());
        Tile committedBlank = session.getGameState().getBoard().getCell(new Position(7, 8)).getPlacedTile();
        assertEquals(placedBlank.getTileID(), committedBlank.getTileID());
        assertEquals(Character.valueOf('T'), committedBlank.getAssignedLetter());
        assertTrue(committedBlank.isFixed());
        assertNull(remainingBlank.getAssignedLetter());
    }

        @Test
    public void executeRemoteCommandUsesAssignedLettersForBlankPlacements() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
        new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository(Set.of("AT")));
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        TileBag tileBag = session.getGameState().getTileBag();
        Tile tileA = drawTileWithLetter(tileBag, 'A');
        Tile blank = drawBlankTile(tileBag);
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, blank);

        PlayerAction action = PlayerAction.place(
            currentPlayer.getPlayerId(),
            List.of(
                new ActionPlacement(tileA.getTileID(), new Position(7, 7)),
                new ActionPlacement(blank.getTileID(), new Position(7, 8), 't')));

        GameActionResult result = service.executeRemoteCommand(session, action, "remote-submit-1");

        assertTrue(result.isSuccess());
        Tile committedBlank = session.getGameState().getBoard().getCell(new Position(7, 8)).getPlacedTile();
        assertNotNull(committedBlank);
        assertEquals(Character.valueOf('T'), committedBlank.getAssignedLetter());
        assertTrue(committedBlank.isFixed());
    }

        @Test
    public void draftEditingUpdatesOnlyDraftAndPreview() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
        new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Tile tile = drawTileWithLetter(session.getGameState().getTileBag(), 'A');

        PreviewResult placed = service.placeDraftTile(session, tile.getTileID(), new Position(7, 7));
        assertFalse(placed.isValid());
        assertEquals(0, placed.getEstimatedScore());
        assertFalse(placed.getHighlights().isEmpty());
        assertEquals("At least one new word must be formed", placed.getMessages().get(0));
        assertEquals(1, session.getTurnDraft().getPlacements().size());
        assertNotNull(session.getTurnDraft().getPreviewResult());

        PreviewResult moved = service.moveDraftTile(session, tile.getTileID(), new Position(0, 0));
        assertFalse(moved.isValid());
        assertFalse(moved.getHighlights().isEmpty());
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
    public void draftPreviewBuildsMainWordFromContiguousDraftPlacements() {
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

        service.placeDraftTile(session, tileA.getTileID(), new Position(7, 7));
        PreviewResult preview = service.placeDraftTile(session, tileT.getTileID(), new Position(7, 8));

        assertTrue(preview.isValid());
        assertEquals(4, preview.getEstimatedScore());
        assertEquals(1, preview.getWordList().size());
        assertEquals("AT", preview.getWordList().get(0).getWord());
        assertEquals(WordType.MAIN_WORD, preview.getWordList().get(0).getWordType());
        assertEquals(2, preview.getWordList().get(0).getCoveredPositions().size());
        assertEquals(7, preview.getWordList().get(0).getCoveredPositions().get(0).getRow());
        assertEquals(7, preview.getWordList().get(0).getCoveredPositions().get(0).getCol());
        assertEquals(7, preview.getWordList().get(0).getCoveredPositions().get(1).getRow());
        assertEquals(8, preview.getWordList().get(0).getCoveredPositions().get(1).getCol());
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

        GameActionResult result = service.passTurn(session);

        assertTrue(result.isSuccess());
        assertEquals("Turn passed.", result.getMessage());
        assertEquals("p2", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertSame(result, session.getLatestActionResult());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());
        assertTrue(session.getGameState().getBoard().getCell(new Position(7, 7)).isEmpty());
        assertEquals("p2", session.getGameState().requireCurrentActivePlayer().getPlayerId());
        assertEquals(1, session.getTurnCoordinator().getTurnNumber());
        assertEquals(SessionStatus.IN_PROGRESS, session.getSessionStatus());
        assertEquals(0, settlementService.callCount);
    }

        @Test
    public void passTurnClearsAssignedLetterOnRackBlank() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
        new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile blank = drawBlankTile(session.getGameState().getTileBag());
        currentPlayer.getRack().setTileAt(0, blank);
        blank.setAssignedLetter('Q');

        GameActionResult result = service.passTurn(session);

        assertTrue(result.isSuccess());
        assertNull(blank.getAssignedLetter());
    }

        @Test
    public void confirmHotSeatHandoffDoesNotClearLatestActionResult() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
        new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());

        GameActionResult result = service.passTurn(session);

        assertSame(result, session.getLatestActionResult());

        service.confirmHotSeatHandoff(session);

        assertSame(result, session.getLatestActionResult());
    }

        @Test
    public void passTurnEchoesClientActionIdIntoActionResult() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
        new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());

        GameActionResult result = service.passTurn(session, "ui-pass-1");

        assertTrue(result.isSuccess());
        assertEquals("ui-pass-1", result.getClientActionId());
        assertSame(result, session.getLatestActionResult());
    }

        @Test
    public void resignDeactivatesPlayerAdvancesTurnAndEchoesClientActionIdWhenOthersRemain() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session =
        createInProgressSession(settlementService, "Alice", "Bob", "Cleo");
        GameApplicationServiceImpl service =
        new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());

        GameActionResult result = service.resign(session, "ui-resign-1");

        assertTrue(result.isSuccess());
        assertEquals("Player resigned.", result.getMessage());
        assertEquals("ui-resign-1", result.getClientActionId());
        assertEquals("p1", result.getPlayerId());
        assertEquals("p2", result.getNextPlayerId());
        assertFalse(result.isGameEnded());
        assertSame(result, session.getLatestActionResult());
        assertFalse(session.getGameState().getPlayerById("p1").getActive());
        assertEquals("p2", session.getGameState().requireCurrentActivePlayer().getPlayerId());
        assertEquals(SessionStatus.IN_PROGRESS, session.getSessionStatus());
        assertEquals(0, settlementService.callCount);
    }

        @Test
    public void resignEndsGameWhenOnlyOneActivePlayerRemains() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        GameSession session = createInProgressSession(settlementService);
        GameApplicationServiceImpl service =
        new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());

        GameActionResult result = service.resign(session);

        assertTrue(result.isSuccess());
        assertEquals("Player resigned.", result.getMessage());
        assertTrue(result.isGameEnded());
        assertNull(result.getNextPlayerId());
        assertFalse(session.getGameState().getPlayerById("p1").getActive());
        assertEquals(SessionStatus.COMPLETED, session.getSessionStatus());
        assertEquals(
            GameEndReason.ONLY_ONE_PLAYER_REMAINING,
            session.getGameState().getGameEndReason());
        assertEquals(1, settlementService.callCount);
    }

    private GameSession createInProgressSession(SettlementService settlementService) {
        return createInProgressSession(settlementService, "Alice", "Bob");
    }

    private GameSession createInProgressSession(
        SettlementService settlementService, String... playerNames) {
        List<Player> players = new java.util.ArrayList<>();
        List<PlayerConfig> playerConfigs = new java.util.ArrayList<>();
        for (int index = 0; index < playerNames.length; index++) {
            String playerId = "p" + (index + 1);
            String playerName = playerNames[index];
            players.add(new Player(playerId, playerName, PlayerType.LOCAL));
            playerConfigs.add(new PlayerConfig(playerName, PlayerType.LOCAL));
        }

        GameState gameState = new GameState(players);
        GameConfig config =
        new GameConfig(
            GameMode.HOT_SEAT,
            playerConfigs,
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

    private Tile drawBlankTile(TileBag tileBag) {
        while (!tileBag.isEmpty()) {
            Tile tile = tileBag.drawTile();
            if (tile.isBlank()) {
                return tile;
            }
        }
        throw new AssertionError("Expected blank tile to be available.");
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
        private final Set<String> acceptedWords;

        private StubDictionaryRepository() {
            this(Set.of("AT"));
        }

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

    private static class RecordingSettlementService implements SettlementService {
        private int callCount;

            @Override
        public SettlementResult settle(GameState gameState, GameEndReason endReason) {
            callCount++;
            return new SettlementResult(endReason, List.of(), List.of(), new BoardSnapshot(List.of()));
        }
    }
}
