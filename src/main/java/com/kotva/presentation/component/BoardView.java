package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class BoardView extends StackPane {
    public static final int BOARD_SIZE = 15;
    public static final double CELL_SIZE = 30;
    public static final double CELL_GAP = 3;
    private static final String HOVER_CLASS = "game-board-cell-hover";
    private static final String DOUBLE_LETTER_CLASS = "game-board-cell-double-letter";
    private static final String TRIPLE_LETTER_CLASS = "game-board-cell-triple-letter";
    private static final String DOUBLE_WORD_CLASS = "game-board-cell-double-word";
    private static final String TRIPLE_WORD_CLASS = "game-board-cell-triple-word";

    private final GridPane gridPane;
    private final PreviewWordOutlineView previewWordOutlineView;
    private final StackPane[][] cellViews;
    private BoardCoordinate hoveredCoordinate;

    public BoardView() {
        this.gridPane = new GridPane();
        this.previewWordOutlineView =
        new PreviewWordOutlineView(
            BOARD_SIZE * CELL_SIZE + (BOARD_SIZE - 1) * CELL_GAP,
            CELL_SIZE,
            CELL_GAP);
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
        getChildren().addAll(gridPane, previewWordOutlineView);
    }

    public void setTiles(List<GameViewModel.BoardTileModel> boardTiles) {
        Objects.requireNonNull(boardTiles, "boardTiles cannot be null.");

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                StackPane cell = cellViews[row][column];
                cell.getChildren().clear();
                cell.getStyleClass().removeAll(
                    DOUBLE_LETTER_CLASS,
                    TRIPLE_LETTER_CLASS,
                    DOUBLE_WORD_CLASS,
                    TRIPLE_WORD_CLASS);
            }
        }

        for (GameViewModel.BoardTileModel boardTile : boardTiles) {
            BoardCoordinate coordinate = boardTile.getCoordinate();
            StackPane cell = cellViews[coordinate.row()][coordinate.col()];
            applyBonusClass(cell, boardTile);

            if (boardTile.getTile().isEmpty()) {
                Label bonusLabel = createBonusLabel(boardTile);
                if (bonusLabel != null) {
                    cell.getChildren().add(bonusLabel);
                }
                continue;
            }

            TileView tileView = new TileView(CELL_SIZE);
            tileView.setTile(boardTile.getTile());
            tileView.setMouseTransparent(true);
            if (boardTile.isDraft()) {
                tileView.getStyleClass().add("game-board-draft-tile");
            }
            cell.getChildren().add(tileView);
        }
    }

    public void setWordOutline(GameViewModel.WordOutlineModel wordOutline) {
        previewWordOutlineView.showOutline(wordOutline);
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

    }

    private void applyBonusClass(StackPane cell, GameViewModel.BoardTileModel boardTile) {
        switch (boardTile.getBonusType()) {
        case DOUBLE_LETTER -> cell.getStyleClass().add(DOUBLE_LETTER_CLASS);
        case TRIPLE_LETTER -> cell.getStyleClass().add(TRIPLE_LETTER_CLASS);
        case DOUBLE_WORD -> cell.getStyleClass().add(DOUBLE_WORD_CLASS);
        case TRIPLE_WORD -> cell.getStyleClass().add(TRIPLE_WORD_CLASS);
        case NONE -> {
            }
        }
    }

    private Label createBonusLabel(GameViewModel.BoardTileModel boardTile) {
        String text = switch (boardTile.getBonusType()) {
        case DOUBLE_LETTER -> "DL";
        case TRIPLE_LETTER -> "TL";
        case DOUBLE_WORD -> "DW";
        case TRIPLE_WORD -> "TW";
        case NONE -> "";
        };

        if (text.isEmpty()) {
            return null;
        }

        Label bonusLabel = new Label(text);
        bonusLabel.getStyleClass().add("game-board-bonus-label");
        bonusLabel.setMouseTransparent(true);
        return bonusLabel;
    }
}