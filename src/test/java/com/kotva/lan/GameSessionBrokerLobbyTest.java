package com.kotva.lan;

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
import com.kotva.application.session.TimeControlConfig;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.policy.DictionaryType;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class GameSessionBrokerLobbyTest {
    @Test
    public void lobbyJoinStartAndLeaveFlowsProduceBackendWaitingRoomState() throws Exception {
        GameSessionBroker broker = new GameSessionBroker(0);
        LanLobbyClientSession clientSession = null;

        try {
            String lobbyId =
                    broker.createLobby(
                            new LanLobbySettings(
                                    "Alpha Room",
                                    DictionaryType.AM,
                                    new TimeControlConfig(15L * 60_000L, 30_000L),
                                    3),
                            "player-1",
                            "Host");

            LanLobbySnapshot hostLobbySnapshot = broker.getLobbySnapshot();
            assertNotNull(hostLobbySnapshot);
            assertEquals(lobbyId, hostLobbySnapshot.getLobbyId());
            assertEquals(LanLobbyPhase.WAITING_FOR_PLAYERS, hostLobbySnapshot.getPhase());
            assertEquals(1, hostLobbySnapshot.getCurrentPlayerCount());
            assertEquals("Alpha Room", hostLobbySnapshot.getSettings().getRoomName());
            assertFalse(hostLobbySnapshot.canStart());

            clientSession =
                    LanClientConnector.joinLobby(
                            "127.0.0.1:" + broker.getBoundPort(),
                            "Alice");

            waitForCondition(() -> broker.getLobbySnapshot().getCurrentPlayerCount() == 2);
            assertEquals("player-2", clientSession.getLocalPlayerId());
            assertEquals(2, clientSession.getLobbySnapshot().getCurrentPlayerCount());
            assertTrue(broker.getLobbySnapshot().canStart());

            LanHostGameLaunch hostGameLaunch =
                    broker.startGame(createGameSetupService(), createGameApplicationService());

            assertNotNull(hostGameLaunch);
            assertEquals(2, hostGameLaunch.session().getConfig().getPlayerCount());
            assertEquals(
                    "Host",
                    hostGameLaunch.session().getConfig().getPlayers().get(0).getPlayerName());
            assertEquals(
                    "Alice",
                    hostGameLaunch.session().getConfig().getPlayers().get(1).getPlayerName());

            waitForCondition(clientSession::hasPendingStartLaunchConfig);
            assertNotNull(clientSession.consumeStartLaunchConfig());

            clientSession.disconnect();
            waitForCondition(broker::hasBlockingSystemNotice);
            assertEquals("Alice disconnected.", broker.getBlockingSystemNotice().summary());
        } finally {
            if (clientSession != null) {
                clientSession.disconnect();
            }
            broker.stopServer();
        }
    }

    @Test
    public void lobbyDisconnectBeforeStartPublishesNonBlockingSystemNotice() throws Exception {
        GameSessionBroker broker = new GameSessionBroker(0);
        LanLobbyClientSession clientSession = null;

        try {
            broker.createLobby(
                    new LanLobbySettings(
                            "Beta Room",
                            DictionaryType.AM,
                            new TimeControlConfig(15L * 60_000L, 30_000L),
                            3),
                    "player-1",
                    "Host");

            clientSession = LanClientConnector.joinLobby("127.0.0.1:" + broker.getBoundPort(), "Alice");
            waitForCondition(() -> broker.getLobbySnapshot().getCurrentPlayerCount() == 2);

            clientSession.disconnect();

            waitForCondition(() -> broker.getLobbySnapshot().getCurrentPlayerCount() == 1);
            LanSystemNotice notice = waitForFirstSystemNotice(broker);
            assertNotNull(notice);
            assertEquals("Alice disconnected.", notice.summary());
            assertFalse(notice.interactionLocked());
        } finally {
            if (clientSession != null) {
                clientSession.disconnect();
            }
            broker.stopServer();
        }
    }

    @Test
    public void stoppingHostDisconnectsLobbyClientSession() throws Exception {
        GameSessionBroker broker = new GameSessionBroker(0);
        LanLobbyClientSession clientSession = null;

        try {
            broker.createLobby(
                    new LanLobbySettings(
                            "Gamma Room",
                            DictionaryType.AM,
                            new TimeControlConfig(15L * 60_000L, 30_000L),
                            2),
                    "player-1",
                    "Host");

            clientSession = LanClientConnector.joinLobby("127.0.0.1:" + broker.getBoundPort(), "Alice");
            waitForCondition(() -> broker.getLobbySnapshot().getCurrentPlayerCount() == 2);

            broker.stopServer();

            LanLobbyClientSession finalClientSession = clientSession;
            waitForCondition(finalClientSession::isDisconnected);
            LanSystemNotice notice = clientSession.consumeDisconnectNotice();
            assertNotNull(notice);
            assertEquals("Connection lost to host.", notice.summary());
            assertTrue(notice.interactionLocked());
        } finally {
            if (clientSession != null) {
                clientSession.disconnect();
            }
            broker.stopServer();
        }
    }

    private static void waitForCondition(CheckedCondition condition) throws Exception {
        long deadline = System.currentTimeMillis() + 3_000L;
        while (System.currentTimeMillis() < deadline) {
            if (condition.isSatisfied()) {
                return;
            }
            Thread.sleep(25L);
        }
        throw new AssertionError("Condition was not satisfied before timeout.");
    }

    private static LanSystemNotice waitForFirstSystemNotice(GameSessionBroker broker) throws Exception {
        long deadline = System.currentTimeMillis() + 3_000L;
        while (System.currentTimeMillis() < deadline) {
            List<LanSystemNotice> notices = broker.drainSystemNotices();
            if (!notices.isEmpty()) {
                return notices.get(0);
            }
            Thread.sleep(25L);
        }
        throw new AssertionError("System notice was not published before timeout.");
    }

    private static GameSetupService createGameSetupService() {
        ClockService clockService = new ClockServiceImpl();
        DictionaryRepository dictionaryRepository = new StubDictionaryRepository();
        return new GameSetupServiceImpl(dictionaryRepository, clockService, new Random(13L));
    }

    private static GameApplicationService createGameApplicationService() {
        ClockService clockService = new ClockServiceImpl();
        DictionaryRepository dictionaryRepository = new StubDictionaryRepository();
        return new GameApplicationServiceImpl(clockService, dictionaryRepository);
    }

    private interface CheckedCondition {
        boolean isSatisfied() throws Exception;
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
