package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * BoardView 负责棋盘区域的 JavaFX 绘制。
 * 它既管理 15x15 的格子节点，也暴露坐标解析和 hover 高亮能力。
 */
public class BoardView extends StackPane {
    public static final int BOARD_SIZE = 15;
    public static final double CELL_SIZE = 30;
    public static final double CELL_GAP = 3;

    private final GridPane gridPane;
    private final StackPane[][] cellViews;
    private BoardCoordinate hoveredCoordinate;

    public BoardView() {
        this.gridPane = new GridPane();
        this.cellViews = new StackPane[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        // 给整个棋盘容器挂样式，并把内部内容居中。
        getStyleClass().add("game-board-shell");
        setAlignment(Pos.CENTER);

        // GridPane 负责真正的 15x15 布局。
        gridPane.getStyleClass().add("game-board-grid");
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(CELL_GAP);
        gridPane.setVgap(CELL_GAP);

        // 逐格创建棋盘单元格，后续正式 Tile 会挂到这些格子内部。
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                // 每个格子本身是一个 StackPane，这样后续可以直接把 Tile 叠进去。
                StackPane cell = new StackPane();
                cell.getStyleClass().add("game-board-cell");
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setMinSize(CELL_SIZE, CELL_SIZE);
                cell.setMaxSize(CELL_SIZE, CELL_SIZE);
                cellViews[row][column] = cell;
                gridPane.add(cell, column, row);
                GridPane.setHalignment(cell, HPos.CENTER);
                GridPane.setValignment(cell, VPos.CENTER);
            }
        }

        // 根据格子大小和间距，算出整个棋盘容器的边长。
        double boardLength = BOARD_SIZE * CELL_SIZE + (BOARD_SIZE - 1) * CELL_GAP;
        setPrefSize(boardLength, boardLength);
        // 最后把 GridPane 加到当前组件中。
        getChildren().add(gridPane);
    }

    public void setTiles(List<GameViewModel.BoardTileModel> boardTiles) {
        Objects.requireNonNull(boardTiles, "boardTiles cannot be null.");

        // 每次重绘前先清空每个格子里旧的 Tile 节点。
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                cellViews[row][column].getChildren().clear();
            }
        }

        // 按照 ViewModel 提供的棋子列表，把 Tile 重新挂回到对应格子里。
        for (GameViewModel.BoardTileModel boardTile : boardTiles) {
            BoardCoordinate coordinate = boardTile.getCoordinate();
            // 棋盘上的 Tile 要用和格子同尺寸的小版组件。
            TileView tileView = new TileView(CELL_SIZE);
            tileView.setTile(boardTile.getTile());
            // 棋盘内的正式 Tile 不直接响应鼠标，由格子本身接事件。
            tileView.setMouseTransparent(true);
            if (boardTile.isDraft()) {
                tileView.getStyleClass().add("game-board-draft-tile");
            }
            cellViews[coordinate.row()][coordinate.col()].getChildren().add(tileView);
        }
    }

    public void setHoveredCell(BoardCoordinate coordinate) {
        // 先把上一个高亮格子的白色边框去掉。
        if (hoveredCoordinate != null) {
            cellViews[hoveredCoordinate.row()][hoveredCoordinate.col()].getStyleClass().remove("game-board-cell-hover");
        }
        // 记录当前 hover 到的新格子。
        hoveredCoordinate = coordinate;
        if (hoveredCoordinate != null) {
            StackPane cell = cellViews[hoveredCoordinate.row()][hoveredCoordinate.col()];
            if (!cell.getStyleClass().contains("game-board-cell-hover")) {
                cell.getStyleClass().add("game-board-cell-hover");
            }
        }
    }

    public BoardCoordinate resolveCoordinate(double sceneX, double sceneY) {
        // 用 scene 坐标去反查鼠标当前压在哪个棋盘格上。
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if (cellViews[row][column].localToScene(cellViews[row][column].getBoundsInLocal()).contains(sceneX, sceneY)) {
                    return new BoardCoordinate(row, column);
                }
            }
        }
        return null;
    }

    public void setOnCellPressed(BiConsumer<BoardCoordinate, MouseEvent> handler) {
        Objects.requireNonNull(handler, "handler cannot be null.");
        // 给每个格子都绑定一个按下事件，并把对应坐标一起抛出去。
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                BoardCoordinate coordinate = new BoardCoordinate(row, column);
                cellViews[row][column].setOnMousePressed(event -> handler.accept(coordinate, event));
            }
        }
    }

    public double getCellSize() {
        return CELL_SIZE;
    }
}
