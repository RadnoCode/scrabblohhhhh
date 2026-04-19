package com.kotva.presentation.controller;

import com.kotva.application.runtime.GameRuntime;
import com.kotva.application.runtime.GameRuntimeFactory;
import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.session.AiRuntimeSnapshot;
import com.kotva.application.session.BoardCellRenderSnapshot;
import com.kotva.application.session.ClientRuntimeSnapshot;
import com.kotva.application.session.GamePlayerSnapshot;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.RackTileSnapshot;
import com.kotva.domain.model.Position;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.SessionStatus;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.fx.UiScheduler;
import com.kotva.presentation.interaction.GameActionPort;
import com.kotva.presentation.interaction.GameDraftState;
import com.kotva.presentation.interaction.GameInteractionCoordinator;
import com.kotva.presentation.renderer.GameRenderer;
import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * GameController 是游戏页总控。
 * 它负责启动对局、轮询快照、组装 ViewModel，并接收 UI 动作。
 */
public class GameController implements GameActionPort {
    private static final Duration POLLING_INTERVAL = Duration.millis(100);
    private static final int RACK_SLOT_COUNT = 7;

    private final GameRuntimeFactory gameRuntimeFactory;
    private final GameLaunchContext launchContext;
    private final GameViewModel viewModel;
    private final GameDraftState draftState;
    private UiScheduler uiScheduler;
    private GameRenderer renderer;
    private GameRuntime gameRuntime;
    private long lastTickNanos;
    private boolean interactionLocked;

    public GameController(SceneNavigator navigator, GameLaunchContext launchContext) {
        Objects.requireNonNull(navigator, "navigator cannot be null.");
        this.gameRuntimeFactory = navigator.getAppContext().getGameRuntimeFactory();
        this.launchContext = Objects.requireNonNull(launchContext, "launchContext cannot be null.");
        this.viewModel = new GameViewModel("S C R A B B L E");
        this.draftState = new GameDraftState();
    }

    public GameViewModel getViewModel() {
        return viewModel;
    }

    public GameDraftState getDraftState() {
        return draftState;
    }

    public GameSession getSession() {
        return gameRuntime == null ? null : gameRuntime.getSession();
    }

    public boolean hasSession() {
        return gameRuntime != null && gameRuntime.hasSession();
    }

    public void refreshFromCurrentSession() {
        if (gameRuntime == null || !gameRuntime.hasSession() || renderer == null) {
            return;
        }
        renderSnapshot(gameRuntime.getSessionSnapshot());
    }

    public void bind(GameRenderer renderer, GameInteractionCoordinator interactionCoordinator) {
        this.renderer = Objects.requireNonNull(renderer, "renderer cannot be null.");
        Objects.requireNonNull(interactionCoordinator, "interactionCoordinator cannot be null.").attach();
        startGameFromLaunchContext();
    }

    private void startGameFromLaunchContext() {
        stopPolling();
        shutdownRuntime();
        if (launchContext.hasProvidedRuntime()) {
            gameRuntime = launchContext.requireProvidedRuntime();
        } else {
            gameRuntime = gameRuntimeFactory.create(launchContext.getLaunchSpec());
            gameRuntime.start(launchContext.getRequest());
        }

        GameSessionSnapshot firstSnapshot = gameRuntime.getSessionSnapshot();
        renderSnapshot(firstSnapshot);

        if (!gameRuntime.requiresBackgroundRefresh()
                && (!gameRuntime.isSessionInProgress() || !gameRuntime.hasTimeControl())) {
            return;
        }

        lastTickNanos = System.nanoTime();
        uiScheduler = new UiScheduler(POLLING_INTERVAL, this::pollSnapshot);
        uiScheduler.start();
    }

    private void pollSnapshot() {
        if (gameRuntime == null || !gameRuntime.hasSession()) {
            return;
        }

        GameSessionSnapshot snapshot =
                gameRuntime.tickClock(resolveElapsedMillisForRuntimePump());
        renderSnapshot(snapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
        }
    }

    private void renderSnapshot(GameSessionSnapshot snapshot) {
        AiRuntimeSnapshot aiRuntimeSnapshot = snapshot.getAiRuntimeSnapshot();
        ClientRuntimeSnapshot clientRuntimeSnapshot = snapshot.getClientRuntimeSnapshot();
        viewModel.setStepTimerTitle("Step Time");
        viewModel.setTotalTimerTitle("Total Time");
        viewModel.setTotalTimerText(resolveTotalTimerText(snapshot));
        viewModel.setStepTimerText(resolveStepTimerText(snapshot));
        viewModel.setPlayerCards(buildPlayerCards(snapshot));
        viewModel.setBoardTiles(buildBoardTiles(snapshot));
        viewModel.setRackTiles(buildRackTiles(snapshot));
        interactionLocked =
                resolveInteractionLocked(snapshot, aiRuntimeSnapshot, clientRuntimeSnapshot);
        viewModel.setInteractionLocked(interactionLocked);
        viewModel.setAiErrorSummary(resolveStatusSummary(aiRuntimeSnapshot, clientRuntimeSnapshot));
        viewModel.setAiErrorDetails(resolveStatusDetails(aiRuntimeSnapshot, clientRuntimeSnapshot));
        draftState.syncSnapshot(viewModel.getRackTiles(), viewModel.getBoardTiles());
        renderer.render(viewModel);
        syncAiTurn(snapshot);
    }

