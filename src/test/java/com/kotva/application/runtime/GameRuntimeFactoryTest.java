package com.kotva.application.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.service.ClockService;
import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.GameSetupServiceImpl;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.LanClientTransport;
import com.kotva.infrastructure.network.LanInboundMessage;
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
    public void lanModeCreatesHostRuntimeForCreateFlow() {
        GameRuntimeFactory runtimeFactory = createRuntimeFactory();
        NewGameRequest request =
                new NewGameRequest(
                        GameMode.LAN_MULTIPLAYER,
                        2,
                        List.of("Host", "Guest 1"),
                        DictionaryType.AM,
                        null);

        GameRuntime runtime = runtimeFactory.create(request);

        assertTrue(runtime instanceof HostGameRuntime);
    }

    @Test
    public void lanClientLaunchCreatesDedicatedClientRuntime() {
        GameSetupService gameSetupService = createGameSetupService();
        GameRuntimeFactory runtimeFactory = createRuntimeFactory(gameSetupService);
        NewGameRequest request =
                new NewGameRequest(
                        GameMode.LAN_MULTIPLAYER,
                        2,
                        List.of("Host", "Guest 1"),
                        DictionaryType.AM,
                        null);

        GameConfig config = gameSetupService.buildConfig(request);
        GameSession session = gameSetupService.startNewGame(config);
        GameSessionSnapshot initialSnapshot =
                GameSessionSnapshotFactory.fromSessionForViewer(session, "player-2");
        LanLaunchConfig lanLaunchConfig =
                new LanLaunchConfig(
                        LanRole.CLIENT,
                        config,
                        "player-2",
                        initialSnapshot,
                        new StubLanClientTransport());

        GameRuntime runtime = runtimeFactory.create(RuntimeLaunchSpec.forLanClient(lanLaunchConfig));

        assertTrue(runtime instanceof ClientGameRuntime);
    }

    private static GameRuntimeFactory createRuntimeFactory() {
        return createRuntimeFactory(createGameSetupService());
    }

    private static GameRuntimeFactory createRuntimeFactory(GameSetupService gameSetupService) {
        GameApplicationService gameApplicationService =
                new GameApplicationServiceImpl(new ClockServiceImpl(), new StubDictionaryRepository());
        return new GameRuntimeFactory(gameSetupService, gameApplicationService);
    }

    private static GameSetupService createGameSetupService() {
        ClockService clockService = new ClockServiceImpl();
        DictionaryRepository dictionaryRepository = new StubDictionaryRepository();
        return new GameSetupServiceImpl(dictionaryRepository, clockService, new Random(11L));
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

    private static class StubLanClientTransport implements LanClientTransport {
        @Override
        public void sendCommand(CommandEnvelope commandEnvelope) {
        }

        @Override
        public List<LanInboundMessage> drainInboundMessages() {
            return List.of();
        }
    }
}
