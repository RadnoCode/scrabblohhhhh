package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class PreviewWordOutlineView extends Pane {
    private static final String BASE_CLASS = "game-board-word-outline";
    private static final String VALID_CLASS = "game-board-word-outline-valid";
    private static final String INVALID_CLASS = "game-board-word-outline-invalid";

    private final double cellSize;
    private final double cellGap;
    private final Rectangle outlineRectangle;

    public PreviewWordOutlineView(double boardLength, double cellSize, double cellGap) {
        this.cellSize = cellSize;
        this.cellGap = cellGap;
        this.outlineRectangle = new Rectangle();
        initialize(boardLength);
    }

    private void initialize(double boardLength) {
        getStyleClass().add("game-board-word-outline-layer");
        setPickOnBounds(false);
        setMouseTransparent(true);
        setPrefSize(boardLength, boardLength);
        setMinSize(boardLength, boardLength);
        setMaxSize(boardLength, boardLength);

        outlineRectangle.setManaged(false);
        outlineRectangle.setMouseTransparent(true);
        outlineRectangle.setStrokeType(StrokeType.INSIDE);
        outlineRectangle.getStyleClass().add(BASE_CLASS);
        outlineRectangle.setVisible(false);
        getChildren().add(outlineRectangle);
    }

    public void showOutline(GameViewModel.WordOutlineModel wordOutline) {
        if (wordOutline == null) {
            outlineRectangle.setVisible(false);
            outlineRectangle.getStyleClass().removeAll(VALID_CLASS, INVALID_CLASS);
            return;
        }

        double x = toCellOrigin(wordOutline.getStartCol());
        double y = toCellOrigin(wordOutline.getStartRow());
        double width = spanLength(wordOutline.getStartCol(), wordOutline.getEndCol());
        double height = spanLength(wordOutline.getStartRow(), wordOutline.getEndRow());

        outlineRectangle.setX(x);
        outlineRectangle.setY(y);
        outlineRectangle.setWidth(width);
        outlineRectangle.setHeight(height);
        outlineRectangle.getStyleClass().removeAll(VALID_CLASS, INVALID_CLASS);
        outlineRectangle.getStyleClass().add(wordOutline.isValid() ? VALID_CLASS : INVALID_CLASS);
        outlineRectangle.setVisible(true);
    }

    private double toCellOrigin(int index) {
        return index * (cellSize + cellGap);
    }

    private double spanLength(int startIndex, int endIndex) {
        int cellCount = Math.max(1, endIndex - startIndex + 1);
        return cellCount * cellSize + Math.max(0, cellCount - 1) * cellGap;
    }
}