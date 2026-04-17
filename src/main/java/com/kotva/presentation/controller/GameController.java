package com.kotva.presentation.controller;

import com.kotva.application.result.BoardCellSnapshot;
import com.kotva.application.runtime.GameRuntime;
import com.kotva.application.runtime.GameRuntimeFactory;
import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.service.GameActionResult;
import com.kotva.application.session.AiRuntimeSnapshot;
import com.kotva.application.session.BoardCellRenderSnapshot;
import com.kotva.application.session.GamePlayerSnapshot;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.PreviewPositionSnapshot;
import com.kotva.application.session.PreviewSnapshot;
import com.kotva.application.session.PreviewWordSnapshot;
import com.kotva.application.session.RackTileSnapshot;
import com.kotva.domain.model.Position;
import com.kotva.domain.action.ActionType;
import com.kotva.infrastructure.AudioManager;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.SessionStatus;
import com.kotva.policy.WordType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javafx.application.Platform;
import javafx.util.Duration;

public class GameController implements GameActionPort {
    private static final Duration POLLING_INTERVAL = Duration.millis(100);
    private static final int RACK_SLOT_COUNT = 7;

    private final SceneNavigator navigator;
    private final GameRuntimeFactory gameRuntimeFactory;
    private final AudioManager audioManager;
    private final GameLaunchContext launchContext;
    private final GameViewModel viewModel;
    private final GameDraftState draftState;
    private final String presentationClientId;
    private UiScheduler uiScheduler;
    private GameRenderer renderer;
    private GameRuntime gameRuntime;
    private long lastTickNanos;
    private long nextClientActionSequence;
    private String pendingClientActionId;
    private boolean interactionLocked;
    private boolean settlementNavigated;
    private String lastPresentedActionResultId;

    public GameController(SceneNavigator navigator, GameLaunchContext launchContext) {
        this.navigator = Objects.requireNonNull(navigator, "navigator cannot be null.");
        this.gameRuntimeFactory = navigator.getAppContext().getGameRuntimeFactory();
        this.audioManager = navigator.getAppContext().getAudioManager();
        this.launchContext = Objects.requireNonNull(launchContext, "launchContext cannot be null.");
        this.viewModel = new GameViewModel("S C R A B B L E");
        this.draftState = new GameDraftState();
        this.presentationClientId = UUID.randomUUID().toString();
        this.nextClientActionSequence = 1L;
        this.settlementNavigated = false;
        this.lastPresentedActionResultId = null;
    }

    public GameViewModel getViewModel() {
        return viewModel;
    }

    public GameDraftState getDraftState() {
        return draftState;
    }

    public String getPendingClientActionId() {
        return pendingClientActionId;
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
        Objects.requireNonNull(interactionCoordinator, "interactionCoordinator cannot be null.")
                .attach();
        startGameFromLaunchContext();
    }

    private void startGameFromLaunchContext() {
        stopPolling();
        shutdownRuntime();
        settlementNavigated = false;
        gameRuntime = gameRuntimeFactory.create(launchContext.getRequest());
        gameRuntime.start(launchContext.getRequest());
        clearClientActionTracking();

        GameSessionSnapshot firstSnapshot = gameRuntime.getSessionSnapshot();
        renderSnapshot(firstSnapshot);

        if (!gameRuntime.isSessionInProgress() || !gameRuntime.hasTimeControl()) {
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

        if (!gameRuntime.isSessionInProgress()) {
            renderSnapshot(gameRuntime.getSessionSnapshot());
            stopPolling();
            return;
        }

        long now = System.nanoTime();
        long elapsedMillis = Math.max(0L, (now - lastTickNanos) / 1_000_000L);
        lastTickNanos = now;

        GameSessionSnapshot snapshot = gameRuntime.tickClock(elapsedMillis);
        renderSnapshot(snapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
        }
    }

    private void renderSnapshot(GameSessionSnapshot snapshot) {
        AiRuntimeSnapshot aiRuntimeSnapshot = snapshot.getAiRuntimeSnapshot();
        syncClientActionTracking(snapshot);
        syncActionFeedback(snapshot);
        viewModel.setStepTimerTitle("Step Time");
        viewModel.setTotalTimerTitle("Total Time");
        viewModel.setTotalTimerText(resolveTotalTimerText(snapshot));
        viewModel.setStepTimerText(resolveStepTimerText(snapshot));
        viewModel.setWordOutline(resolveWordOutline(snapshot.getPreview()));
        viewModel.setPlayerCards(buildPlayerCards(snapshot));
        viewModel.setBoardTiles(buildBoardTiles(snapshot));
        viewModel.setRackTiles(buildRackTiles(snapshot));
        interactionLocked = resolveInteractionLocked(snapshot, aiRuntimeSnapshot);
        viewModel.setInteractionLocked(interactionLocked);
        viewModel.setAiErrorSummary(aiRuntimeSnapshot == null ? "" : aiRuntimeSnapshot.summary());
        viewModel.setAiErrorDetails(aiRuntimeSnapshot == null ? "" : aiRuntimeSnapshot.details());
        draftState.syncSnapshot(viewModel.getRackTiles(), viewModel.getBoardTiles());
        renderer.render(viewModel);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
            navigateToSettlementIfNeeded();
            return;
        }
        syncAiTurn(snapshot);
    }

