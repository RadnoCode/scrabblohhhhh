package com.kotva.presentation.interaction;

import com.kotva.domain.model.TilePlacement;
import com.kotva.presentation.component.ActionPanelView;
import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.RackView;
import com.kotva.presentation.renderer.GameRenderer;
import com.kotva.presentation.renderer.PreviewRenderer;
import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.List;
import java.util.Objects;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

/**
 * GameInteractionCoordinator 负责把 JavaFX 事件接到草稿层与预览层。
 */
public class GameInteractionCoordinator {
    private final BoardView boardView;
    private final RackView rackView;
    private final ActionPanelView actionPanelView;
    private final GameDraftState draftState;
    private final PreviewRenderer previewRenderer;
    private final GameRenderer gameRenderer;
    private final GameActionPort actionPort;
    private final EventHandler<MouseEvent> sceneDragHandler;
    private final EventHandler<MouseEvent> sceneReleaseHandler;
    private Scene attachedScene;

    public GameInteractionCoordinator(
            BoardView boardView,
            RackView rackView,
            ActionPanelView actionPanelView,
            GameDraftState draftState,
            PreviewRenderer previewRenderer,
            GameRenderer gameRenderer,
            GameActionPort actionPort) {
        this.boardView = Objects.requireNonNull(boardView, "boardView cannot be null.");
        this.rackView = Objects.requireNonNull(rackView, "rackView cannot be null.");
        this.actionPanelView = Objects.requireNonNull(actionPanelView, "actionPanelView cannot be null.");
        this.draftState = Objects.requireNonNull(draftState, "draftState cannot be null.");
        this.previewRenderer = Objects.requireNonNull(previewRenderer, "previewRenderer cannot be null.");
        this.gameRenderer = Objects.requireNonNull(gameRenderer, "gameRenderer cannot be null.");
        this.actionPort = Objects.requireNonNull(actionPort, "actionPort cannot be null.");
        this.sceneDragHandler = this::handleSceneMouseDragged;
        this.sceneReleaseHandler = this::handleSceneMouseReleased;
    }

