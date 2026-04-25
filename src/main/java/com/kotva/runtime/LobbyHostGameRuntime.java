package com.kotva.runtime;

import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.ClientRuntimeSnapshot;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.lan.GameSessionBroker;
import com.kotva.lan.LanSystemNotice;
import com.kotva.mode.PlayerController;
import com.kotva.policy.SessionStatus;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Host runtime backed by an already-started lobby session.
 */
public final class LobbyHostGameRuntime implements GameRuntime {
    private static final long CLOCK_SYNC_BROADCAST_INTERVAL_MILLIS = 1_000L;
    private static final String LOCAL_PLAYER_ID = "player-1";

    private final GameApplicationService gameApplicationService;
    private final LanHostService lanHostService;
    private final GameSessionBroker gameSessionBroker;

    private GameSession session;
    private long pendingClockBroadcastMillis;

    public LobbyHostGameRuntime(
            GameApplicationService gameApplicationService,
            GameSession session,
            LanHostService lanHostService,
            GameSessionBroker gameSessionBroker) {
        this.gameApplicationService = Objects.requireNonNull(
                gameApplicationService,
                "gameApplicationService cannot be null.");
        this.session = Objects.requireNonNull(session, "session cannot be null.");
        this.lanHostService = Objects.requireNonNull(lanHostService, "lanHostService cannot be null.");
        this.gameSessionBroker = Objects.requireNonNull(
                gameSessionBroker,
                "gameSessionBroker cannot be null.");
        this.pendingClockBroadcastMillis = 0L;
    }

    @Override
    public void start(NewGameRequest request) {
        // Session is already created before the game scene opens.
    }

    @Override
    public boolean requiresBackgroundRefresh() {
        return true;
    }

    @Override
    public boolean hasSession() {
        return session != null;
    }

    @Override
    public GameSession getSession() {
        return session;
    }

    @Override
    public boolean hasTimeControl() {
        return session != null && session.getConfig().hasTimeControl();
    }

    @Override
    public boolean isSessionInProgress() {
        return session != null && session.getSessionStatus() == SessionStatus.IN_PROGRESS;
    }

    @Override
    public GameSessionSnapshot getSessionSnapshot() {
        return decorateSnapshot(gameApplicationService.getSessionSnapshot(requireSession()));
    }

    @Override
    public GameSessionSnapshot tickClock(long elapsedMillis) {
        if (gameSessionBroker.hasBlockingSystemNotice()) {
            return getSessionSnapshot();
        }
        GameSessionSnapshot previousSnapshot = hasTimeControl() ? getSessionSnapshot() : null;
        GameSessionSnapshot snapshot =
                hasTimeControl()
                        ? decorateSnapshot(gameApplicationService.tickClock(requireSession(), elapsedMillis))
                        : getSessionSnapshot();
        if (elapsedMillis > 0L && hasTimeControl()) {
            pendingClockBroadcastMillis += elapsedMillis;
            if (shouldBroadcastClockUpdate(previousSnapshot, snapshot)
                    || pendingClockBroadcastMillis >= CLOCK_SYNC_BROADCAST_INTERVAL_MILLIS) {
                broadcastViewerSnapshots();
            }
        }
        return snapshot;
    }

    @Override
    public void placeDraftTile(String tileId, Position position) {
        requireCurrentPlayerController().placeDraftTile(
                gameApplicationService,
                requireSession(),
                tileId,
                Objects.requireNonNull(position, "position cannot be null."));
    }

    @Override
    public void moveDraftTile(String tileId, Position position) {
        requireCurrentPlayerController().moveDraftTile(
                gameApplicationService,
                requireSession(),
                tileId,
                Objects.requireNonNull(position, "position cannot be null."));
    }

    @Override
    public void removeDraftTile(String tileId) {
        requireCurrentPlayerController().removeDraftTile(
                gameApplicationService,
                requireSession(),
                tileId);
    }

    @Override
    public void assignBlankTileLetter(String tileId, char assignedLetter) {
        requireCurrentPlayerController().assignLettertoBlank(
                gameApplicationService,
                requireSession(),
                tileId,
                assignedLetter);
    }

    @Override
    public void recallAllDraftTiles() {
        requireCurrentPlayerController().recallAllDraftTiles(
                gameApplicationService,
                requireSession());
    }

    @Override
    public void submitDraft() {
        requireCurrentPlayerController().submitDraft(gameApplicationService, requireSession());
        broadcastViewerSnapshots();
    }

    @Override
    public void submitDraft(String clientActionId) {
        requireCurrentPlayerController().submitDraft(
                gameApplicationService,
                requireSession(),
                clientActionId);
        broadcastViewerSnapshots();
    }

    @Override
    public void passTurn() {
        requireCurrentPlayerController().passTurn(gameApplicationService, requireSession());
        broadcastViewerSnapshots();
    }

    @Override
    public void passTurn(String clientActionId) {
        requireCurrentPlayerController().passTurn(
                gameApplicationService,
                requireSession(),
                clientActionId);
        broadcastViewerSnapshots();
    }

    @Override
    public void resign() {
        requireCurrentPlayerController().resign(gameApplicationService, requireSession());
        broadcastViewerSnapshots();
    }

    @Override
    public void resign(String clientActionId) {
        requireCurrentPlayerController().resign(
                gameApplicationService,
                requireSession(),
                clientActionId);
        broadcastViewerSnapshots();
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
        if (session != null) {
            gameSessionBroker.stopServer();
        }
        session = null;
        pendingClockBroadcastMillis = 0L;
    }

    private GameSessionSnapshot decorateSnapshot(GameSessionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        GameSessionSnapshot viewerSnapshot =
                GameSessionSnapshotFactory.fromSessionForViewer(requireSession(), LOCAL_PLAYER_ID);
        LanSystemNotice blockingNotice = gameSessionBroker.getBlockingSystemNotice();
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

    private void broadcastViewerSnapshots() {
        gameSessionBroker.broadcastViewerSnapshotsToAllConnectedClients();
        pendingClockBroadcastMillis = 0L;
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

    private GameSession requireSession() {
        return Objects.requireNonNull(session, "session cannot be null.");
    }

    private PlayerController requireCurrentPlayerController() {
        Player currentPlayer = requireSession().getGameState().requireCurrentActivePlayer();
        return Objects.requireNonNull(
                currentPlayer.getController(),
                "current player controller cannot be null.");
    }
}
