package com.kotva.application.runtime;

import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.service.client.ClientGameContext;
import com.kotva.application.service.client.LanClientService;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.Position;
import com.kotva.policy.SessionStatus;
import java.util.Objects;
import java.util.function.Consumer;

final class ClientGameRuntime implements GameRuntime {
    private final RuntimeLaunchSpec launchSpec;

    private ClientGameContext context;
    private LanClientService lanClientService;

    ClientGameRuntime(RuntimeLaunchSpec launchSpec) {
        this.launchSpec = Objects.requireNonNull(launchSpec, "launchSpec cannot be null.");
    }

    @Override
    public void start(NewGameRequest request) {
        shutdown();
        LanLaunchConfig lanLaunchConfig = launchSpec.requireLanLaunchConfig();
        context =
                new ClientGameContext(
                        lanLaunchConfig.getGameConfig(),
                        lanLaunchConfig.getInitialSnapshot(),
                        lanLaunchConfig.getLocalPlayerId());
        lanClientService = new LanClientService(context, lanLaunchConfig.getClientTransport());
    }

    @Override
    public boolean hasSession() {
        return context != null;
    }

    @Override
    public GameSession getSession() {
        return null;
    }

    @Override
    public boolean hasTimeControl() {
        return context != null && context.getConfig().hasTimeControl();
    }

    @Override
    public boolean requiresBackgroundRefresh() {
        return true;
    }

    @Override
    public boolean isSessionInProgress() {
        return context != null && context.getSessionStatus() == SessionStatus.IN_PROGRESS;
    }

    @Override
    public GameSessionSnapshot getSessionSnapshot() {
        return requireLanClientService().getUiSnapshot();
    }

    @Override
    public GameSessionSnapshot tickClock(long elapsedMillis) {
        return requireLanClientService().tickClock(elapsedMillis);
    }

    @Override
    public void placeDraftTile(String tileId, Position position) {
        requireLanClientService().placeDraftTile(tileId, Objects.requireNonNull(position, "position cannot be null."));
    }

    @Override
    public void moveDraftTile(String tileId, Position position) {
        requireLanClientService().moveDraftTile(tileId, Objects.requireNonNull(position, "position cannot be null."));
    }

    @Override
    public void removeDraftTile(String tileId) {
        requireLanClientService().removeDraftTile(tileId);
    }

    @Override
    public void recallAllDraftTiles() {
        requireLanClientService().recallAllDraftTiles();
    }

    @Override
    public void submitDraft() {
        requireLanClientService().submitDraft();
    }

    @Override
    public void passTurn() {
        requireLanClientService().passTurn();
    }

    @Override
    public boolean hasAutomatedTurnSupport() {
        return false;
    }

    @Override
    public boolean isCurrentTurnAutomated() {
        return false;
    }

    @Override
    public void requestAutomatedTurnIfIdle(
            Consumer<AiSessionRuntime.TurnCompletion> completionConsumer) {
    }

    @Override
    public boolean matchesAutomatedTurn(AiSessionRuntime.TurnCompletion completion) {
        return false;
    }

    @Override
    public void applyAutomatedTurn(AiSessionRuntime.TurnCompletion completion) {
        throw new IllegalStateException("Automated turn support is not available.");
    }

    @Override
    public void cancelPendingAutomatedTurn() {
    }

    @Override
    public void disableAutomatedTurnSupport() {
    }

    @Override
    public void shutdown() {
        if (lanClientService != null) {
            lanClientService.shutdown();
        }
        lanClientService = null;
        context = null;
    }

    private LanClientService requireLanClientService() {
        return Objects.requireNonNull(lanClientService, "lanClientService cannot be null.");
    }
}
