package com.kotva.presentation.renderer;

import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.TileView;
import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.Objects;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

/**
 * PreviewRenderer 专门负责“假的视觉层”。
 * 包括拖拽跟手 Tile、棋盘 hover 白边、以及源位置的临时隐藏信息。
 */
public class PreviewRenderer {
    private final BoardView boardView;
    private final Pane overlayPane;

    private TileView floatingTile;
    private GameViewModel.TileModel draggedTile;
    private DragSource dragSource;
    private Integer suppressedRackIndex;
    private BoardCoordinate suppressedBoardCoordinate;
    private BoardCoordinate hoveredCoordinate;

    public PreviewRenderer(BoardView boardView, Pane overlayPane) {
        this.boardView = Objects.requireNonNull(boardView, "boardView cannot be null.");
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

        // 每次拖动时重新探测当前鼠标落在哪个棋盘格上。
        hoveredCoordinate = boardView.resolveCoordinate(sceneX, sceneY);
        // 同步刷新棋盘高亮边框。
        boardView.setHoveredCell(hoveredCoordinate);
        // 再把跟手 Tile 挪到最新鼠标位置。
        moveFloatingTile(sceneX, sceneY);
    }

    public void clear() {
        // 清掉所有临时状态，回到没有拖拽中的状态。
        hoveredCoordinate = null;
        suppressedRackIndex = null;
        suppressedBoardCoordinate = null;
        draggedTile = null;
        dragSource = null;
        boardView.setHoveredCell(null);
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

    private void startDrag(
            GameViewModel.TileModel tileModel,
            DragSource dragSource,
            Integer rackIndex,
            BoardCoordinate boardCoordinate,
            double sceneX,
            double sceneY) {
        // 先清旧状态，避免连续拖拽时残留旧浮层。
        clear();
        this.draggedTile = Objects.requireNonNull(tileModel, "tileModel cannot be null.");
        this.dragSource = Objects.requireNonNull(dragSource, "dragSource cannot be null.");
        // 记录原始来源，方便 renderer 临时隐藏对应位置。
        this.suppressedRackIndex = rackIndex;
        this.suppressedBoardCoordinate = boardCoordinate;
        // 创建一个和棋盘格同尺寸的小 Tile 作为“假”的跟手效果。
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

        // 把 scene 坐标换算成 overlayPane 自己的局部坐标。
        Point2D localPoint = overlayPane.sceneToLocal(sceneX, sceneY);
        // 让浮动 Tile 的中心对齐到鼠标，而不是左上角对齐。
        double halfSize = floatingTile.getTileSize() / 2.0;
        floatingTile.relocate(localPoint.getX() - halfSize, localPoint.getY() - halfSize);
    }

    private enum DragSource {
        RACK,
        BOARD
    }
}