    private List<GameViewModel.PlayerCardModel> buildPlayerCards(GameSessionSnapshot snapshot) {
        List<GameViewModel.PlayerCardModel> playerCards = new ArrayList<>();
        for (GamePlayerSnapshot player : snapshot.getPlayers()) {
            playerCards.add(new GameViewModel.PlayerCardModel(
                    player.getPlayerName(),
                    player.getPlayerId(),
                    player.getScore(),
                    player.isCurrentTurn(),
                    player.isActive()));
        }
        return playerCards;
    }

    private List<GameViewModel.TileModel> buildRackTiles(GameSessionSnapshot snapshot) {
        List<GameViewModel.TileModel> rackTiles = new ArrayList<>();
        for (int index = 0; index < RACK_SLOT_COUNT; index++) {
            rackTiles.add(GameViewModel.TileModel.empty());
        }

        for (RackTileSnapshot rackTile : snapshot.getVisibleRackTiles()) {
            int slotIndex = rackTile.getSlotIndex();
            if (slotIndex < 0 || slotIndex >= rackTiles.size()) {
                continue;
            }
            if (rackTile.getTileId() == null) {
                rackTiles.set(slotIndex, GameViewModel.TileModel.empty());
                continue;
            }
            rackTiles.set(
                    slotIndex,
                    GameViewModel.TileModel.filled(
                            rackTile.getTileId(),
                            resolveDisplayLetter(rackTile.getDisplayLetter()),
                            rackTile.getScore()));
        }
        return rackTiles;
    }

    private List<GameViewModel.BoardTileModel> buildBoardTiles(GameSessionSnapshot snapshot) {
        List<GameViewModel.BoardTileModel> boardTiles = new ArrayList<>();
        for (BoardCellRenderSnapshot boardCell : snapshot.getBoardCells()) {
            boardTiles.add(new GameViewModel.BoardTileModel(
                    new BoardCoordinate(boardCell.getRow(), boardCell.getCol()),
                    GameViewModel.TileModel.filled(
                            boardCell.getTileId(),
                            resolveDisplayLetter(boardCell.getDisplayLetter()),
                            boardCell.getScore()),
                    boardCell.isDraft(),
                    boardCell.isPreviewValid(),
                    boardCell.isPreviewInvalid(),
                    boardCell.isMainWordHighlighted(),
                    boardCell.isCrossWordHighlighted()));
        }
        return boardTiles;
    }

    private String resolveTotalTimerText(GameSessionSnapshot snapshot) {
        if (snapshot.getCurrentPlayerClockPhase() == ClockPhase.DISABLED) {
            return "--:--";
        }
        return formatDuration(snapshot.getCurrentPlayerMainTimeRemainingMillis());
    }

    private String resolveStepTimerText(GameSessionSnapshot snapshot) {
        if (snapshot.getCurrentPlayerClockPhase() != ClockPhase.BYO_YOMI) {
            return "--:--";
        }
        return formatDuration(snapshot.getCurrentPlayerByoYomiRemainingMillis());
    }

    private String formatDuration(long millis) {
        long safeMillis = Math.max(0L, millis);
        if (safeMillis == 0L) {
            return "00:00";
        }

        long totalSeconds = (safeMillis + 999L) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0L) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void stopPolling() {
        if (uiScheduler != null) {
            uiScheduler.stop();
            uiScheduler = null;
        }
    }

    public void shutdown() {
        stopPolling();
        shutdownRuntime();
    }

    private void refreshSnapshotAfterAction() {
        if (gameRuntime == null || !gameRuntime.hasSession()) {
            return;
        }

        GameSessionSnapshot snapshot = gameRuntime.getSessionSnapshot();
        renderSnapshot(snapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
        }
    }

    private void tickClockBeforeActionIfNeeded() {
        if (gameRuntime == null || !gameRuntime.hasSession()) {
            return;
        }

        if (!gameRuntime.requiresBackgroundRefresh()
                && (!gameRuntime.hasTimeControl() || !gameRuntime.isSessionInProgress())) {
            return;
        }

        GameSessionSnapshot snapshot =
                gameRuntime.tickClock(resolveElapsedMillisForRuntimePump());
        renderSnapshot(snapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
        }
    }

