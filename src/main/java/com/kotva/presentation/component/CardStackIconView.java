package com.kotva.presentation.component;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * CardStackIconView draws simple stacked cards for the GameSetting page.
 */
public class CardStackIconView extends Pane {
    private final Rectangle leftCard;
    private final Rectangle rightCard;
    private final Rectangle centerCard;
    private final Rectangle upperSquare;
    private final Rectangle lowerSquare;
    private final Line leftStrokeTop;
    private final Line leftStrokeBottom;
    private final Line rightStrokeTop;
    private final Line rightStrokeBottom;

    public CardStackIconView() {
        leftCard = new Rectangle();
        rightCard = new Rectangle();
        centerCard = new Rectangle();
        upperSquare = new Rectangle();
        lowerSquare = new Rectangle();
        leftStrokeTop = new Line();
        leftStrokeBottom = new Line();
        rightStrokeTop = new Line();
        rightStrokeBottom = new Line();

        initializeShapes();
        getChildren().addAll(
                leftCard,
                rightCard,
                centerCard,
                upperSquare,
                lowerSquare,
                leftStrokeTop,
                leftStrokeBottom,
                rightStrokeTop,
                rightStrokeBottom
        );
    }

    private void initializeShapes() {
        getStyleClass().add("cards-icon");

        leftCard.getStyleClass().add("cards-side");
        rightCard.getStyleClass().add("cards-side");
        centerCard.getStyleClass().add("cards-center");
        upperSquare.getStyleClass().add("cards-detail");
        lowerSquare.getStyleClass().add("cards-detail");
        leftStrokeTop.getStyleClass().add("cards-line");
        leftStrokeBottom.getStyleClass().add("cards-line");
        rightStrokeTop.getStyleClass().add("cards-line");
        rightStrokeBottom.getStyleClass().add("cards-line");
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();

        double centerWidth = width * 0.54;
        double centerHeight = height * 0.76;
        double centerX = (width - centerWidth) / 2.0;
        double centerY = (height - centerHeight) / 2.0;

        centerCard.setX(centerX);
        centerCard.setY(centerY);
        centerCard.setWidth(centerWidth);
        centerCard.setHeight(centerHeight);

        leftCard.setX(centerX - width * 0.18);
        leftCard.setY(centerY + height * 0.12);
        leftCard.setWidth(centerWidth * 0.78);
        leftCard.setHeight(centerHeight * 0.86);
        leftCard.setRotate(-12);

        rightCard.setX(centerX + width * 0.40);
        rightCard.setY(centerY + height * 0.12);
        rightCard.setWidth(centerWidth * 0.78);
        rightCard.setHeight(centerHeight * 0.86);
        rightCard.setRotate(12);

        double detailSize = centerWidth * 0.20;
        upperSquare.setX(centerX + centerWidth * 0.34);
        upperSquare.setY(centerY + centerHeight * 0.25);
        upperSquare.setWidth(detailSize);
        upperSquare.setHeight(detailSize);

        lowerSquare.setX(centerX + centerWidth * 0.46);
        lowerSquare.setY(centerY + centerHeight * 0.46);
        lowerSquare.setWidth(detailSize);
        lowerSquare.setHeight(detailSize);

        leftStrokeTop.setStartX(centerX - width * 0.12);
        leftStrokeTop.setStartY(centerY + centerHeight * 0.54);
        leftStrokeTop.setEndX(centerX - width * 0.16);
        leftStrokeTop.setEndY(centerY + centerHeight * 0.34);

        leftStrokeBottom.setStartX(centerX - width * 0.15);
        leftStrokeBottom.setStartY(centerY + centerHeight * 0.60);
        leftStrokeBottom.setEndX(centerX - width * 0.19);
        leftStrokeBottom.setEndY(centerY + centerHeight * 0.86);

        rightStrokeTop.setStartX(centerX + centerWidth - width * 0.01);
        rightStrokeTop.setStartY(centerY + centerHeight * 0.44);
        rightStrokeTop.setEndX(centerX + centerWidth + width * 0.14);
        rightStrokeTop.setEndY(centerY + centerHeight * 0.50);

        rightStrokeBottom.setStartX(centerX + centerWidth);
        rightStrokeBottom.setStartY(centerY + centerHeight * 0.80);
        rightStrokeBottom.setEndX(centerX + centerWidth + width * 0.13);
        rightStrokeBottom.setEndY(centerY + centerHeight * 0.86);
    }
}
