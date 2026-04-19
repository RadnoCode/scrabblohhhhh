package com.kotva.presentation.component;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * SearchIconView draws a small magnifier icon for the room search field.
 */
public class SearchIconView extends Pane {
    private final Circle lens;
    private final Line handle;

    public SearchIconView() {
        this.lens = new Circle();
        this.handle = new Line();

        getStyleClass().add("search-icon");
        lens.getStyleClass().add("search-icon-lens");
        handle.getStyleClass().add("search-icon-handle");

        setPrefSize(22, 22);
        setMinSize(22, 22);
        setMaxSize(22, 22);

        getChildren().addAll(lens, handle);
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        double radius = Math.min(width, height) * 0.28;
        double centerX = width * 0.42;
        double centerY = height * 0.42;

        lens.setCenterX(centerX);
        lens.setCenterY(centerY);
        lens.setRadius(radius);

        handle.setStartX(centerX + radius * 0.65);
        handle.setStartY(centerY + radius * 0.65);
        handle.setEndX(width * 0.84);
        handle.setEndY(height * 0.84);
    }
}
