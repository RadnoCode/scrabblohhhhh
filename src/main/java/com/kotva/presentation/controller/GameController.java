package com.kotva.presentation.controller;

import com.kotva.application.result.BoardCellSnapshot;
import com.kotva.application.runtime.GameRuntime;
import com.kotva.application.runtime.GameRuntimeFactory;
import com.kotva.application.runtime.TutorialRuntimeFactory;
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
import com.kotva.application.session.TutorialGhostTileSnapshot;
import com.kotva.application.session.TutorialSnapshot;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.model.Position;
import com.kotva.infrastructure.AudioManager;
import com.kotva.infrastructure.settings.AppSettings;
import com.kotva.infrastructure.settings.SettingsRepository;
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
import com.kotva.presentation.viewmodel.LaunchKind;
import com.kotva.tutorial.TutorialActionKey;
import com.kotva.tutorial.TutorialUiEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javafx.application.Platform;
import javafx.util.Duration;

public class GameController implements GameActionPort {
    private static final Duration POLLING_INTERVAL = Duration.millis(100);
    private static final int RACK_SLOT_COUNT = 7;

    private final SceneNavigator navigator;
    private final GameRuntimeFactory gameRuntimeFactory;
    private final TutorialRuntimeFactory tutorialRuntimeFactory;
    private final SettingsRepository settingsRepository;
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
    private boolean tutorialCompletionPersisted;
    private String lastPresentedActionResultId;

    public GameController(SceneNavigator navigator, GameLaunchContext launchContext) {
        this.navigator = Objects.requireNonNull(navigator, "navigator cannot be null.");
        this.gameRuntimeFactory = navigator.getAppContext().getGameRuntimeFactory();
        this.tutorialRuntimeFactory = navigator.getAppContext().getTutorialRuntimeFactory();
        this.settingsRepository = navigator.getAppContext().getSettingsRepository();
        this.audioManager = navigator.getAppContext().getAudioManager();
        this.launchContext = Objects.requireNonNull(launchContext, "launchContext cannot be null.");
        this.viewModel = new GameViewModel("S C R A B B L E");
        this.draftState = new GameDraftState();
        this.presentationClientId = UUID.randomUUID().toString();
        this.nextClientActionSequence = 1L;
        this.settlementNavigated = false;
        this.tutorialCompletionPersisted = false;
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
        tutorialCompletionPersisted = false;

        if (launchContext.getLaunchKind() == LaunchKind.TUTORIAL) {
            gameRuntime = tutorialRuntimeFactory.create(launchContext.getTutorialScriptId());
            gameRuntime.start(null);
        } else {
            gameRuntime = gameRuntimeFactory.create(launchContext.getRequest());
            gameRuntime.start(launchContext.getRequest());
        }
        clearClientActionTracking();

        GameSessionSnapshot firstSnapshot = gameRuntime.getSessionSnapshot();
        if (gameRuntime.isSessionInProgress() && gameRuntime.hasTimeControl()) {
            // Prime the clock baseline before the initial render can trigger an AI move.
            lastTickNanos = System.nanoTime();
        } else {
            lastTickNanos = 0L;
        }
        renderSnapshot(firstSnapshot);

        if (!gameRuntime.isSessionInProgress() || !gameRuntime.hasTimeControl()) {
            return;
        }

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
        long elapsedMillis = calculateElapsedMillis(lastTickNanos, now);
        lastTickNanos = now;

        GameSessionSnapshot snapshot = gameRuntime.tickClock(elapsedMillis);
        renderSnapshot(snapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
        }
    }

