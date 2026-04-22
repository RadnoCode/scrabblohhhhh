package com.kotva.application.service.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kotva.application.session.ClientRuntimeSnapshot;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.session.PlayerConfig;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.service.MovePreviewService;
import com.kotva.application.draft.DraftManager;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.LanClientTransport;
import com.kotva.infrastructure.network.LanInboundMessage;
import com.kotva.infrastructure.network.LanSnapshotMessage;
import com.kotva.mode.GameMode;
import com.kotva.mode.PlayerController;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class LanClientServiceTest {
    private static final String HOST_PLAYER_ID = "player-1";
    private static final String GUEST_PLAYER_ID = "player-2";

    @Test
    public void uiSnapshotLocksInteractionWhileAnotherPlayerOwnsTurn() {
        ClientGameContext context = createContext(HOST_PLAYER_ID, GUEST_PLAYER_ID);
        LanClientService service = new LanClientService(context, new NoOpLanClientTransport());

        ClientRuntimeSnapshot runtimeSnapshot = service.getUiSnapshot().getClientRuntimeSnapshot();

        assertTrue(runtimeSnapshot.interactionLocked());
        assertEquals("Waiting for remote player.", runtimeSnapshot.summary());
        assertEquals("Host is taking this turn.", runtimeSnapshot.details());
    }

    @Test
    public void uiSnapshotKeepsInteractionEnabledDuringLocalPlayersTurn() {
        ClientGameContext context = createContext(GUEST_PLAYER_ID, GUEST_PLAYER_ID);
        LanClientService service = new LanClientService(context, new NoOpLanClientTransport());

        ClientRuntimeSnapshot runtimeSnapshot = service.getUiSnapshot().getClientRuntimeSnapshot();

        assertFalse(runtimeSnapshot.interactionLocked());
        assertEquals("", runtimeSnapshot.summary());
        assertEquals("", runtimeSnapshot.details());
    }

    @Test
    public void sameTurnHostSnapshotPreservesClientsLocalDraftPreview() {
        ClientGameContext context = createContext(GUEST_PLAYER_ID, GUEST_PLAYER_ID);
        QueueLanClientTransport transport = new QueueLanClientTransport();
        ClientDraftService draftService = new ClientDraftService(
            context,
            new DraftManager(),
            new ClientPreviewService(context, new StubMovePreviewService()));
        LanClientService service = new LanClientService(
            context,
            draftService,
            transport,
            PlayerController.create(GUEST_PLAYER_ID, PlayerType.LAN));

        String tileId = context.getLatestSnapshot().getCurrentRackTiles().stream()
            .filter(rackTile -> rackTile.getTileId() != null)
            .findFirst()
            .orElseThrow()
            .getTileId();

        service.placeDraftTile(tileId, new Position(7, 7));
        transport.enqueue(new LanSnapshotMessage(context.getLatestSnapshot()));

        GameSessionSnapshot uiSnapshot = service.tickClock(0L);

        assertEquals(1, uiSnapshot.getDraftPlacements().size());
        assertEquals(tileId, uiSnapshot.getDraftPlacements().get(0).getTileId());
        assertTrue(
            uiSnapshot.getBoardCells().stream().anyMatch(boardCell ->
                boardCell.isDraft()
                    && boardCell.getRow() == 7
                    && boardCell.getCol() == 7
                    && tileId.equals(boardCell.getTileId())));
    }

    @Test
    public void uiSnapshotPreservesLocalBlankAssignmentForRackDisplay() {
        ClientGameContext context = createContextWithGuestBlankRackTile();
        LanClientService service = new LanClientService(context, new NoOpLanClientTransport());
        String blankTileId = context.getLatestSnapshot().getCurrentRackTiles().stream()
            .filter(rackTile -> rackTile.isBlank() && rackTile.getTileId() != null)
            .findFirst()
            .orElseThrow()
            .getTileId();

        service.assignBlankTileLetter(blankTileId, 'q');

        GameSessionSnapshot uiSnapshot = service.getUiSnapshot();

        assertEquals(
            Character.valueOf('Q'),
            uiSnapshot.getCurrentRackTiles().stream()
                .filter(rackTile -> blankTileId.equals(rackTile.getTileId()))
                .findFirst()
                .orElseThrow()
                .getAssignedLetter());
        assertEquals(
            Character.valueOf('Q'),
            uiSnapshot.getCurrentRackTiles().stream()
                .filter(rackTile -> blankTileId.equals(rackTile.getTileId()))
                .findFirst()
                .orElseThrow()
                .getDisplayLetter());
    }

    private ClientGameContext createContext(String currentPlayerId, String localPlayerId) {
        GameConfig config = createConfig();
        GameSessionSnapshot initialSnapshot = createViewerSnapshot(config, currentPlayerId, localPlayerId);
        return new ClientGameContext(config, initialSnapshot, localPlayerId);
    }

    private GameSessionSnapshot createViewerSnapshot(
        GameConfig config,
        String currentPlayerId,
        String viewerPlayerId) {
        Player host = new Player(HOST_PLAYER_ID, "Host", PlayerType.LOCAL);
        Player guest = new Player(GUEST_PLAYER_ID, "Guest", PlayerType.LAN);
        GameState gameState = new GameState(List.of(host, guest));
        gameState.initialDraw();
        if (GUEST_PLAYER_ID.equals(currentPlayerId)) {
            gameState.advanceToNextActivePlayer();
        }

        GameSession session = new GameSession("session-1", config, gameState);
        session.setSessionStatus(SessionStatus.IN_PROGRESS);
        return GameSessionSnapshotFactory.fromSessionForViewer(session, viewerPlayerId);
    }

    private GameConfig createConfig() {
        return new GameConfig(
            GameMode.LAN_MULTIPLAYER,
            List.of(
                new PlayerConfig("Host", PlayerType.LOCAL),
                new PlayerConfig("Guest", PlayerType.LAN)),
            DictionaryType.AM,
            null);
    }

    private ClientGameContext createContextWithGuestBlankRackTile() {
        GameConfig config = createConfig();
        Player host = new Player(HOST_PLAYER_ID, "Host", PlayerType.LOCAL);
        Player guest = new Player(GUEST_PLAYER_ID, "Guest", PlayerType.LAN);
        guest.getRack().setTileAt(0, new Tile("blank-1", ' ', 0, true));
        guest.getRack().setTileAt(1, new Tile("tile-a", 'A', 1, false));
        GameSession session = new GameSession("session-blank", config, new GameState(List.of(host, guest)));
        session.setSessionStatus(SessionStatus.IN_PROGRESS);
        session.getGameState().advanceToNextActivePlayer();
        GameSessionSnapshot snapshot = GameSessionSnapshotFactory.fromSessionForViewer(session, GUEST_PLAYER_ID);
        return new ClientGameContext(config, snapshot, GUEST_PLAYER_ID);
    }

    private static final class NoOpLanClientTransport implements LanClientTransport {
        @Override
        public void sendCommand(CommandEnvelope commandEnvelope) {
        }

        @Override
        public List<LanInboundMessage> drainInboundMessages() {
            return List.of();
        }
    }

    private static final class QueueLanClientTransport implements LanClientTransport {
        private final List<LanInboundMessage> inboundMessages = new ArrayList<>();

        @Override
        public void sendCommand(CommandEnvelope commandEnvelope) {
        }

        @Override
        public List<LanInboundMessage> drainInboundMessages() {
            List<LanInboundMessage> drainedMessages = List.copyOf(inboundMessages);
            inboundMessages.clear();
            return drainedMessages;
        }

        private void enqueue(LanInboundMessage inboundMessage) {
            inboundMessages.add(inboundMessage);
        }
    }

    private static final class StubMovePreviewService implements MovePreviewService {
        @Override
        public PreviewResult preview(com.kotva.application.session.GameSession session) {
            return new PreviewResult(false, 0, List.of(), List.of(), List.of());
        }

        @Override
        public PreviewResult preview(
            GameState gameState,
            DictionaryType dictionaryType,
            String playerId,
            com.kotva.application.draft.TurnDraft turnDraft) {
            return new PreviewResult(false, 0, List.of(), List.of(), List.of());
        }
    }
}
