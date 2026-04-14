package com.kotva.application.runtime;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.ClientRuntimeSnapshot;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.RemoteCommandResult;
import com.kotva.policy.SessionStatus;
import java.util.Objects;

final class HostGameRuntime extends AbstractLocalGameRuntime {
    private final String localPlayerId;

    private LanHostService lanHostService;

    HostGameRuntime(
            RuntimeLaunchSpec launchSpec,
            GameSetupService gameSetupService,
            GameApplicationService gameApplicationService) {
        super(launchSpec, gameSetupService, gameApplicationService);
        this.localPlayerId = launchSpec.requireLanLaunchConfig().getLocalPlayerId();
    }

    @Override
    protected void afterSessionStarted() {
        lanHostService = new LanHostService(requireSession(), gameApplicationService);
    }

    @Override
    protected GameSessionSnapshot decorateSnapshot(GameSessionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        GameSessionSnapshot viewerSnapshot =
                GameSessionSnapshotFactory.fromSessionForViewer(requireSession(), localPlayerId);
        if (viewerSnapshot.getSessionStatus() != SessionStatus.IN_PROGRESS
                || Objects.equals(viewerSnapshot.getCurrentPlayerId(), localPlayerId)) {
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
    public void shutdown() {
        lanHostService = null;
        super.shutdown();
    }

    public RemoteCommandResult handle(CommandEnvelope commandEnvelope) {
        return requireLanHostService().handle(commandEnvelope);
    }

    public GameSessionSnapshot getViewerSnapshot(String viewerPlayerId) {
        return requireLanHostService().snapshotForViewer(viewerPlayerId);
    }

    private LanHostService requireLanHostService() {
        return Objects.requireNonNull(lanHostService, "lanHostService cannot be null.");
    }
}
