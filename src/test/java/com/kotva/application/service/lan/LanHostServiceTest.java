package com.kotva.application.service.lan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.PlayerConfig;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.RemoteCommandResult;
import com.kotva.mode.GameMode;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class LanHostServiceTest {
    @Test
    public void duplicateCommandReturnsCachedResultWithoutReapplyingTurn() {
        GameSession session = createLanSession();
        LanHostService hostService =
                new LanHostService(
                        session,
                        new GameApplicationServiceImpl(
                                new ClockServiceImpl(),
                                new StubDictionaryRepository(Set.of("AT"))));

        CommandEnvelope commandEnvelope =
                new CommandEnvelope(
                        "cmd-1",
                        session.getSessionId(),
                        "p1",
                        session.getTurnCoordinator().getTurnNumber(),
                        PlayerAction.pass("p1"));

        RemoteCommandResult firstResult = hostService.handle(commandEnvelope);
        RemoteCommandResult duplicateResult = hostService.handle(commandEnvelope);

        assertTrue(firstResult.success());
        assertTrue(duplicateResult.success());
        assertEquals(firstResult.snapshot().getTurnNumber(), duplicateResult.snapshot().getTurnNumber());
        assertEquals(1, session.getTurnCoordinator().getTurnNumber());
        assertEquals("p2", session.getGameState().getCurrentPlayer().getPlayerId());
    }

    @Test
    public void staleTurnCommandReturnsFailureSnapshot() {
        GameSession session = createLanSession();
        LanHostService hostService =
                new LanHostService(
                        session,
                        new GameApplicationServiceImpl(
                                new ClockServiceImpl(),
                                new StubDictionaryRepository(Set.of("AT"))));

        RemoteCommandResult result =
                hostService.handle(
                        new CommandEnvelope(
                                "cmd-stale",
                                session.getSessionId(),
                                "p1",
                                99,
                                PlayerAction.pass("p1")));

        assertFalse(result.success());
        assertEquals(0, session.getTurnCoordinator().getTurnNumber());
        assertEquals(0, result.snapshot().getTurnNumber());
        assertTrue(result.message().contains("Turn number mismatch"));
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
        session.setSessionStatus(SessionStatus.IN_PROGRESS);
        return session;
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
