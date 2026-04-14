package com.kotva.application.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.BoardCellRenderSnapshot;
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
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.LanClientTransport;
import com.kotva.infrastructure.network.LanCommandResultMessage;
import com.kotva.infrastructure.network.LanInboundMessage;
import com.kotva.infrastructure.network.RemoteCommandResult;
import com.kotva.mode.GameMode;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.junit.Test;

public class ClientGameRuntimeTest {
    @Test
    public void failedResultOnSameTurnPreservesLocalDraft() {
        GameSession session = createLanSession();
        Player localPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        Tile tileT = drawTileWithLetter(session.getGameState().getTileBag(), 'T');
        localPlayer.getRack().setTileAt(0, tileA);
        localPlayer.getRack().setTileAt(1, tileT);

        StubLanClientTransport transport = new StubLanClientTransport();
        ClientGameRuntime runtime = createRuntime(session, transport, localPlayer.getPlayerId());
        runtime.start();

        runtime.placeDraftTile(tileA.getTileID(), new Position(7, 7));
        runtime.placeDraftTile(tileT.getTileID(), new Position(7, 8));
        runtime.submitDraft();

        GameSessionSnapshot pendingSnapshot = runtime.getSessionSnapshot();
        assertTrue(pendingSnapshot.getClientRuntimeSnapshot().interactionLocked());
        assertNotNull(pendingSnapshot.getClientRuntimeSnapshot().pendingCommandId());

        transport.enqueue(
                new LanCommandResultMessage(
                        new RemoteCommandResult(
                                pendingSnapshot.getClientRuntimeSnapshot().pendingCommandId(),
                                false,
                                "Illegal placement.",
                                0,
                                localPlayer.getPlayerId(),
                                false,
                                null,
                                GameSessionSnapshotFactory.fromSessionForViewer(
                                        session, localPlayer.getPlayerId()))));

        runtime.tickClock(0L);
        GameSessionSnapshot rejectedSnapshot = runtime.getSessionSnapshot();

        assertFalse(rejectedSnapshot.getClientRuntimeSnapshot().interactionLocked());
        assertEquals("Host rejected action.", rejectedSnapshot.getClientRuntimeSnapshot().summary());
        assertEquals(2, rejectedSnapshot.getDraftPlacements().size());
        assertEquals(2, countDraftBoardCells(rejectedSnapshot));
    }

    @Test
    public void successfulHostResultClearsDraftAndAppliesAuthoritativeSnapshot() {
        GameSession session = createLanSession();
        GameApplicationServiceImpl gameApplicationService =
                new GameApplicationServiceImpl(
                        new ClockServiceImpl(),
                        new StubDictionaryRepository(Set.of("AT")));
        LanHostService hostService = new LanHostService(session, gameApplicationService);
        Player localPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile tileA = drawTileWithLetter(session.getGameState().getTileBag(), 'A');
        Tile tileT = drawTileWithLetter(session.getGameState().getTileBag(), 'T');
        localPlayer.getRack().setTileAt(0, tileA);
        localPlayer.getRack().setTileAt(1, tileT);

        StubLanClientTransport transport = new StubLanClientTransport();
        ClientGameRuntime runtime = createRuntime(session, transport, localPlayer.getPlayerId());
        runtime.start();

        runtime.placeDraftTile(tileA.getTileID(), new Position(7, 7));
        runtime.placeDraftTile(tileT.getTileID(), new Position(7, 8));
        runtime.submitDraft();

        CommandEnvelope submittedCommand = transport.getSentCommands().get(0);
        assertEquals(session.getSessionId(), submittedCommand.getSessionId());
        assertEquals(localPlayer.getPlayerId(), submittedCommand.getPlayerId());
        assertEquals(0, submittedCommand.getExpectedTurnNumber());

        transport.enqueue(new LanCommandResultMessage(hostService.handle(submittedCommand)));

        runtime.tickClock(0L);
        GameSessionSnapshot settledSnapshot = runtime.getSessionSnapshot();

        assertFalse(settledSnapshot.getClientRuntimeSnapshot().interactionLocked());
        assertTrue(settledSnapshot.getClientRuntimeSnapshot().summary().isBlank());
        assertTrue(settledSnapshot.getDraftPlacements().isEmpty());
        assertEquals(1, settledSnapshot.getTurnNumber());
        assertEquals(2, settledSnapshot.getBoardCells().size());
        assertTrue(settledSnapshot.getBoardCells().stream().noneMatch(BoardCellRenderSnapshot::isDraft));
    }

    private ClientGameRuntime createRuntime(
            GameSession session, StubLanClientTransport transport, String localPlayerId) {
        LanLaunchConfig lanLaunchConfig =
                new LanLaunchConfig(
                        LanRole.CLIENT,
                        session.getConfig(),
                        localPlayerId,
                        GameSessionSnapshotFactory.fromSessionForViewer(session, localPlayerId),
                        transport);
        return new ClientGameRuntime(RuntimeLaunchSpec.forLanClient(lanLaunchConfig));
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

    private Tile drawTileWithLetter(TileBag tileBag, char letter) {
        while (!tileBag.isEmpty()) {
            Tile tile = tileBag.drawTile();
            if (tile.getLetter() == letter && !tile.isBlank()) {
                return tile;
            }
        }
        throw new AssertionError("Expected tile with letter " + letter + " to be available.");
    }

    private int countDraftBoardCells(GameSessionSnapshot snapshot) {
        int draftCellCount = 0;
        for (BoardCellRenderSnapshot boardCellRenderSnapshot : snapshot.getBoardCells()) {
            if (boardCellRenderSnapshot.isDraft()) {
                draftCellCount++;
            }
        }
        return draftCellCount;
    }

    private static final class StubLanClientTransport implements LanClientTransport {
        private final List<CommandEnvelope> sentCommands = new ArrayList<>();
        private final Queue<LanInboundMessage> inboundMessages = new ArrayDeque<>();

        @Override
        public void sendCommand(CommandEnvelope commandEnvelope) {
            sentCommands.add(commandEnvelope);
        }

        @Override
        public List<LanInboundMessage> drainInboundMessages() {
            List<LanInboundMessage> drained = new ArrayList<>();
            while (!inboundMessages.isEmpty()) {
                drained.add(inboundMessages.remove());
            }
            return drained;
        }

        private void enqueue(LanInboundMessage inboundMessage) {
            inboundMessages.add(inboundMessage);
        }

        private List<CommandEnvelope> getSentCommands() {
            return sentCommands;
        }
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
