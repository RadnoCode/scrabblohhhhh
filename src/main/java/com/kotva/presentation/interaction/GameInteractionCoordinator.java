package com.kotva.presentation.interaction;

import com.kotva.presentation.component.ActionPanelView;
import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.RackView;
import com.kotva.presentation.renderer.GameRenderer;
import com.kotva.presentation.renderer.PreviewRenderer;
import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.Objects;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

/**
 * GameInteractionCoordinator 负责把 JavaFX 事件接到控制器和纯视觉预览层。
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
        bindRackInteractions();
        bindBoardInteractions();
        bindWorkbenchButtons();
        boardView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            detachSceneHandlers(oldScene);
            attachSceneHandlers(newScene);
        });
        attachSceneHandlers(boardView.getScene());
    }

    private void bindRackInteractions() {
        for (int index = 0; index < rackView.getSlotCount(); index++) {
            final int rackIndex = index;
            rackView.getTileView(index).setOnMousePressed(event -> handleRackPressed(rackIndex, event));
        }
    }

    private void bindBoardInteractions() {
        boardView.setOnCellPressed(this::handleBoardPressed);
    }

    private void bindWorkbenchButtons() {
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
        GameViewModel.TileModel tileModel = draftState.getRackTileAt(rackIndex);
        if (tileModel == null || tileModel.isEmpty()) {
            return;
        }

        previewRenderer.beginRackDrag(rackIndex, tileModel, event.getSceneX(), event.getSceneY());
        gameRenderer.refresh();
        event.consume();
    }

    private void handleBoardPressed(BoardCoordinate coordinate, MouseEvent event) {
        GameDraftState.DraftPlacementModel draftPlacement = draftState.getDraftTileAt(coordinate);
        if (draftPlacement == null) {
            return;
        }

        previewRenderer.beginBoardDrag(coordinate, draftPlacement.getTile(), event.getSceneX(), event.getSceneY());
        gameRenderer.refresh();
        event.consume();
    }

    private void handleSceneMouseDragged(MouseEvent event) {
        if (!previewRenderer.hasActiveDrag()) {
            return;
        }

        previewRenderer.update(event.getSceneX(), event.getSceneY());
        event.consume();
    }

    private void handleSceneMouseReleased(MouseEvent event) {
        if (!previewRenderer.hasActiveDrag()) {
            return;
        }

        BoardCoordinate targetCoordinate = previewRenderer.getHoveredCoordinate();
        String tileId = previewRenderer.getDraggedTileId();
        if (targetCoordinate == null || draftState.isCellOccupied(targetCoordinate, tileId)) {
            previewRenderer.clear();
            gameRenderer.refresh();
            event.consume();
            return;
        }

        if (previewRenderer.isDraggingFromRack()) {
            actionPort.onDraftTilePlaced(tileId, targetCoordinate.toPosition());
        } else if (previewRenderer.isDraggingFromBoard()) {
            actionPort.onDraftTileMoved(tileId, targetCoordinate.toPosition());
        }

        previewRenderer.clear();
        gameRenderer.refresh();
        event.consume();
    }

    private void recallAllDraftTiles() {
        if (!draftState.hasDraftPlacements()) {
            return;
        }
        previewRenderer.clear();
        actionPort.onRecallAllDraftTilesRequested();
    }

    private void submitDraft() {
        if (!draftState.hasDraftPlacements()) {
            return;
        }
        previewRenderer.clear();
        actionPort.onSubmitDraftRequested();
    }
}