    private List<GameViewModel.PlayerCardModel> buildPlayerCards(GameSessionSnapshot snapshot) {
        List<GameViewModel.PlayerCardModel> playerCards = new ArrayList<>();
        PreviewSnapshot previewSnapshot = snapshot.getPreview();
        for (GamePlayerSnapshot player : snapshot.getPlayers()) {
            playerCards.add(
                    new GameViewModel.PlayerCardModel(
                            player.getPlayerName(),
                            player.getPlayerId(),
                            player.getScore(),
                            resolveStepMarkText(player, previewSnapshot),
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

        for (RackTileSnapshot rackTile : snapshot.getCurrentRackTiles()) {
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
        Map<BoardCoordinate, BoardCellRenderSnapshot> renderCellsByCoordinate = new HashMap<>();
        for (BoardCellRenderSnapshot boardCell : snapshot.getBoardCells()) {
            renderCellsByCoordinate.put(
                    new BoardCoordinate(boardCell.getRow(), boardCell.getCol()), boardCell);
        }

        List<GameViewModel.BoardTileModel> boardTiles = new ArrayList<>();
        for (BoardCellSnapshot boardCell : snapshot.getBoardSnapshot().getCells()) {
            BoardCoordinate coordinate = new BoardCoordinate(boardCell.getRow(), boardCell.getCol());
            BoardCellRenderSnapshot renderCell = renderCellsByCoordinate.get(coordinate);
            GameViewModel.TileModel tileModel =
                    renderCell == null
                            ? GameViewModel.TileModel.empty()
                            : GameViewModel.TileModel.filled(
                                    renderCell.getTileId(),
                                    resolveDisplayLetter(renderCell.getDisplayLetter()),
                                    renderCell.getScore());
            boardTiles.add(
                    new GameViewModel.BoardTileModel(
                            coordinate,
                            tileModel,
                            boardCell.getBonusType(),
                            renderCell != null && renderCell.isDraft(),
                            renderCell != null && renderCell.isPreviewValid(),
                            renderCell != null && renderCell.isPreviewInvalid(),
                            renderCell != null && renderCell.isMainWordHighlighted(),
                            renderCell != null && renderCell.isCrossWordHighlighted(),
                            renderCell != null
                                    && renderCell.isMainWordHighlighted()
                                    && snapshot.getPreview() != null
                                    && snapshot.getPreview().isValid(),
                            renderCell != null
                                    && renderCell.isMainWordHighlighted()
                                    && snapshot.getPreview() != null
                                    && !snapshot.getPreview().isValid()));
        }
        return boardTiles;
    }

    private String resolveStepMarkText(GamePlayerSnapshot player, PreviewSnapshot previewSnapshot) {
        Objects.requireNonNull(player, "player cannot be null.");
        if (!player.isCurrentTurn() || previewSnapshot == null) {
            return "--";
        }
        return Integer.toString(previewSnapshot.isValid() ? previewSnapshot.getEstimatedScore() : 0);
    }

    private GameViewModel.WordOutlineModel resolveWordOutline(PreviewSnapshot previewSnapshot) {
        if (previewSnapshot == null) {
            return null;
        }

        PreviewWordSnapshot mainWord = findMainWord(previewSnapshot);
        if (mainWord == null || mainWord.getCoveredPositions().isEmpty()) {
            return null;
        }

        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (PreviewPositionSnapshot coveredPosition : mainWord.getCoveredPositions()) {
            minRow = Math.min(minRow, coveredPosition.getRow());
            maxRow = Math.max(maxRow, coveredPosition.getRow());
            minCol = Math.min(minCol, coveredPosition.getCol());
            maxCol = Math.max(maxCol, coveredPosition.getCol());
        }

        return new GameViewModel.WordOutlineModel(
                minRow, minCol, maxRow, maxCol, previewSnapshot.isValid());
    }

    private PreviewWordSnapshot findMainWord(PreviewSnapshot previewSnapshot) {
        for (PreviewWordSnapshot previewWord : previewSnapshot.getWords()) {
            if (previewWord.getWordType() == WordType.MAIN_WORD) {
                return previewWord;
            }
        }
        return null;
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
        if (gameRuntime == null || !gameRuntime.hasTimeControl() || !gameRuntime.isSessionInProgress()) {
            return;
        }

        long now = System.nanoTime();
        long elapsedMillis = Math.max(0L, (now - lastTickNanos) / 1_000_000L);
        lastTickNanos = now;
        if (elapsedMillis > 0L) {
            gameRuntime.tickClock(elapsedMillis);
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
        clearClientActionTracking();
        interactionLocked = false;
    }

    private void navigateToSettlementIfNeeded() {
        if (settlementNavigated) {
            return;
        }
        settlementNavigated = true;
        Platform.runLater(navigator::showSettlement);
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
        audioManager.playTilePlace();
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
        audioManager.playTilePlace();
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
        audioManager.playTileRecall();
        refreshSnapshotAfterAction();
    }

    @Override
    public void onRecallAllDraftTilesRequested() {
        if (isInteractionLocked()) {
            return;
        }
        tickClockBeforeActionIfNeeded();
        gameRuntime.recallAllDraftTiles();
        audioManager.playTileRecall();
        refreshSnapshotAfterAction();
    }

    @Override
    public void onSubmitDraftRequested() {
        if (isInteractionLocked()) {
            return;
        }
        tickClockBeforeActionIfNeeded();
        gameRuntime.submitDraft(trackPendingClientAction(nextClientActionId("submit-draft")));
        refreshSnapshotAfterAction();
    }

    @Override
    public void onSkipTurnRequested() {
        if (isInteractionLocked()) {
            return;
        }
        tickClockBeforeActionIfNeeded();
        gameRuntime.passTurn(trackPendingClientAction(nextClientActionId("pass-turn")));
        refreshSnapshotAfterAction();
    }

    @Override
    public void onRearrangeRequested() {
        if (isInteractionLocked()) {
            return;
        }
        if (draftState.rearrangeRackTiles() && renderer != null) {
            renderer.refresh();
        }
    }

    @Override
    public void onResignRequested() {
        if (isInteractionLocked()) {
            return;
        }
        tickClockBeforeActionIfNeeded();
        gameRuntime.resign(trackPendingClientAction(nextClientActionId("resign")));
        refreshSnapshotAfterAction();
    }

    private boolean resolveInteractionLocked(
            GameSessionSnapshot snapshot, AiRuntimeSnapshot aiRuntimeSnapshot) {
        if (aiRuntimeSnapshot != null && aiRuntimeSnapshot.interactionLocked()) {
            return true;
        }
        return gameRuntime != null
                && snapshot.getSessionStatus() == SessionStatus.IN_PROGRESS
                && gameRuntime.isCurrentTurnAutomated();
    }

    private String nextClientActionId(String actionName) {
        Objects.requireNonNull(actionName, "actionName cannot be null.");
        return presentationClientId + ":" + actionName + ":" + nextClientActionSequence++;
    }

    private String trackPendingClientAction(String clientActionId) {
        pendingClientActionId =
                Objects.requireNonNull(clientActionId, "clientActionId cannot be null.");
        return clientActionId;
    }

    private void syncClientActionTracking(GameSessionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        GameActionResult latestActionResult = snapshot.getLatestActionResult();
        if (latestActionResult == null || latestActionResult.getClientActionId() == null) {
            return;
        }

        if (!Objects.equals(latestActionResult.getClientActionId(), pendingClientActionId)) {
            return;
        }

        pendingClientActionId = null;
    }

    private void syncActionFeedback(GameSessionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        GameActionResult latestActionResult = snapshot.getLatestActionResult();
        if (latestActionResult == null) {
            return;
        }

        String actionId = latestActionResult.getActionId();
        if (actionId == null || Objects.equals(actionId, lastPresentedActionResultId)) {
            return;
        }

        lastPresentedActionResultId = actionId;
        if (shouldPlayActionConfirm(latestActionResult)) {
            audioManager.playActionConfirm();
        }
        if (!latestActionResult.isSuccess() && !latestActionResult.getMessage().isBlank()) {
            viewModel.pushTransientMessage(latestActionResult.getMessage());
        }
    }

    private boolean shouldPlayActionConfirm(GameActionResult latestActionResult) {
        Objects.requireNonNull(latestActionResult, "latestActionResult cannot be null.");
        if (!latestActionResult.isSuccess()) {
            return false;
        }
        if (latestActionResult.getActionType() != ActionType.PLACE_TILE) {
            return false;
        }

        String clientActionId = latestActionResult.getClientActionId();
        return clientActionId != null && clientActionId.startsWith(presentationClientId + ":");
    }

    private void clearClientActionTracking() {
        pendingClientActionId = null;
        lastPresentedActionResultId = null;
    }
}