    private void renderSnapshot(GameSessionSnapshot snapshot) {
        AiRuntimeSnapshot aiRuntimeSnapshot = snapshot.getAiRuntimeSnapshot();
        TutorialSnapshot tutorialSnapshot = snapshot.getTutorial();
        syncClientActionTracking(snapshot);
        syncActionFeedback(snapshot);
        viewModel.setStepTimerTitle("Step Time");
        viewModel.setTotalTimerTitle("Total Time");
        viewModel.setTotalTimerText(resolveTotalTimerText(snapshot));
        viewModel.setStepTimerText(resolveStepTimerText(snapshot));
        viewModel.setWordOutline(resolveWordOutline(snapshot.getPreview()));
        viewModel.setPreviewPanel(resolvePreviewPanel(snapshot.getPreview()));
        viewModel.setTutorialOverlay(resolveTutorialOverlay(tutorialSnapshot));
        viewModel.setPlayerCards(buildPlayerCards(snapshot));
        viewModel.setBoardTiles(buildBoardTiles(snapshot, tutorialSnapshot));
        viewModel.setRackTiles(buildRackTiles(snapshot, tutorialSnapshot));
        viewModel.setActionPanel(resolveActionPanel(tutorialSnapshot));
        interactionLocked = resolveInteractionLocked(snapshot, aiRuntimeSnapshot, tutorialSnapshot);
        viewModel.setInteractionLocked(interactionLocked);
        viewModel.setAiErrorSummary(aiRuntimeSnapshot == null ? "" : aiRuntimeSnapshot.summary());
        viewModel.setAiErrorDetails(aiRuntimeSnapshot == null ? "" : aiRuntimeSnapshot.details());
        draftState.syncSnapshot(viewModel.getRackTiles(), viewModel.getBoardTiles());
        renderer.render(viewModel);
        persistTutorialCompletionIfNeeded(tutorialSnapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED && !gameRuntime.isTutorialRuntime()) {
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

    private List<GameViewModel.TileModel> buildRackTiles(
        GameSessionSnapshot snapshot,
        TutorialSnapshot tutorialSnapshot) {
        Set<Integer> highlightedSlots = tutorialSnapshot == null
            ? Set.of()
            : new HashSet<>(tutorialSnapshot.getHighlightedRackSlots());
        boolean dimNonTargets = tutorialSnapshot != null && tutorialSnapshot.isDimNonTargetRackSlots();

        List<GameViewModel.TileModel> rackTiles = new ArrayList<>();
        for (int index = 0; index < RACK_SLOT_COUNT; index++) {
            boolean highlighted = highlightedSlots.contains(index);
            boolean dimmed = dimNonTargets && !highlighted;
            rackTiles.add(GameViewModel.TileModel.empty(highlighted, dimmed));
        }

        for (RackTileSnapshot rackTile : snapshot.getCurrentRackTiles()) {
            int slotIndex = rackTile.getSlotIndex();
            if (slotIndex < 0 || slotIndex >= rackTiles.size()) {
                continue;
            }
            boolean highlighted = highlightedSlots.contains(slotIndex);
            boolean dimmed = dimNonTargets && !highlighted;
            if (rackTile.getTileId() == null) {
                rackTiles.set(slotIndex, GameViewModel.TileModel.empty(highlighted, dimmed));
                continue;
            }
            rackTiles.set(
                slotIndex,
                GameViewModel.TileModel.filled(
                    rackTile.getTileId(),
                    resolveDisplayLetter(rackTile.getDisplayLetter()),
                    rackTile.getScore(),
                    highlighted,
                    dimmed));
        }
        return rackTiles;
    }

    private List<GameViewModel.BoardTileModel> buildBoardTiles(
        GameSessionSnapshot snapshot,
        TutorialSnapshot tutorialSnapshot) {
        Set<BoardCoordinate> highlightedCoordinates = collectHighlightedCoordinates(tutorialSnapshot);
        Map<BoardCoordinate, GameViewModel.TileModel> ghostTiles = collectGhostTiles(tutorialSnapshot);
        boolean dimNonTargets = tutorialSnapshot != null && tutorialSnapshot.isDimNonTargetBoardCells();

        Map<BoardCoordinate, BoardCellRenderSnapshot> renderCellsByCoordinate = new HashMap<>();
        for (BoardCellRenderSnapshot boardCell : snapshot.getBoardCells()) {
            renderCellsByCoordinate.put(
                new BoardCoordinate(boardCell.getRow(), boardCell.getCol()),
                boardCell);
        }

        List<GameViewModel.BoardTileModel> boardTiles = new ArrayList<>();
        for (BoardCellSnapshot boardCell : snapshot.getBoardSnapshot().getCells()) {
            BoardCoordinate coordinate = new BoardCoordinate(boardCell.getRow(), boardCell.getCol());
            BoardCellRenderSnapshot renderCell = renderCellsByCoordinate.get(coordinate);
            boolean tutorialHighlighted = highlightedCoordinates.contains(coordinate);
            boolean tutorialDimmed = dimNonTargets && !tutorialHighlighted;
            GameViewModel.TileModel tileModel =
                renderCell == null
                    ? GameViewModel.TileModel.empty(false, false)
                    : GameViewModel.TileModel.filled(
                        renderCell.getTileId(),
                        resolveDisplayLetter(renderCell.getDisplayLetter()),
                        renderCell.getScore(),
                        false,
                        false);
            GameViewModel.TileModel ghostTile =
                renderCell == null ? ghostTiles.get(coordinate) : null;

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
                        && !snapshot.getPreview().isValid(),
                    ghostTile,
                    tutorialHighlighted,
                    tutorialDimmed));
        }
        return boardTiles;
    }