    private String resolveDisplayLetter(Character displayLetter) {
        return displayLetter == null ? "" : String.valueOf(displayLetter);
    }

    private void shutdownRuntime() {
        if (gameRuntime != null) {
            gameRuntime.shutdown();
            gameRuntime = null;
        }
        interactionLocked = false;
    }

    private void syncAiTurn(GameSessionSnapshot snapshot) {
        if (gameRuntime == null || !gameRuntime.hasAutomatedTurnSupport()) {
            return;
        }

        if (snapshot.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            gameRuntime.disableAutomatedTurnSupport();
            return;
        }

        if (!gameRuntime.isCurrentTurnAutomated()) {
            gameRuntime.cancelPendingAutomatedTurn();
            return;
        }

        gameRuntime.requestAutomatedTurnIfIdle(
                completion -> Platform.runLater(() -> handleAiMoveCompleted(completion)));
    }

    private void handleAiMoveCompleted(AiSessionRuntime.TurnCompletion completion) {
        if (gameRuntime == null || !gameRuntime.hasSession()) {
            return;
        }

        if (!gameRuntime.matchesAutomatedTurn(completion)) {
            return;
        }

        tickClockBeforeActionIfNeeded();
        gameRuntime.applyAutomatedTurn(completion);
        refreshSnapshotAfterAction();
    }

    @Override
    public boolean isInteractionLocked() {
        return interactionLocked;
    }

    @Override
    public void onDraftTilePlaced(String tileId, Position position) {
        if (isInteractionLocked()) {
            return;
        }
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        tickClockBeforeActionIfNeeded();
        gameRuntime.placeDraftTile(tileId, position);
        refreshSnapshotAfterAction();
    }

    @Override
    public void onDraftTileMoved(String tileId, Position position) {
        if (isInteractionLocked()) {
            return;
        }
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        tickClockBeforeActionIfNeeded();
        gameRuntime.moveDraftTile(tileId, position);
        refreshSnapshotAfterAction();
    }

    @Override
    public void onDraftTileRemoved(String tileId) {
        if (isInteractionLocked()) {
            return;
        }
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        tickClockBeforeActionIfNeeded();
        gameRuntime.removeDraftTile(tileId);
        refreshSnapshotAfterAction();
    }

    @Override
    public void onRecallAllDraftTilesRequested() {
        if (isInteractionLocked()) {
            return;
        }
        tickClockBeforeActionIfNeeded();
        gameRuntime.recallAllDraftTiles();
        refreshSnapshotAfterAction();
    }

    @Override
    public void onSubmitDraftRequested() {
        if (isInteractionLocked()) {
            return;
        }
        tickClockBeforeActionIfNeeded();
        gameRuntime.submitDraft();
        refreshSnapshotAfterAction();
    }

    @Override
    public void onSkipTurnRequested() {
        if (isInteractionLocked()) {
            return;
        }
        tickClockBeforeActionIfNeeded();
        gameRuntime.passTurn();
        refreshSnapshotAfterAction();
    }

    @Override
    public void onRearrangeRequested() {
        // Not wired yet.
    }

    @Override
    public void onResignRequested() {
        // Not wired yet.
    }

    private boolean resolveInteractionLocked(
            GameSessionSnapshot snapshot,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        if (clientRuntimeSnapshot != null && clientRuntimeSnapshot.interactionLocked()) {
            return true;
        }
        if (aiRuntimeSnapshot != null && aiRuntimeSnapshot.interactionLocked()) {
            return true;
        }
        return gameRuntime != null
                && snapshot.getSessionStatus() == SessionStatus.IN_PROGRESS
                && gameRuntime.isCurrentTurnAutomated();
    }

    private String resolveStatusSummary(
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        if (clientRuntimeSnapshot != null && !clientRuntimeSnapshot.summary().isBlank()) {
            return clientRuntimeSnapshot.summary();
        }
        return aiRuntimeSnapshot == null ? "" : aiRuntimeSnapshot.summary();
    }

    private String resolveStatusDetails(
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        if (clientRuntimeSnapshot != null && !clientRuntimeSnapshot.details().isBlank()) {
            return clientRuntimeSnapshot.details();
        }
        return aiRuntimeSnapshot == null ? "" : aiRuntimeSnapshot.details();
    }

    private long resolveElapsedMillisForRuntimePump() {
        long now = System.nanoTime();
        long elapsedMillis = Math.max(0L, (now - lastTickNanos) / 1_000_000L);
        lastTickNanos = now;
        return elapsedMillis;
    }
}
