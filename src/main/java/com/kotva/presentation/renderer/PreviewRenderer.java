package com.kotva.presentation.renderer;

import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.RackView;
import com.kotva.presentation.component.TileView;
import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.Objects;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

public class PreviewRenderer {
    private final BoardView boardView;
    private final RackView rackView;
    private final Pane overlayPane;

    private TileView floatingTile;
    private GameViewModel.TileModel draggedTile;
    private DragSource dragSource;
    private Integer suppressedRackIndex;
    private BoardCoordinate suppressedBoardCoordinate;
    private BoardCoordinate hoveredCoordinate;
    private Integer hoveredRackIndex;

    public PreviewRenderer(BoardView boardView, RackView rackView, Pane overlayPane) {
        this.boardView = Objects.requireNonNull(boardView, "boardView cannot be null.");
        this.rackView = Objects.requireNonNull(rackView, "rackView cannot be null.");
        this.overlayPane = Objects.requireNonNull(overlayPane, "overlayPane cannot be null.");
    }

    public void beginRackDrag(int rackIndex, GameViewModel.TileModel tileModel, double sceneX, double sceneY) {
        startDrag(tileModel, DragSource.RACK, rackIndex, null, sceneX, sceneY);
    }

    public void beginBoardDrag(BoardCoordinate coordinate, GameViewModel.TileModel tileModel, double sceneX, double sceneY) {
        startDrag(tileModel, DragSource.BOARD, null, coordinate, sceneX, sceneY);
    }

    public void update(double sceneX, double sceneY) {
        if (!hasActiveDrag()) {
            return;
        }

        hoveredCoordinate = boardView.resolveCoordinate(sceneX, sceneY);
        hoveredRackIndex = rackView.resolveRackIndex(sceneX, sceneY);
        boardView.setHoveredCell(hoveredCoordinate);
        rackView.setHoveredSlot(dragSource == DragSource.BOARD ? hoveredRackIndex : null);
        moveFloatingTile(sceneX, sceneY);
    }

    public void clear() {
        hoveredCoordinate = null;
        hoveredRackIndex = null;
        suppressedRackIndex = null;
        suppressedBoardCoordinate = null;
        draggedTile = null;
        dragSource = null;
        boardView.setHoveredCell(null);
        rackView.setHoveredSlot(null);
        if (floatingTile != null) {
            overlayPane.getChildren().remove(floatingTile);
            floatingTile = null;
        }
    }

    public boolean hasActiveDrag() {
        return draggedTile != null && dragSource != null;
    }

    public boolean isDraggingFromRack() {
        return dragSource == DragSource.RACK;
    }

    public boolean isDraggingFromBoard() {
        return dragSource == DragSource.BOARD;
    }

    public String getDraggedTileId() {
        return draggedTile != null ? draggedTile.getTileId() : "";
    }

    public GameViewModel.TileModel getDraggedTile() {
        return draggedTile;
    }

    public Integer getSuppressedRackIndex() {
        return suppressedRackIndex;
    }

    public BoardCoordinate getSuppressedBoardCoordinate() {
        return suppressedBoardCoordinate;
    }

    public BoardCoordinate getHoveredCoordinate() {
        return hoveredCoordinate;
    }

    public Integer getHoveredRackIndex() {
        return hoveredRackIndex;
    }

    private void startDrag(
        GameViewModel.TileModel tileModel,
        DragSource dragSource,
        Integer rackIndex,
        BoardCoordinate boardCoordinate,
        double sceneX,
        double sceneY) {
        clear();
        this.draggedTile = Objects.requireNonNull(tileModel, "tileModel cannot be null.");
        this.dragSource = Objects.requireNonNull(dragSource, "dragSource cannot be null.");
        this.suppressedRackIndex = rackIndex;
        this.suppressedBoardCoordinate = boardCoordinate;
        this.floatingTile = new TileView(boardView.getCellSize());
        this.floatingTile.setTile(tileModel);
        this.floatingTile.getStyleClass().add("game-drag-floating-tile");
        this.floatingTile.setMouseTransparent(true);
        overlayPane.getChildren().add(floatingTile);
        update(sceneX, sceneY);
    }

    private void moveFloatingTile(double sceneX, double sceneY) {
        if (floatingTile == null) {
            return;
        }

        Point2D localPoint = overlayPane.sceneToLocal(sceneX, sceneY);
        double halfSize = floatingTile.getTileSize() / 2.0;
        floatingTile.relocate(localPoint.getX() - halfSize, localPoint.getY() - halfSize);
    }

    private enum DragSource {
        RACK,
        BOARD
    }
}