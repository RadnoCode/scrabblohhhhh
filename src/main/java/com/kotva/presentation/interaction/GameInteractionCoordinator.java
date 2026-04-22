package com.kotva.presentation.interaction;

import com.kotva.presentation.component.BlankTilePickerView;
import com.kotva.presentation.component.ActionPanelView;
import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.RackView;
import com.kotva.presentation.renderer.GameRenderer;
import com.kotva.presentation.renderer.PreviewRenderer;
import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class GameInteractionCoordinator {
    private static final Duration PICKER_HIDE_DELAY = Duration.millis(120);
    private static final double PICKER_TOP_OFFSET = 10;
    private static final double PICKER_SCENE_MARGIN = 8;

    private final BoardView boardView;
    private final RackView rackView;
    private final ActionPanelView actionPanelView;
    private final BlankTilePickerView blankTilePickerView;
    private final Pane blankTilePickerLayer;
    private final GameDraftState draftState;
    private final PreviewRenderer previewRenderer;
    private final GameRenderer gameRenderer;
    private final GameActionPort actionPort;
    private final PauseTransition blankTilePickerHideDelay;
    private final EventHandler<MouseEvent> sceneDragHandler;
    private final EventHandler<MouseEvent> sceneReleaseHandler;
    private final EventHandler<KeyEvent> sceneKeyPressedHandler;
    private Scene attachedScene;
    private PickerAnchor currentPickerAnchor;

    public GameInteractionCoordinator(
        BoardView boardView,
        RackView rackView,
        ActionPanelView actionPanelView,
        BlankTilePickerView blankTilePickerView,
        Pane blankTilePickerLayer,
        GameDraftState draftState,
        PreviewRenderer previewRenderer,
        GameRenderer gameRenderer,
        GameActionPort actionPort) {
        this.boardView = Objects.requireNonNull(boardView, "boardView cannot be null.");
        this.rackView = Objects.requireNonNull(rackView, "rackView cannot be null.");
        this.actionPanelView = Objects.requireNonNull(actionPanelView, "actionPanelView cannot be null.");
        this.blankTilePickerView =
            Objects.requireNonNull(blankTilePickerView, "blankTilePickerView cannot be null.");
        this.blankTilePickerLayer =
            Objects.requireNonNull(blankTilePickerLayer, "blankTilePickerLayer cannot be null.");
        this.draftState = Objects.requireNonNull(draftState, "draftState cannot be null.");
        this.previewRenderer = Objects.requireNonNull(previewRenderer, "previewRenderer cannot be null.");
        this.gameRenderer = Objects.requireNonNull(gameRenderer, "gameRenderer cannot be null.");
        this.actionPort = Objects.requireNonNull(actionPort, "actionPort cannot be null.");
        this.blankTilePickerHideDelay = new PauseTransition(PICKER_HIDE_DELAY);
        this.sceneDragHandler = this::handleSceneMouseDragged;
        this.sceneReleaseHandler = this::handleSceneMouseReleased;
        this.sceneKeyPressedHandler = this::handleSceneKeyPressed;
        this.blankTilePickerHideDelay.setOnFinished(event -> hideBlankTilePicker());
    }

    public void attach() {
        bindRackInteractions();
        bindBoardInteractions();
        bindWorkbenchButtons();
        bindBlankTilePicker();
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
        rackView.setOnSlotEntered(this::handleRackHovered);
        rackView.setOnSlotExited((rackIndex, event) -> scheduleBlankTilePickerHide());
    }

    private void bindBoardInteractions() {
        boardView.setOnCellPressed(this::handleBoardPressed);
        boardView.setOnCellEntered(this::handleBoardHovered);
        boardView.setOnCellExited((coordinate, event) -> scheduleBlankTilePickerHide());
    }

    private void bindWorkbenchButtons() {
        actionPanelView.getSkipTurnButton().setOnAction(event -> actionPort.onSkipTurnRequested());
        actionPanelView.getRearrangeButton().setOnAction(event -> actionPort.onRearrangeRequested());
        actionPanelView.getResignButton().setOnAction(event -> actionPort.onResignRequested());
        actionPanelView.getRecallButton().setOnAction(event -> recallAllDraftTiles());
        actionPanelView.getSubmitButton().setOnAction(event -> submitDraft());
    }

    private void bindBlankTilePicker() {
        blankTilePickerView.setOnLetterSelected(this::handleBlankTileLetterSelected);
        blankTilePickerView.setOnMouseEntered(event -> cancelBlankTilePickerHide());
        blankTilePickerView.setOnMouseExited(event -> scheduleBlankTilePickerHide());
    }

    private void attachSceneHandlers(Scene scene) {
        if (scene == null || scene == attachedScene) {
            return;
        }
        attachedScene = scene;
        attachedScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneDragHandler);
        attachedScene.addEventFilter(MouseEvent.MOUSE_RELEASED, sceneReleaseHandler);
        attachedScene.addEventFilter(KeyEvent.KEY_PRESSED, sceneKeyPressedHandler);
    }

    private void detachSceneHandlers(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.removeEventFilter(MouseEvent.MOUSE_DRAGGED, sceneDragHandler);
        scene.removeEventFilter(MouseEvent.MOUSE_RELEASED, sceneReleaseHandler);
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, sceneKeyPressedHandler);
        if (scene == attachedScene) {
            attachedScene = null;
        }
    }

    private void handleRackPressed(int rackIndex, MouseEvent event) {
        hideBlankTilePicker();
        if (actionPort.isInteractionLocked()) {
            previewRenderer.clear();
            gameRenderer.refresh();
            event.consume();
            return;
        }
        GameViewModel.TileModel tileModel = draftState.getRackTileAt(rackIndex);
        if (tileModel == null || tileModel.isEmpty()) {
            return;
        }

        previewRenderer.beginRackDrag(rackIndex, tileModel, event.getSceneX(), event.getSceneY());
        gameRenderer.refresh();
        event.consume();
    }

    private void handleBoardPressed(BoardCoordinate coordinate, MouseEvent event) {
        hideBlankTilePicker();
        if (actionPort.isInteractionLocked()) {
            previewRenderer.clear();
            gameRenderer.refresh();
            event.consume();
            return;
        }
        GameDraftState.DraftPlacementModel draftPlacement = draftState.getDraftTileAt(coordinate);
        if (draftPlacement == null) {
            return;
        }

        previewRenderer.beginBoardDrag(coordinate, draftPlacement.getTile(), event.getSceneX(), event.getSceneY());
        gameRenderer.refresh();
        event.consume();
    }

    private void handleSceneMouseDragged(MouseEvent event) {
        hideBlankTilePicker();
        if (actionPort.isInteractionLocked()) {
            if (previewRenderer.hasActiveDrag()) {
                previewRenderer.clear();
                gameRenderer.refresh();
                event.consume();
            }
            return;
        }
        if (!previewRenderer.hasActiveDrag()) {
            return;
        }

        previewRenderer.update(event.getSceneX(), event.getSceneY());
        event.consume();
    }

    private void handleSceneMouseReleased(MouseEvent event) {
        if (actionPort.isInteractionLocked()) {
            hideBlankTilePicker();
            if (previewRenderer.hasActiveDrag()) {
                previewRenderer.clear();
                gameRenderer.refresh();
                event.consume();
            }
            return;
        }
        if (!previewRenderer.hasActiveDrag()) {
            return;
        }

        BoardCoordinate targetCoordinate = previewRenderer.getHoveredCoordinate();
        String tileId = previewRenderer.getDraggedTileId();
        Integer targetRackIndex = previewRenderer.getHoveredRackIndex();
        if (previewRenderer.isDraggingFromBoard() && targetRackIndex != null) {
            actionPort.onDraftTileRemoved(tileId);
            previewRenderer.clear();
            gameRenderer.refresh();
            event.consume();
            return;
        }
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

    private void handleSceneKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.D && event.isShortcutDown() && event.isShiftDown()) {
            actionPort.onDebugRackEditRequested();
            event.consume();
            return;
        }
        if (event.getCode() != KeyCode.ENTER || !event.isShortcutDown()) {
            return;
        }
        submitDraft();
        event.consume();
    }

    private void handleRackHovered(int rackIndex, MouseEvent event) {
        if (actionPort.isInteractionLocked() || previewRenderer.hasActiveDrag()) {
            hideBlankTilePicker();
            return;
        }

        GameViewModel.TileModel tileModel = draftState.getRackTileAt(rackIndex);
        if (tileModel == null || tileModel.isEmpty() || !tileModel.isBlank()) {
            hideBlankTilePicker();
            return;
        }
        showBlankTilePicker(
            PickerAnchor.forRack(rackIndex, tileModel.getTileId()),
            tileModel,
            rackView.getTileBoundsInScene(rackIndex));
    }

    private void handleBoardHovered(BoardCoordinate coordinate, MouseEvent event) {
        if (actionPort.isInteractionLocked() || previewRenderer.hasActiveDrag()) {
            hideBlankTilePicker();
            return;
        }

        GameDraftState.DraftPlacementModel placement = draftState.getDraftTileAt(coordinate);
        if (placement == null || !placement.getTile().isBlank()) {
            hideBlankTilePicker();
            return;
        }
        showBlankTilePicker(
            PickerAnchor.forBoard(coordinate, placement.getTile().getTileId()),
            placement.getTile(),
            boardView.getCellBoundsInScene(coordinate));
    }

    private void showBlankTilePicker(
        PickerAnchor pickerAnchor,
        GameViewModel.TileModel tileModel,
        Bounds anchorBounds) {
        cancelBlankTilePickerHide();
        currentPickerAnchor = pickerAnchor;
        blankTilePickerView.showPicker(tileModel.getAssignedLetter());

        Point2D anchorPoint = blankTilePickerLayer.sceneToLocal(anchorBounds.getMinX(), anchorBounds.getMinY());
        double pickerWidth = blankTilePickerView.getPickerWidth();
        double pickerHeight = blankTilePickerView.getPickerHeight();

        double x = anchorPoint.getX() + (anchorBounds.getWidth() - pickerWidth) / 2.0;
        double y = anchorPoint.getY() - pickerHeight - PICKER_TOP_OFFSET;

        double layerWidth = blankTilePickerLayer.getWidth() > 0
            ? blankTilePickerLayer.getWidth()
            : boardView.getScene().getWidth();
        x = Math.max(PICKER_SCENE_MARGIN, x);
        x = Math.min(layerWidth - pickerWidth - PICKER_SCENE_MARGIN, x);
        y = Math.max(PICKER_SCENE_MARGIN, y);

        blankTilePickerView.relocate(x, y);
        blankTilePickerView.toFront();
    }

    private void handleBlankTileLetterSelected(Character selectedLetter) {
        if (currentPickerAnchor == null || selectedLetter == null) {
            return;
        }
        actionPort.onBlankTileLetterAssigned(currentPickerAnchor.tileId(), selectedLetter);
        hideBlankTilePicker();
    }

    private void scheduleBlankTilePickerHide() {
        if (!blankTilePickerView.isPickerVisible()) {
            return;
        }
        blankTilePickerHideDelay.playFromStart();
    }

    private void cancelBlankTilePickerHide() {
        blankTilePickerHideDelay.stop();
    }

    private void hideBlankTilePicker() {
        cancelBlankTilePickerHide();
        currentPickerAnchor = null;
        blankTilePickerView.hidePicker();
    }

    private void recallAllDraftTiles() {
        if (actionPort.isInteractionLocked()) {
            hideBlankTilePicker();
            previewRenderer.clear();
            gameRenderer.refresh();
            return;
        }
        if (!draftState.hasDraftPlacements()) {
            return;
        }
        previewRenderer.clear();
        actionPort.onRecallAllDraftTilesRequested();
    }

    private void submitDraft() {
        if (actionPort.isInteractionLocked()) {
            hideBlankTilePicker();
            previewRenderer.clear();
            gameRenderer.refresh();
            return;
        }
        if (!draftState.hasDraftPlacements()) {
            return;
        }
        hideBlankTilePicker();
        previewRenderer.clear();
        actionPort.onSubmitDraftRequested();
    }

    private record PickerAnchor(String tileId) {
        private static PickerAnchor forRack(int rackIndex, String tileId) {
            return new PickerAnchor(tileId);
        }

        private static PickerAnchor forBoard(BoardCoordinate coordinate, String tileId) {
            return new PickerAnchor(tileId);
        }
    }
}
