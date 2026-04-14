package com.kotva.application.service.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.kotva.application.draft.DraftManager;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.session.PlayerConfig;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ClientPreviewServiceTest {
    @Test
    public void clientPreviewMatchesAuthoritativePreviewForSameDraft() {
        GameSession session = createLanSession();
        GameApplicationServiceImpl authoritativeService = createService(Set.of("AT"));
        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        Tile tileT = drawTileWithLetter(session.getGameState().getTileBag(), 'T');
        currentPlayer.getRack().setTileAt(0, tileA);
        currentPlayer.getRack().setTileAt(1, tileT);

        GameSessionSnapshot baseSnapshot =
                GameSessionSnapshotFactory.fromSessionForViewer(session, currentPlayer.getPlayerId());
        ClientGameContext context =
                new ClientGameContext(session.getConfig(), baseSnapshot, currentPlayer.getPlayerId());
        ClientPreviewService clientPreviewService =
                new ClientPreviewService(context, new StubDictionaryRepository(Set.of("AT")));

        TurnDraft clientDraft = new TurnDraft();
        DraftManager draftManager = new DraftManager();
        draftManager.placeTile(clientDraft, tileA.getTileID(), new Position(7, 7));
        draftManager.placeTile(clientDraft, tileT.getTileID(), new Position(7, 8));
        PreviewResult clientPreview = clientPreviewService.refreshPreview(clientDraft);

        authoritativeService.placeDraftTile(session, tileA.getTileID(), new Position(7, 7));
        PreviewResult authoritativePreview =
                authoritativeService.placeDraftTile(session, tileT.getTileID(), new Position(7, 8));

        assertTrue(clientPreview.isValid());
        assertTrue(authoritativePreview.isValid());
        assertEquals(authoritativePreview.isValid(), clientPreview.isValid());
        assertEquals(authoritativePreview.getEstimatedScore(), clientPreview.getEstimatedScore());
        assertEquals(authoritativePreview.getWordList().size(), clientPreview.getWordList().size());
        assertEquals(
                authoritativePreview.getWordList().get(0).getWord(),
                clientPreview.getWordList().get(0).getWord());
        assertEquals(authoritativePreview.getMessages(), clientPreview.getMessages());
    }

    private GameSession createLanSession() {
        Player first = new Player("p1", "Host", PlayerType.LOCAL);
        Player second = new Player("p2", "Guest", PlayerType.LAN);
        GameState gameState = new GameState(List.of(first, second));
        GameConfig config =
                new GameConfig(
                        GameMode.LAN_MULTIPLAYER,
                        List.of(
                                new PlayerConfig("Host", PlayerType.LOCAL),
                                new PlayerConfig("Guest", PlayerType.LAN)),
                        DictionaryType.AM,
                        null);
        GameSession session = new GameSession("session-1", config, gameState);
        session.setSessionStatus(com.kotva.policy.SessionStatus.IN_PROGRESS);
        return session;
    }

    private GameApplicationServiceImpl createService(Set<String> acceptedWords) {
        return new GameApplicationServiceImpl(
                new ClockServiceImpl(),
                new StubDictionaryRepository(acceptedWords));
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

    private static final class StubDictionaryRepository extends DictionaryRepository {
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