    public void attach() {
        // 先绑定牌架、棋盘和工作台上的事件。
        bindRackInteractions();
        bindBoardInteractions();
        bindWorkbenchButtons();
        // 再监听 scene 的变化，给整个场景挂拖拽中的全局鼠标事件。
        boardView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            detachSceneHandlers(oldScene);
            attachSceneHandlers(newScene);
        });
        attachSceneHandlers(boardView.getScene());
    }

    private void bindRackInteractions() {
        // 牌架里的每个槽位都绑定按下事件，作为拖拽起点。
        for (int index = 0; index < rackView.getSlotCount(); index++) {
            final int rackIndex = index;
            rackView.getTileView(index).setOnMousePressed(event -> handleRackPressed(rackIndex, event));
        }
    }

    private void bindBoardInteractions() {
        boardView.setOnCellPressed(this::handleBoardPressed);
    }

    private void bindWorkbenchButtons() {
        // 这些按钮本身不关心后端，只负责把意图抛给控制器。
        actionPanelView.getSkipTurnButton().setOnAction(event -> actionPort.onSkipTurnRequested());
        actionPanelView.getRearrangeButton().setOnAction(event -> actionPort.onRearrangeRequested());
        actionPanelView.getResignButton().setOnAction(event -> actionPort.onResignRequested());
        actionPanelView.getRecallButton().setOnAction(event -> recallAllDraftTiles());
        actionPanelView.getSubmitButton().setOnAction(event -> submitDraft());
    }

    private void attachSceneHandlers(Scene scene) {
        if (scene == null || scene == attachedScene) {
            return;
        }
        // 拖拽中的鼠标移动和松开都要挂在 Scene 上，避免鼠标离开原组件后事件丢失。
        attachedScene = scene;
        attachedScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneDragHandler);
        attachedScene.addEventFilter(MouseEvent.MOUSE_RELEASED, sceneReleaseHandler);
    }

    private void detachSceneHandlers(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.removeEventFilter(MouseEvent.MOUSE_DRAGGED, sceneDragHandler);
        scene.removeEventFilter(MouseEvent.MOUSE_RELEASED, sceneReleaseHandler);
        if (scene == attachedScene) {
            attachedScene = null;
        }
    }

    private void handleRackPressed(int rackIndex, MouseEvent event) {
        // 只有非空牌架槽位才允许开始拖拽。
        GameViewModel.TileModel tileModel = draftState.getRackTileAt(rackIndex);
        if (tileModel == null || tileModel.isEmpty()) {
            return;
        }

        // 启动一段“从 rack 出发”的假拖拽预览。
        previewRenderer.beginRackDrag(rackIndex, tileModel, event.getSceneX(), event.getSceneY());
        gameRenderer.refresh();
        event.consume();
    }

    private void handleBoardPressed(BoardCoordinate coordinate, MouseEvent event) {
        // 只有草稿层里已有的 Tile 才允许从棋盘再次拖起。
        GameDraftState.DraftPlacementModel draftPlacement = draftState.getDraftTileAt(coordinate);
        if (draftPlacement == null) {
            return;
        }

        // 启动一段“从棋盘出发”的假拖拽预览。
        previewRenderer.beginBoardDrag(coordinate, draftPlacement.getTile(), event.getSceneX(), event.getSceneY());
        gameRenderer.refresh();
        event.consume();
    }

    private void handleSceneMouseDragged(MouseEvent event) {
        if (!previewRenderer.hasActiveDrag()) {
            return;
        }

        // 拖拽中不断刷新假 Tile 和 hover 高亮。
        previewRenderer.update(event.getSceneX(), event.getSceneY());
        event.consume();
    }

    private void handleSceneMouseReleased(MouseEvent event) {
        if (!previewRenderer.hasActiveDrag()) {
            return;
        }

        // 先看当前鼠标是否停在某个棋盘格上。
        BoardCoordinate targetCoordinate = previewRenderer.getHoveredCoordinate();
        String tileId = previewRenderer.getDraggedTileId();

        // 松手不在格子上，或者目标格已占用，就直接取消这次假拖拽并回滚视觉。
        if (targetCoordinate == null || draftState.isCellOccupied(targetCoordinate, tileId)) {
            previewRenderer.clear();
            gameRenderer.refresh();
            event.consume();
            return;
        }

        if (previewRenderer.isDraggingFromRack()) {
            // 从 rack 来：登记一条新的草稿放置记录。
            draftState.placeRackTile(previewRenderer.getSuppressedRackIndex(), targetCoordinate);
            actionPort.onDraftTilePlaced(tileId, targetCoordinate.toPosition());
        } else if (previewRenderer.isDraggingFromBoard()) {
            // 从棋盘来：只更新现有草稿的位置。
            draftState.moveDraftTile(tileId, targetCoordinate);
            actionPort.onDraftTileMoved(tileId, targetCoordinate.toPosition());
        }

        // 最后清掉假预览，并让真实渲染层重画。
        previewRenderer.clear();
        gameRenderer.refresh();
        event.consume();
    }

    private void recallAllDraftTiles() {
        // 先取出当前全部草稿，方便后端后续直接消费。
        List<TilePlacement> placements = draftState.toTilePlacements();
        if (placements.isEmpty()) {
            return;
        }

        // 先清前端状态，再通知控制器。
        previewRenderer.clear();
        draftState.recallAllDraftTiles();
        gameRenderer.refresh();
        actionPort.onRecallAllDraftTilesRequested(placements);
    }

    private void submitDraft() {
        // 提交时直接把前端草稿序列化结果交给控制器。
        actionPort.onSubmitDraftRequested(draftState.toTilePlacements());
    }
}
