package com.kotva.presentation.controller;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.PlayerClockSnapshot;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TilePlacement;
import com.kotva.policy.SessionStatus;
import com.kotva.launcher.AppContext;
import com.kotva.policy.ClockPhase;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.fx.UiScheduler;
import com.kotva.presentation.integration.GameDraftBridge;
import com.kotva.presentation.integration.NoOpGameDraftBridge;
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

    private final AppContext appContext;
    private final GameApplicationService gameApplicationService;
    private final GameLaunchContext launchContext;
    private final GameViewModel viewModel;
    private final GameDraftState draftState;
    private final GameDraftBridge gameDraftBridge;
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
        this.gameDraftBridge = new NoOpGameDraftBridge();
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

    public List<TilePlacement> getDraftPlacementsSnapshot() {
        return draftState.toTilePlacements();
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
        // 重新进页面时，先确保之前的轮询被停掉。
        stopPolling();
        // 根据 setup 页参数正式创建一局新游戏。
        session = appContext.getGameSetupService().startNewGame(launchContext.getRequest());

        // 首帧只拿快照，不推进时钟。
        GameSessionSnapshot firstSnapshot = gameApplicationService.getSessionSnapshot(session);
        renderSnapshot(firstSnapshot);

        // 无计时模式不需要起轮询器。
        if (!session.getConfig().hasTimeControl()) {
            return;
        }

        // 记录本地时间戳，后续每次 tick 都按真实耗时推进。
        lastTickNanos = System.nanoTime();
        uiScheduler = new UiScheduler(POLLING_INTERVAL, this::pollSnapshot);
        uiScheduler.start();
    }

    private void pollSnapshot() {
        if (session == null) {
            return;
        }

        // 对局结束后只再刷新一次终态，然后停止轮询。
        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            renderSnapshot(gameApplicationService.getSessionSnapshot(session));
            stopPolling();
            return;
        }

        // 严格按文档要求，用真实 elapsedMillis 推进时钟。
        long now = System.nanoTime();
        long elapsedMillis = Math.max(0L, (now - lastTickNanos) / 1_000_000L);
        lastTickNanos = now;

        // tickClock 返回的新快照直接作为 UI 真相源。
        GameSessionSnapshot snapshot = gameApplicationService.tickClock(session, elapsedMillis);
        renderSnapshot(snapshot);
        if (snapshot.getSessionStatus() == SessionStatus.COMPLETED) {
            stopPolling();
        }
    }

    private void renderSnapshot(GameSessionSnapshot snapshot) {
        // 先写时钟文案。
        viewModel.setStepTimerTitle("Step Time");
        viewModel.setTotalTimerTitle("Total Time");
        viewModel.setTotalTimerText(resolveTotalTimerText(snapshot));
        viewModel.setStepTimerText(resolveStepTimerText(snapshot));
        // 再写玩家、棋盘和牌架数据。
        viewModel.setPlayerCards(buildPlayerCards(snapshot));
        viewModel.setBoardTiles(buildBoardTiles());
        viewModel.setRackTiles(buildRackTiles(snapshot));
        // 最后统一交给 renderer 投到界面上。
        renderer.render(viewModel);
    }

    private List<GameViewModel.PlayerCardModel> buildPlayerCards(GameSessionSnapshot snapshot) {
        List<GameViewModel.PlayerCardModel> playerCards = new ArrayList<>();

        // 玩家卡片优先从 snapshot 取当前行动与时钟相关信息，再补充分数。
        for (PlayerClockSnapshot playerClockSnapshot : snapshot.getPlayerClockSnapshots()) {
            Player player = session.getGameState().getPlayerById(playerClockSnapshot.getPlayerId());
            playerCards.add(new GameViewModel.PlayerCardModel(
                    playerClockSnapshot.getPlayerName(),
                    playerClockSnapshot.getPlayerId(),
                    player != null ? player.getScore() : 0,
                    playerClockSnapshot.getPlayerId().equals(snapshot.getCurrentPlayerId()),
                    playerClockSnapshot.isActive()));
        }

        return playerCards;
    }

    private List<GameViewModel.TileModel> buildRackTiles(GameSessionSnapshot snapshot) {
        // 当前牌架始终显示当前行动玩家。
        Player currentPlayer = session.getGameState().getPlayerById(snapshot.getCurrentPlayerId());
        if (currentPlayer == null) {
            currentPlayer = session.getGameState().getCurrentPlayer();
        }

        List<GameViewModel.TileModel> rackTiles = new ArrayList<>();
        for (RackSlot rackSlot : currentPlayer.getRack().getSlots()) {
            // 空槽位直接映射为空 TileModel。
            if (rackSlot.isEmpty()) {
                rackTiles.add(GameViewModel.TileModel.empty());
                continue;
            }

            // 空白牌如果已指定字母，则优先显示 assignedLetter。
            Tile tile = rackSlot.getTile();
            char displayLetter = tile.isBlank() && tile.getAssignedLetter() != null
                    ? tile.getAssignedLetter()
                    : tile.getLetter();
            rackTiles.add(GameViewModel.TileModel.filled(
                    tile.getTileID(),
                    String.valueOf(displayLetter),
                    tile.getScore()));
        }
        return rackTiles;
    }

    private List<GameViewModel.BoardTileModel> buildBoardTiles() {
        List<GameViewModel.BoardTileModel> boardTiles = new ArrayList<>();

        // 把棋盘上已经正式存在的 Tile 全部投影成前端模型。
        for (int row = 0; row < 15; row++) {
            for (int column = 0; column < 15; column++) {
                Cell cell = session.getGameState().getBoard().getCell(new Position(row, column));
                if (cell.isEmpty()) {
                    continue;
                }

                Tile tile = cell.getPlacedTile();
                char displayLetter = tile.isBlank() && tile.getAssignedLetter() != null
                        ? tile.getAssignedLetter()
                        : tile.getLetter();
                boardTiles.add(new GameViewModel.BoardTileModel(
                        new BoardCoordinate(row, column),
                        GameViewModel.TileModel.filled(
                                tile.getTileID(),
                                String.valueOf(displayLetter),
                                tile.getScore()),
                        false));
            }
        }

        return boardTiles;
    }

    private String resolveTotalTimerText(GameSessionSnapshot snapshot) {
        // 没有计时规则时统一显示占位文本。
        if (snapshot.getCurrentPlayerClockPhase() == ClockPhase.DISABLED) {
            return "--:--";
        }
        return formatDuration(snapshot.getCurrentPlayerMainTimeRemainingMillis());
    }

    private String resolveStepTimerText(GameSessionSnapshot snapshot) {
        // 只有处于 BYO_YOMI 阶段才显示步时。
        if (snapshot.getCurrentPlayerClockPhase() != ClockPhase.BYO_YOMI) {
            return "--:--";
        }
        return formatDuration(snapshot.getCurrentPlayerByoYomiRemainingMillis());
    }

    private String formatDuration(long millis) {
        // 先把毫秒值保护到非负范围。
        long safeMillis = Math.max(0L, millis);
        if (safeMillis == 0L) {
            return "00:00";
        }

        // 向上取整到秒，避免界面视觉上过早少一秒。
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

    @Override
    public void onDraftTilePlaced(String tileId, Position position) {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        gameDraftBridge.placeDraftTile(session, tileId, position);
    }

    @Override
    public void onDraftTileMoved(String tileId, Position position) {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");
        gameDraftBridge.moveDraftTile(session, tileId, position);
    }

    @Override
    public void onDraftTileRemoved(String tileId) {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        gameDraftBridge.removeDraftTile(session, tileId);
    }

    @Override
    public void onRecallAllDraftTilesRequested(List<TilePlacement> placements) {
        Objects.requireNonNull(placements, "placements cannot be null.");
        gameDraftBridge.recallAllDraftTiles(session);
    }

    @Override
    public void onSubmitDraftRequested(List<TilePlacement> placements) {
        Objects.requireNonNull(placements, "placements cannot be null.");
        gameDraftBridge.submitDraft(session);
    }

    @Override
    public void onSkipTurnRequested() {
        gameDraftBridge.passTurn(session);
    }

    @Override
    public void onRearrangeRequested() {
        gameDraftBridge.rearrangeRack(session);
    }

    @Override
    public void onResignRequested() {
        gameDraftBridge.resign(session);
    }
}
