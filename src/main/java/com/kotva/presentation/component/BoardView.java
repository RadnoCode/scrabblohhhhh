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
    private static final String MAIN_WORD_CLASS = "game-board-cell-main-word";
    private static final String VALID_PREVIEW_CLASS = "game-board-cell-valid-preview";
    private static final String INVALID_PREVIEW_CLASS = "game-board-cell-invalid-preview";
    private static final String HOVER_CLASS = "game-board-cell-hover";

    private final GridPane gridPane;
    private final StackPane[][] cellViews;
    private BoardCoordinate hoveredCoordinate;

    public BoardView() {
        this.gridPane = new GridPane();
        this.cellViews = new StackPane[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        getStyleClass().add("game-board-shell");
        setAlignment(Pos.CENTER);

        gridPane.getStyleClass().add("game-board-grid");
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(CELL_GAP);
        gridPane.setVgap(CELL_GAP);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
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

        double boardLength = BOARD_SIZE * CELL_SIZE + (BOARD_SIZE - 1) * CELL_GAP;
        setPrefSize(boardLength, boardLength);
        getChildren().add(gridPane);
    }

    public void setTiles(List<GameViewModel.BoardTileModel> boardTiles) {
        Objects.requireNonNull(boardTiles, "boardTiles cannot be null.");

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                StackPane cell = cellViews[row][column];
                cell.getChildren().clear();
                cell.getStyleClass().removeAll(MAIN_WORD_CLASS, VALID_PREVIEW_CLASS, INVALID_PREVIEW_CLASS);
            }
        }

        for (GameViewModel.BoardTileModel boardTile : boardTiles) {
            BoardCoordinate coordinate = boardTile.getCoordinate();
            StackPane cell = cellViews[coordinate.row()][coordinate.col()];
            applyCellHighlightClasses(cell, boardTile);

            TileView tileView = new TileView(CELL_SIZE);
            tileView.setTile(boardTile.getTile());
            tileView.setMouseTransparent(true);
            if (boardTile.isDraft()) {
                tileView.getStyleClass().add("game-board-draft-tile");
            }
            cell.getChildren().add(tileView);
        }
    }

    public void setHoveredCell(BoardCoordinate coordinate) {
        if (hoveredCoordinate != null) {
            cellViews[hoveredCoordinate.row()][hoveredCoordinate.col()].getStyleClass().remove(HOVER_CLASS);
        }
        hoveredCoordinate = coordinate;
        if (hoveredCoordinate != null) {
            StackPane cell = cellViews[hoveredCoordinate.row()][hoveredCoordinate.col()];
            if (!cell.getStyleClass().contains(HOVER_CLASS)) {
                cell.getStyleClass().add(HOVER_CLASS);
            }
        }
    }

    public BoardCoordinate resolveCoordinate(double sceneX, double sceneY) {
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

    private void applyCellHighlightClasses(StackPane cell, GameViewModel.BoardTileModel boardTile) {
        if (boardTile.isMainWordHighlighted()) {
            cell.getStyleClass().add(MAIN_WORD_CLASS);
        }
        if (boardTile.isPreviewValid()) {
            cell.getStyleClass().add(VALID_PREVIEW_CLASS);
        }
        if (boardTile.isPreviewInvalid()) {
            cell.getStyleClass().add(INVALID_PREVIEW_CLASS);
        }
    }
}
