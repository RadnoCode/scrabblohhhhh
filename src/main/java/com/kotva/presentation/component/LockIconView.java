package com.kotva.presentation.component;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;

/**
 * Draws the lock icon.
 */
public class LockIconView extends Pane {
    private final Rectangle body;
    private final Arc arc;

    public LockIconView() {
        body = new Rectangle();
        arc = new Arc();

        initializeShapes();
        getChildren().addAll(arc, body);
    }

    private void initializeShapes() {
        getStyleClass().add("lock-icon");
        body.getStyleClass().add("lock-body");
        arc.getStyleClass().add("lock-arc");
        arc.setType(ArcType.OPEN);
    }

        @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();

        body.setX(width * 0.20);
        body.setY(height * 0.48);
        body.setWidth(width * 0.60);
        body.setHeight(height * 0.36);

        arc.setCenterX(width * 0.50);
        arc.setCenterY(height * 0.45);
        arc.setRadiusX(width * 0.20);
        arc.setRadiusY(height * 0.24);
        arc.setStartAngle(30);
        arc.setLength(120);
    }
}