    private Set<BoardCoordinate> collectHighlightedCoordinates(TutorialSnapshot tutorialSnapshot) {
        Set<BoardCoordinate> highlightedCoordinates = new HashSet<>();
        if (tutorialSnapshot == null) {
            return highlightedCoordinates;
        }
        for (PreviewPositionSnapshot position : tutorialSnapshot.getHighlightedBoardPositions()) {
            highlightedCoordinates.add(new BoardCoordinate(position.getRow(), position.getCol()));
        }
        return highlightedCoordinates;
    }

    private Map<BoardCoordinate, GameViewModel.TileModel> collectGhostTiles(TutorialSnapshot tutorialSnapshot) {
        Map<BoardCoordinate, GameViewModel.TileModel> ghostTiles = new HashMap<>();
        if (tutorialSnapshot == null) {
            return ghostTiles;
        }
        for (TutorialGhostTileSnapshot ghostTile : tutorialSnapshot.getGhostTiles()) {
            ghostTiles.put(
                new BoardCoordinate(ghostTile.getRow(), ghostTile.getCol()),
                GameViewModel.TileModel.filled("", ghostTile.getLetter(), ghostTile.getScore()));
        }
        return ghostTiles;
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

    private GameViewModel.PreviewPanelModel resolvePreviewPanel(PreviewSnapshot previewSnapshot) {
        if (previewSnapshot == null) {
            return GameViewModel.PreviewPanelModel.hidden();
        }

        PreviewWordSnapshot mainWord = findMainWord(previewSnapshot);
        String mainWordText = mainWord == null ? "主词：--" : "主词：" + mainWord.getWord();
        List<String> messages = previewSnapshot.getMessages().isEmpty()
            ? List.of(previewSnapshot.isValid() ? "当前落子合法。" : "当前落子不合法。")
            : previewSnapshot.getMessages();
        return new GameViewModel.PreviewPanelModel(
            true,
            previewSnapshot.isValid(),
            previewSnapshot.isValid() ? "合法" : "不合法",
            "预估分数：" + previewSnapshot.getEstimatedScore(),
            mainWordText,
            messages);
    }

    private GameViewModel.TutorialOverlayModel resolveTutorialOverlay(TutorialSnapshot tutorialSnapshot) {
        if (tutorialSnapshot == null) {
            return GameViewModel.TutorialOverlayModel.hidden();
        }
        return new GameViewModel.TutorialOverlayModel(
            true,
            "步骤 " + tutorialSnapshot.getStepNumber() + " / " + tutorialSnapshot.getStepCount(),
            tutorialSnapshot.getTitle(),
            tutorialSnapshot.getBody(),
            tutorialSnapshot.isTapToContinue(),
            tutorialSnapshot.isShowExitButton(),
            tutorialSnapshot.isShowReturnHomeButton());
    }

    private GameViewModel.ActionPanelModel resolveActionPanel(TutorialSnapshot tutorialSnapshot) {
        if (tutorialSnapshot == null) {
            return GameViewModel.ActionPanelModel.defaultState();
        }
        Set<TutorialActionKey> enabled = new HashSet<>(tutorialSnapshot.getEnabledActions());
        Set<TutorialActionKey> highlighted = new HashSet<>(tutorialSnapshot.getHighlightedActions());
        return new GameViewModel.ActionPanelModel(
            GameViewModel.ActionButtonModel.of(
                enabled.contains(TutorialActionKey.SKIP),
                highlighted.contains(TutorialActionKey.SKIP)),
            GameViewModel.ActionButtonModel.of(
                enabled.contains(TutorialActionKey.REARRANGE),
                highlighted.contains(TutorialActionKey.REARRANGE)),
            GameViewModel.ActionButtonModel.of(
                enabled.contains(TutorialActionKey.RECALL),
                highlighted.contains(TutorialActionKey.RECALL)),
            GameViewModel.ActionButtonModel.of(
                enabled.contains(TutorialActionKey.RESIGN),
                highlighted.contains(TutorialActionKey.RESIGN)),
            GameViewModel.ActionButtonModel.of(
                enabled.contains(TutorialActionKey.SUBMIT),
                highlighted.contains(TutorialActionKey.SUBMIT)));
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
        long elapsedMillis = calculateElapsedMillis(lastTickNanos, now);
        lastTickNanos = now;
        if (elapsedMillis > 0L) {
            gameRuntime.tickClock(elapsedMillis);
        }
    }

    static long calculateElapsedMillis(long previousTickNanos, long currentTickNanos) {
        if (previousTickNanos <= 0L || currentTickNanos <= previousTickNanos) {
            return 0L;
        }
        return (currentTickNanos - previousTickNanos) / 1_000_000L;
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
        lastTickNanos = 0L;
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
        GameActionResult latestBeforeAction = latestActionResult();
        tickClockBeforeActionIfNeeded();
        gameRuntime.placeDraftTile(tileId, position);
        if (!wasRejected(latestBeforeAction)) {
            audioManager.playTilePlace();
        }
        refreshSnapshotAfterAction();
    }

    @Override
    public void onDraftTileMoved(String tileId, Position position) {
        if (isInteractionLocked()) {
            return;
        }
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        GameActionResult latestBeforeAction = latestActionResult();
        tickClockBeforeActionIfNeeded();
        gameRuntime.moveDraftTile(tileId, position);
        if (!wasRejected(latestBeforeAction)) {
            audioManager.playTilePlace();
        }
        refreshSnapshotAfterAction();
    }

    @Override
    public void onDraftTileRemoved(String tileId) {
        if (isInteractionLocked()) {
            return;
        }
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        GameActionResult latestBeforeAction = latestActionResult();
        tickClockBeforeActionIfNeeded();
        gameRuntime.removeDraftTile(tileId);
        if (!wasRejected(latestBeforeAction)) {
            audioManager.playTileRecall();
        }
        refreshSnapshotAfterAction();
    }

    @Override
    public void onRecallAllDraftTilesRequested() {
        if (isInteractionLocked()) {
            return;
        }
        GameActionResult latestBeforeAction = latestActionResult();
        tickClockBeforeActionIfNeeded();
        gameRuntime.recallAllDraftTiles();
        if (!wasRejected(latestBeforeAction)) {
            audioManager.playTileRecall();
        }
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
        boolean rearranged = draftState.rearrangeRackTiles();
        if (rearranged && renderer != null) {
            audioManager.playActionConfirm();
        }
        if (rearranged && gameRuntime != null && gameRuntime.isTutorialRuntime()) {
            gameRuntime.recordTutorialEvent(TutorialUiEvent.REARRANGE_USED);
            refreshSnapshotAfterAction();
            return;
        }
        if (rearranged && renderer != null) {
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

    @Override
    public void onTutorialAdvanceRequested() {
        if (gameRuntime == null || !gameRuntime.isTutorialRuntime()) {
            return;
        }
        gameRuntime.advanceTutorialInstruction();
        refreshSnapshotAfterAction();
    }

    @Override
    public void onTutorialExitRequested() {
        navigator.showHome();
    }

    @Override
    public void onTutorialReturnHomeRequested() {
        persistTutorialCompleted();
        navigator.showHome();
    }

    private boolean resolveInteractionLocked(
        GameSessionSnapshot snapshot,
        AiRuntimeSnapshot aiRuntimeSnapshot,
        TutorialSnapshot tutorialSnapshot) {
        if (aiRuntimeSnapshot != null && aiRuntimeSnapshot.interactionLocked()) {
            return true;
        }
        if (tutorialSnapshot != null
            && (tutorialSnapshot.isTapToContinue() || tutorialSnapshot.isShowReturnHomeButton())) {
            return true;
        }
        return gameRuntime != null
            && snapshot.getSessionStatus() == SessionStatus.IN_PROGRESS
            && gameRuntime.isCurrentTurnAutomated();
    }

    private void persistTutorialCompletionIfNeeded(TutorialSnapshot tutorialSnapshot) {
        if (tutorialSnapshot == null
            || !tutorialSnapshot.isShowReturnHomeButton()
            || tutorialCompletionPersisted) {
            return;
        }
        persistTutorialCompleted();
    }

    private void persistTutorialCompleted() {
        AppSettings settings = settingsRepository.load();
        settingsRepository.save(
            new AppSettings(
                settings.getMusicVolume(),
                settings.getSfxVolume(),
                true,
                true));
        tutorialCompletionPersisted = true;
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

    private GameActionResult latestActionResult() {
        return gameRuntime != null && gameRuntime.hasSession()
            ? gameRuntime.getSession().getLatestActionResult()
            : null;
    }

    private boolean wasRejected(GameActionResult latestBeforeAction) {
        GameActionResult latestAfterAction = latestActionResult();
        return latestAfterAction != null
            && latestAfterAction != latestBeforeAction
            && !latestAfterAction.isSuccess();
    }
}
