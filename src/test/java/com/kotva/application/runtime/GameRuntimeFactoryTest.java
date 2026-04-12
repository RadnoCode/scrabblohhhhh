package com.kotva.application.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.kotva.application.service.ClockService;
import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.GameSetupServiceImpl;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.SessionStatus;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class GameRuntimeFactoryTest {
    @Test
    public void hotSeatRuntimeRunsGameThroughRuntimeBoundary() {
        GameRuntimeFactory runtimeFactory = createRuntimeFactory();
        NewGameRequest request =
                new NewGameRequest(
                        GameMode.HOT_SEAT,
                        2,
                        List.of("Alice", "Bob"),
                        DictionaryType.AM,
                        null);

        GameRuntime runtime = runtimeFactory.create(request);
        assertTrue(runtime instanceof HotSeatGameRuntime);
        runtime.start(request);

        assertTrue(runtime.hasSession());
        assertNotNull(runtime.getSession());
        assertFalse(runtime.hasAutomatedTurnSupport());

        runtime.passTurn();
        assertEquals(SessionStatus.IN_PROGRESS, runtime.getSession().getSessionStatus());

        runtime.passTurn();
        assertEquals(SessionStatus.COMPLETED, runtime.getSession().getSessionStatus());
        assertEquals(
                GameEndReason.ALL_PLAYERS_PASSED,
                runtime.getSession().getGameState().getGameEndReason());
    }

    @Test
    public void aiModeCreatesDedicatedAiRuntime() {
        GameRuntimeFactory runtimeFactory = createRuntimeFactory();
        NewGameRequest request =
                new NewGameRequest(
                        GameMode.HUMAN_VS_AI,
                        2,
                        List.of("Player", "Easy Bot"),
                        DictionaryType.AM,
                        null,
                        com.kotva.policy.AiDifficulty.EASY);

        GameRuntime runtime = runtimeFactory.create(request);

        assertTrue(runtime instanceof LocalAiGameRuntime);
    }

    @Test
    public void lanModeRemainsUnsupportedAtRuntimeFactoryBoundary() {
        GameRuntimeFactory runtimeFactory = createRuntimeFactory();
        NewGameRequest request =
                new NewGameRequest(
                        GameMode.LAN_MULTIPLAYER,
                        2,
                        List.of("Host", "Guest 1"),
                        DictionaryType.AM,
                        null);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> runtimeFactory.create(request));

        assertEquals("LAN_MULTIPLAYER is not supported on this branch.", exception.getMessage());
    }

    private static GameRuntimeFactory createRuntimeFactory() {
        ClockService clockService = new ClockServiceImpl();
        DictionaryRepository dictionaryRepository = new StubDictionaryRepository();
        GameSetupService gameSetupService =
                new GameSetupServiceImpl(dictionaryRepository, clockService, new Random(11L));
        GameApplicationService gameApplicationService =
                new GameApplicationServiceImpl(clockService, dictionaryRepository);
        return new GameRuntimeFactory(gameSetupService, gameApplicationService);
    }

    private static class StubDictionaryRepository extends DictionaryRepository {
        @Override
        public void loadDictionary(DictionaryType dictionaryType) {
        }

        @Override
        public Set<String> getDictionary() {
            return Collections.singleton("BOOK");
        }

        @Override
        public DictionaryType getLoadedDictionaryType() {
            return DictionaryType.AM;
        }

        @Override
        public boolean isAccepted(String word) {
            return "BOOK".equalsIgnoreCase(word);
        }
    }
}
