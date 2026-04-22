package com.kotva.application.service.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.session.PlayerConfig;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ClientPreviewServiceTest {
    private static final String HOST_PLAYER_ID = "host";
    private static final String GUEST_PLAYER_ID = "guest";

    @Test
    public void refreshPreviewTreatsOccupiedBoardAsPostOpeningTurn() {
        GameConfig config = new GameConfig(
                GameMode.LAN_MULTIPLAYER,
                List.of(
                        new PlayerConfig("Host", PlayerType.LOCAL),
                        new PlayerConfig("Guest", PlayerType.LAN)),
                DictionaryType.AM,
                null);

        Player host = new Player(HOST_PLAYER_ID, "Host", PlayerType.LOCAL);
        Player guest = new Player(GUEST_PLAYER_ID, "Guest", PlayerType.LAN);
        GameState gameState = new GameState(List.of(host, guest));

        Tile centerTile = new Tile("board-a", 'A', 1, false);
        gameState.getTileBag().indexTile(centerTile);
        gameState.getBoard().getCell(new Position(7, 7)).setPlacedTile(centerTile);

        Tile guestTile = new Tile("rack-t", 'T', 1, false);
        gameState.getTileBag().indexTile(guestTile);
        guest.getRack().setTileAt(0, guestTile);
        gameState.advanceToNextActivePlayer();

        GameSession session = new GameSession("session-1", config, gameState);
        session.setSessionStatus(SessionStatus.IN_PROGRESS);

        GameSessionSnapshot snapshot =
                GameSessionSnapshotFactory.fromSessionForViewer(session, GUEST_PLAYER_ID);
        ClientGameContext context = new ClientGameContext(config, snapshot, GUEST_PLAYER_ID);
        ClientPreviewService previewService =
                new ClientPreviewService(context, new StubDictionaryRepository(Set.of("AT")));

        TurnDraft turnDraft = new TurnDraft();
        turnDraft.getPlacements().add(new DraftPlacement("rack-t", new Position(8, 7)));

        PreviewResult previewResult = previewService.refreshPreview(turnDraft);

        assertTrue(previewResult.isValid());
        assertEquals(2, previewResult.getEstimatedScore());
        assertEquals(List.of(), previewResult.getMessages());
        assertEquals(1, previewResult.getWordList().size());
        assertEquals("AT", previewResult.getWordList().get(0).getWord());
        assertFalse(previewResult.getMessages().contains("First word shall sit on the center"));
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
            return word != null && acceptedWords.contains(word.toUpperCase());
        }
    }
}
