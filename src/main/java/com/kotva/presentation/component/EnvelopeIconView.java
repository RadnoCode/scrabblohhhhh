package com.kotva.presentation.component;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class EnvelopeIconView extends Pane {
    private final Rectangle envelopeBody;
    private final Polygon topFlap;
    private final Line leftFold;
    private final Line rightFold;
    private final Rectangle seal;

    public EnvelopeIconView() {
        envelopeBody = new Rectangle();
        topFlap = new Polygon();
        leftFold = new Line();
        rightFold = new Line();
        seal = new Rectangle();

        initializeShapes();
        getChildren().addAll(envelopeBody, topFlap, leftFold, rightFold, seal);
    }

    private void initializeShapes() {
        getStyleClass().add("envelope-icon");

        envelopeBody.getStyleClass().add("envelope-body");
        topFlap.getStyleClass().add("envelope-flap");
        leftFold.getStyleClass().add("envelope-fold-line");
        rightFold.getStyleClass().add("envelope-fold-line");
        seal.getStyleClass().add("envelope-seal");

        setPrefSize(380, 270);
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
    }

        @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();

        double x = width * 0.08;
        double y = height * 0.10;
        double envelopeWidth = width * 0.84;
        double envelopeHeight = height * 0.72;

        double left = x;
        double right = x + envelopeWidth;
        double top = y;
        double bottom = y + envelopeHeight;
        double centerX = x + envelopeWidth / 2.0;
        double centerY = y + envelopeHeight / 2.0;

        envelopeBody.setX(left);
        envelopeBody.setY(top);
        envelopeBody.setWidth(envelopeWidth);
        envelopeBody.setHeight(envelopeHeight);

        topFlap.getPoints().setAll(
            left, top,
            centerX, centerY,
            right, top
        );

        leftFold.setStartX(left);
        leftFold.setStartY(bottom);
        leftFold.setEndX(centerX);
        leftFold.setEndY(centerY);

        rightFold.setStartX(right);
        rightFold.setStartY(bottom);
        rightFold.setEndX(centerX);
        rightFold.setEndY(centerY);

        double sealWidth = envelopeWidth * 0.075;
        double sealHeight = envelopeHeight * 0.14;
        seal.setX(centerX - sealWidth / 2.0);
        seal.setY(centerY - sealHeight * 0.15);
        seal.setWidth(sealWidth);
        seal.setHeight(sealHeight);
        seal.setArcWidth(10);
        seal.setArcHeight(10);
    }

    public Polygon getTopFlap() {
        return topFlap;
    }

    public Rectangle getSeal() {
        return seal;
    }

    public Rectangle getEnvelopeBody() {
        return envelopeBody;
    }
}