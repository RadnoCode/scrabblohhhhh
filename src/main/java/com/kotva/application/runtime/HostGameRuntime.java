package com.kotva.application.runtime;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.ClientRuntimeSnapshot;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.lan.GameSessionBroker;
import com.kotva.lan.LanSystemNotice;
import com.kotva.lan.LocalGameSession;
import com.kotva.lan.discovery.LanDiscoveryHostService;
import com.kotva.lan.discovery.UdpLanDiscoveryHostService;
import com.kotva.lan.udp.DiscoveredRoom;
import com.kotva.policy.SessionStatus;
import java.io.IOException;
import java.util.Objects;

final class HostGameRuntime extends AbstractLocalGameRuntime {
    private static final long CLOCK_SYNC_BROADCAST_INTERVAL_MILLIS = 1_000L;
    private static final String LOCAL_PLAYER_ID = "player-1";

    private LanHostService lanHostService;
    private GameSessionBroker gameSessionBroker;
    private LanDiscoveryHostService lanDiscoveryHostService;
    private long pendingClockBroadcastMillis;

    HostGameRuntime(
            GameSetupService gameSetupService,
            GameApplicationService gameApplicationService) {
        super(gameSetupService, gameApplicationService);
    }

    @Override
    protected void afterSessionStarted() {
        lanHostService = new LanHostService(requireSession(), gameApplicationService);
        gameSessionBroker = new GameSessionBroker(GameSessionBroker.DEFAULT_PORT);
        pendingClockBroadcastMillis = 0L;
        try {
            gameSessionBroker.createSession(
                    requireSession(),
                    lanHostService,
                    LOCAL_PLAYER_ID,
                    requireLocalPlayerName());
            lanDiscoveryHostService = new UdpLanDiscoveryHostService();
            lanDiscoveryHostService.startHosting(this::buildDiscoveredRoom);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start LAN host broker.", exception);
        }
    }

    @Override
    protected GameSessionSnapshot decorateSnapshot(GameSessionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        GameSessionSnapshot viewerSnapshot =
                GameSessionSnapshotFactory.fromSessionForViewer(requireSession(), LOCAL_PLAYER_ID);
        LanSystemNotice blockingNotice =
                gameSessionBroker == null ? null : gameSessionBroker.getBlockingSystemNotice();
        if (blockingNotice != null && blockingNotice.interactionLocked()) {
            return GameSessionSnapshotFactory.withClientRuntimeSnapshot(
                    viewerSnapshot,
                    new ClientRuntimeSnapshot(
                            true,
                            null,
                            blockingNotice.summary(),
                            blockingNotice.details()));
        }
        if (viewerSnapshot.getSessionStatus() != SessionStatus.IN_PROGRESS
                || Objects.equals(viewerSnapshot.getCurrentPlayerId(), LOCAL_PLAYER_ID)) {
            return viewerSnapshot;
        }
        return GameSessionSnapshotFactory.withClientRuntimeSnapshot(
                viewerSnapshot,
                new ClientRuntimeSnapshot(
                        true,
                        null,
                        "Waiting for remote player.",
                        viewerSnapshot.getCurrentPlayerName() + " is taking this turn."));
    }

    @Override
    public boolean requiresBackgroundRefresh() {
        return true;
    }

    @Override
    public GameSessionSnapshot tickClock(long elapsedMillis) {
        if (gameSessionBroker != null && gameSessionBroker.hasBlockingSystemNotice()) {
            return getSessionSnapshot();
        }
        GameSessionSnapshot previousSnapshot = hasTimeControl() ? getSessionSnapshot() : null;
        GameSessionSnapshot snapshot =
                hasTimeControl()
                        ? super.tickClock(elapsedMillis)
                        : getSessionSnapshot();
        if (gameSessionBroker != null && elapsedMillis > 0L && hasTimeControl()) {
            pendingClockBroadcastMillis += elapsedMillis;
            if (shouldBroadcastClockUpdate(previousSnapshot, snapshot)
                    || pendingClockBroadcastMillis >= CLOCK_SYNC_BROADCAST_INTERVAL_MILLIS) {
                broadcastViewerSnapshots();
            }
        }
        return snapshot;
    }

    @Override
    public void submitDraft() {
        super.submitDraft();
        broadcastViewerSnapshots();
    }

    @Override
    public void passTurn() {
        super.passTurn();
        broadcastViewerSnapshots();
    }

    @Override
    public void shutdown() {
        if (lanDiscoveryHostService != null) {
            lanDiscoveryHostService.stop();
            lanDiscoveryHostService = null;
        }
        if (gameSessionBroker != null) {
            gameSessionBroker.stopServer();
            gameSessionBroker = null;
        }
        lanHostService = null;
        pendingClockBroadcastMillis = 0L;
        super.shutdown();
    }

    private void broadcastViewerSnapshots() {
        if (gameSessionBroker != null) {
            gameSessionBroker.broadcastViewerSnapshotsToAllConnectedClients();
            pendingClockBroadcastMillis = 0L;
        }
    }

    private boolean shouldBroadcastClockUpdate(
            GameSessionSnapshot previousSnapshot,
            GameSessionSnapshot currentSnapshot) {
        if (previousSnapshot == null || currentSnapshot == null) {
            return false;
        }
        return previousSnapshot.getSessionStatus() != currentSnapshot.getSessionStatus()
                || previousSnapshot.getTurnNumber() != currentSnapshot.getTurnNumber()
                || !Objects.equals(
                        previousSnapshot.getCurrentPlayerId(),
                        currentSnapshot.getCurrentPlayerId())
                || previousSnapshot.getCurrentPlayerClockPhase()
                        != currentSnapshot.getCurrentPlayerClockPhase();
    }

    private String requireLocalPlayerName() {
        return requireSession().getGameState().getPlayerById(LOCAL_PLAYER_ID).getPlayerName();
    }

    private DiscoveredRoom buildDiscoveredRoom() {
        LocalGameSession localGameSession =
                gameSessionBroker == null ? null : gameSessionBroker.getLocalGameSession();
        int currentPlayers =
                localGameSession == null
                        ? requireSession().getConfig().getPlayerCount()
                        : localGameSession.getCurrentPlayerCount();
        return new DiscoveredRoom(
                requireSession().getSessionId(),
                requireLocalPlayerName(),
                "",
                gameSessionBroker == null ? GameSessionBroker.DEFAULT_PORT : gameSessionBroker.getBoundPort(),
                currentPlayers,
                requireSession().getConfig().getPlayerCount(),
                resolveLanguageLabel(),
                resolveGameTimeLabel(),
                System.currentTimeMillis());
    }

    private String resolveLanguageLabel() {
        return switch (requireSession().getConfig().getDictionaryType()) {
            case BR -> "British";
            case AM -> "American";
        };
    }

    private String resolveGameTimeLabel() {
        if (requireSession().getConfig().getTimeControlConfig() == null) {
            return "--";
        }
        long minutes =
                requireSession().getConfig().getTimeControlConfig().getMainTimeMillis() / 60_000L;
        return minutes + "min";
    }
}
