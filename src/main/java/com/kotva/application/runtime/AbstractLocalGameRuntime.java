package com.kotva.application.runtime;

import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.mode.PlayerController;
import com.kotva.policy.SessionStatus;
import java.util.Objects;
import java.util.function.Consumer;

abstract class AbstractLocalGameRuntime implements GameRuntime {
    private final GameSetupService gameSetupService;
    protected final GameApplicationService gameApplicationService;

    private GameSession session;

    protected AbstractLocalGameRuntime(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService) {
        this.gameSetupService =
        Objects.requireNonNull(gameSetupService, "gameSetupService cannot be null.");
        this.gameApplicationService = Objects.requireNonNull(
            gameApplicationService, "gameApplicationService cannot be null.");
    }

        @Override
    public void start(NewGameRequest request) {
        Objects.requireNonNull(request, "request cannot be null.");
        shutdown();
        session = gameSetupService.startNewGame(request);
        afterSessionStarted();
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
        return decorateSnapshot(gameApplicationService.tickClock(requireSession(), elapsedMillis));
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
    public void recallAllDraftTiles() {
        requireCurrentPlayerController().recallAllDraftTiles(
            gameApplicationService,
            requireSession());
    }

        @Override
    public void submitDraft() {
        requireCurrentPlayerController().submitDraft(gameApplicationService, requireSession());
    }

        @Override
    public void submitDraft(String clientActionId) {
        requireCurrentPlayerController().submitDraft(
            gameApplicationService,
            requireSession(),
            clientActionId);
    }

        @Override
    public void passTurn() {
        requireCurrentPlayerController().passTurn(gameApplicationService, requireSession());
    }

        @Override
    public void passTurn(String clientActionId) {
        requireCurrentPlayerController().passTurn(
            gameApplicationService,
            requireSession(),
            clientActionId);
    }

        @Override
    public void resign() {
        requireCurrentPlayerController().resign(gameApplicationService, requireSession());
    }

        @Override
    public void resign(String clientActionId) {
        requireCurrentPlayerController().resign(
            gameApplicationService,
            requireSession(),
            clientActionId);
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
        disableAutomatedTurnSupport();
        session = null;
    }

    protected void afterSessionStarted() {
    }

    protected GameSessionSnapshot decorateSnapshot(GameSessionSnapshot snapshot) {
        return Objects.requireNonNull(snapshot, "snapshot cannot be null.");
    }

    protected final GameSession requireSession() {
        return Objects.requireNonNull(session, "session cannot be null.");
    }

    protected final PlayerController requireCurrentPlayerController() {
        Player currentPlayer = resolveCurrentPlayer();
        if (currentPlayer == null) {
            throw new IllegalStateException("current player is unavailable.");
        }
        return Objects.requireNonNull(
            currentPlayer.getController(),
            "current player controller cannot be null.");
    }

    private Player resolveCurrentPlayer() {
        if (session == null || !session.getGameState().hasActivePlayers()) {
            return null;
        }
        return session.getGameState().requireCurrentActivePlayer();
    }
}