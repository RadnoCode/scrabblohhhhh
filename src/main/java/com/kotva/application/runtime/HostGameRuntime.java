package com.kotva.application.runtime;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.ClientRuntimeSnapshot;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.lan.GameSessionBroker;
import com.kotva.policy.SessionStatus;
import java.io.IOException;
import java.util.Objects;

final class HostGameRuntime extends AbstractLocalGameRuntime {
    private static final String LOCAL_PLAYER_ID = "player-1";

    private LanHostService lanHostService;
    private GameSessionBroker gameSessionBroker;

    HostGameRuntime(
            GameSetupService gameSetupService,
            GameApplicationService gameApplicationService) {
        super(gameSetupService, gameApplicationService);
    }

    @Override
    protected void afterSessionStarted() {
        lanHostService = new LanHostService(requireSession(), gameApplicationService);
        gameSessionBroker = new GameSessionBroker(GameSessionBroker.DEFAULT_PORT);
        try {
            gameSessionBroker.createSession(
                    requireSession(),
                    lanHostService,
                    LOCAL_PLAYER_ID,
                    requireLocalPlayerName());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start LAN host broker.", exception);
        }
    }

    @Override
    protected GameSessionSnapshot decorateSnapshot(GameSessionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        GameSessionSnapshot viewerSnapshot =
                GameSessionSnapshotFactory.fromSessionForViewer(requireSession(), LOCAL_PLAYER_ID);
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
        GameSessionSnapshot snapshot =
                hasTimeControl()
                        ? super.tickClock(elapsedMillis)
                        : getSessionSnapshot();
        if (gameSessionBroker != null && elapsedMillis > 0L && hasTimeControl()) {
            gameSessionBroker.broadcastViewerSnapshotsToAllConnectedClients();
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
        if (gameSessionBroker != null) {
            gameSessionBroker.stopServer();
            gameSessionBroker = null;
        }
        lanHostService = null;
        super.shutdown();
    }

    private void broadcastViewerSnapshots() {
        if (gameSessionBroker != null) {
            gameSessionBroker.broadcastViewerSnapshotsToAllConnectedClients();
        }
    }

    private String requireLocalPlayerName() {
        return requireSession().getGameState().getPlayerById(LOCAL_PLAYER_ID).getPlayerName();
    }
}
