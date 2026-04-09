package com.kotva.presentation.controller;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.session.BoardCellRenderSnapshot;
import com.kotva.application.session.GamePlayerSnapshot;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.RackTileSnapshot;
import com.kotva.domain.model.Position;
import com.kotva.launcher.AppContext;
import com.kotva.mode.PlayerController;
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
import javafx.util.Duration;

/**
 * GameController 是游戏页总控。
 * 它负责启动对局、轮询快照、组装 ViewModel，并接收 UI 动作。
 */
public class GameController implements GameActionPort {
    private static final Duration POLLING_INTERVAL = Duration.millis(100);
    private static final int RACK_SLOT_COUNT = 7;

    private final AppContext appContext;
    private final GameApplicationService gameApplicationService;
    private final GameLaunchContext launchContext;
    private final GameViewModel viewModel;
    private final GameDraftState draftState;
    private UiScheduler uiScheduler;
    private GameRenderer renderer;
    private GameSession session;
    private long lastTickNanos;

    public GameController(SceneNavigator navigator, GameLaunchContext launchContext) {
        Objects.requireNonNull(navigator, "navigator cannot be null.");
        this.appContext = navigator.getAppContext();
        this.gameApplicationService = appContext.getGameApplicationService();
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
        return session;
    }

    public boolean hasSession() {
        return session != null;
    }

    public void refreshFromCurrentSession() {
        if (session == null || renderer == null) {
            return;
        }
        renderSnapshot(gameApplicationService.getSessionSnapshot(session));
    }

    public void bind(GameRenderer renderer, GameInteractionCoordinator interactionCoordinator) {
        this.renderer = Objects.requireNonNull(renderer, "renderer cannot be null.");
        Objects.requireNonNull(interactionCoordinator, "interactionCoordinator cannot be null.").attach();
        startGameFromLaunchContext();
    }

    private void startGameFromLaunchContext() {
        stopPolling();
        session = appContext.getGameSetupService().startNewGame(launchContext.getRequest());

        GameSessionSnapshot firstSnapshot = gameApplicationService.getSessionSnapshot(session);
        renderSnapshot(firstSnapshot);

        if (!session.getConfig().hasTimeControl()) {
            return;
        }

        lastTickNanos = System.nanoTime();
        uiScheduler = new UiScheduler(POLLING_INTERVAL, this::pollSnapshot);
        uiScheduler.start();
    }

    private void pollSnapshot() {
        if (session == null) {
            return;
        }

        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            renderSnapshot(gameApplicationService.getSessionSnapshot(session));
            stopPolling();
            return;
        }

        long now = System.nanoTime();
        long elapsedMillis = Math.max(0L, (now - lastTickNanos) / 1_000_000L);
        lastTickNanos = now;

        GameSessionSnapshot snapshot = gameApplicationService.tickClock(session, elapsedMillis);
        renderSnapshot(snapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
        }
    }

    private void renderSnapshot(GameSessionSnapshot snapshot) {
        viewModel.setStepTimerTitle("Step Time");
        viewModel.setTotalTimerTitle("Total Time");
        viewModel.setTotalTimerText(resolveTotalTimerText(snapshot));
        viewModel.setStepTimerText(resolveStepTimerText(snapshot));
        viewModel.setPlayerCards(buildPlayerCards(snapshot));
        viewModel.setBoardTiles(buildBoardTiles(snapshot));
        viewModel.setRackTiles(buildRackTiles(snapshot));
        draftState.syncSnapshot(viewModel.getRackTiles(), viewModel.getBoardTiles());
        renderer.render(viewModel);
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

    private PlayerController requireCurrentPlayerController() {
        Objects.requireNonNull(session, "session cannot be null.");
        return Objects.requireNonNull(
                session.getGameState().requireCurrentActivePlayer().getController(),
                "current player controller cannot be null.");
    }

    private void refreshSnapshotAfterAction() {
        GameSessionSnapshot snapshot = gameApplicationService.getSessionSnapshot(session);
        renderSnapshot(snapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
        }
    }

    private void tickClockBeforeActionIfNeeded() {
        if (session == null
                || !session.getConfig().hasTimeControl()
                || session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            return;
        }

        long now = System.nanoTime();
        long elapsedMillis = Math.max(0L, (now - lastTickNanos) / 1_000_000L);
        lastTickNanos = now;
        if (elapsedMillis > 0L) {
            gameApplicationService.tickClock(session, elapsedMillis);
        }
    }

    private String resolveDisplayLetter(Character displayLetter) {
        return displayLetter == null ? "" : String.valueOf(displayLetter);
    }

    @Override
    public void onDraftTilePlaced(String tileId, Position position) {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        tickClockBeforeActionIfNeeded();
        requireCurrentPlayerController().placeDraftTile(gameApplicationService, session, tileId, position);
        refreshSnapshotAfterAction();
    }

    @Override
    public void onDraftTileMoved(String tileId, Position position) {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        tickClockBeforeActionIfNeeded();
        requireCurrentPlayerController().moveDraftTile(gameApplicationService, session, tileId, position);
        refreshSnapshotAfterAction();
    }

    @Override
    public void onDraftTileRemoved(String tileId) {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        tickClockBeforeActionIfNeeded();
        requireCurrentPlayerController().removeDraftTile(gameApplicationService, session, tileId);
        refreshSnapshotAfterAction();
    }

    @Override
    public void onRecallAllDraftTilesRequested() {
        tickClockBeforeActionIfNeeded();
        requireCurrentPlayerController().recallAllDraftTiles(gameApplicationService, session);
        refreshSnapshotAfterAction();
    }

    @Override
    public void onSubmitDraftRequested() {
        tickClockBeforeActionIfNeeded();
        requireCurrentPlayerController().submitDraft(gameApplicationService, session);
        refreshSnapshotAfterAction();
    }

    @Override
    public void onSkipTurnRequested() {
        tickClockBeforeActionIfNeeded();
        requireCurrentPlayerController().passTurn(gameApplicationService, session);
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
}
