package com.kotva.application.service.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.session.PlayerConfig;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.mode.GameMode;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ClientDraftServiceTest {
    @Test
    public void clientDraftPreviewUsesLocalDraftWithoutMutatingSessionDraft() {
        GameSession session = createInProgressSession();
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        Tile tileT = drawTileWithLetter(session.getGameState().getTileBag(), 'T');
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, tileT);

        GameSessionSnapshot hostSnapshot = GameSessionSnapshotFactory.fromSession(session);
        ClientGameContext context =
                new ClientGameContext(session.getConfig(), hostSnapshot, "p1");
        ClientDraftService draftService =
                new ClientDraftService(
                        context,
                        new ClientPreviewService(context, new StubDictionaryRepository()));

        draftService.placeDraftTile(tileA.getTileID(), new Position(7, 7));
        PreviewResult preview =
                draftService.placeDraftTile(tileT.getTileID(), new Position(7, 8));
        GameSessionSnapshot uiSnapshot = draftService.getUiSnapshot();

        assertTrue(preview.isValid());
        assertEquals(4, preview.getEstimatedScore());
        assertEquals(2, draftService.getPlacements().size());
        assertNotNull(draftService.getPreviewResult());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());
        assertNull(session.getTurnDraft().getPreviewResult());

        assertTrue(hostSnapshot.getDraftPlacements().isEmpty());
        assertNull(hostSnapshot.getPreview());
        assertEquals(2, uiSnapshot.getDraftPlacements().size());
        assertNotNull(uiSnapshot.getPreview());
        assertEquals(4, uiSnapshot.getPreview().getEstimatedScore());
        assertEquals("AT", uiSnapshot.getPreview().getWords().get(0).getWord());
    }

    @Test
    public void remoteClientSubmitCommandIsBuiltFromContextAndLocalDraft() {
        GameSession session = createInProgressSession();
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        currentPlayer.getRack().setTileAt(0, tileA);

        GameSessionSnapshot hostSnapshot = GameSessionSnapshotFactory.fromSession(session);
        ClientGameContext context =
                new ClientGameContext(session.getConfig(), hostSnapshot, "p1");
        ClientDraftService draftService =
                new ClientDraftService(
                        context,
                        new ClientPreviewService(context, new StubDictionaryRepository()));
        draftService.placeDraftTile(tileA.getTileID(), new Position(7, 7));
        RemoteClientService remoteClientService = new RemoteClientService(context, draftService);

        CommandEnvelope command = remoteClientService.submitDraft();

        assertEquals("session-1", command.getSessionId());
        assertEquals("p1", command.getPlayerId());
        assertEquals(0, command.getExpectTurnNumber());
        assertEquals(ActionType.PLACE_TILE, command.getAction().type());
        assertEquals(1, command.getAction().placements().size());
        assertEquals(tileA.getTileID(), command.getAction().placements().get(0).tileId());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());
    }

    @Test
    public void clientPreviewUsesBoardSnapshotForExistingTiles() {
        GameSession session = createInProgressSession();
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile boardTile = new Tile("board-q", 'Q', 10, false);
        Tile rackTile = new Tile("rack-i", 'I', 1, false);
        session.getGameState().getBoard().getCell(new Position(7, 7)).setPlacedTile(boardTile);
        currentPlayer.getRack().setTileAt(0, rackTile);

        GameSessionSnapshot hostSnapshot = GameSessionSnapshotFactory.fromSession(session);
        ClientGameContext context =
                new ClientGameContext(session.getConfig(), hostSnapshot, "p1");
        ClientDraftService draftService =
                new ClientDraftService(
                        context,
                        new ClientPreviewService(context, new StubDictionaryRepository()));

        PreviewResult preview = draftService.placeDraftTile(rackTile.getTileID(), new Position(7, 8));

        assertTrue(preview.isValid());
        assertEquals(11, preview.getEstimatedScore());
        assertEquals("QI", preview.getWordList().get(0).getWord());
        assertEquals(
                10,
                hostSnapshot.getBoardSnapshot().getCells().stream()
                        .filter(cell -> cell.getRow() == 7 && cell.getCol() == 7)
                        .findFirst()
                        .orElseThrow()
                        .getScore());
    }

    private GameSession createInProgressSession() {
        Player first = new Player("p1", "Alice", PlayerType.LAN);
        Player second = new Player("p2", "Bob", PlayerType.LAN);
        GameState gameState = new GameState(List.of(first, second));
        GameConfig config =
                new GameConfig(
                        GameMode.LAN_MULTIPLAYER,
                        List.of(
                                new PlayerConfig("Alice", PlayerType.LAN),
                                new PlayerConfig("Bob", PlayerType.LAN)),
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
            return Set.of("A", "AT", "QI");
        }

        @Override
        public DictionaryType getLoadedDictionaryType() {
            return DictionaryType.AM;
        }

        @Override
        public boolean isAccepted(String word) {
            return "A".equalsIgnoreCase(word)
                    || "AT".equalsIgnoreCase(word)
                    || "QI".equalsIgnoreCase(word);
        }
    }
}
