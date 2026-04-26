package com.kotva.presentation.component;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * Draws the help icon.
 */
public class HelpIconView extends Pane {
    private final Rectangle panelFrame;
    private final Circle bulbCircle;
    private final Rectangle base;
    private final Polygon bottomTriangle;
    private final Label questionLabel;
    private final StackPane questionContainer;

    public HelpIconView() {
        panelFrame = new Rectangle();
        bulbCircle = new Circle();
        base = new Rectangle();
        bottomTriangle = new Polygon();
        questionLabel = new Label("?");
        questionContainer = new StackPane(questionLabel);

        initializeShapes();
        getChildren().addAll(panelFrame, bulbCircle, base, bottomTriangle, questionContainer);
    }

    private void initializeShapes() {
        getStyleClass().add("help-icon");
        panelFrame.getStyleClass().add("help-panel-frame");
        bulbCircle.getStyleClass().add("help-bulb-circle");
        base.getStyleClass().add("help-bulb-base");
        bottomTriangle.getStyleClass().add("help-bulb-base");
        questionLabel.getStyleClass().add("help-question-label");
    }

        @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();

        panelFrame.setX(width * 0.10);
        panelFrame.setY(height * 0.08);
        panelFrame.setWidth(width * 0.80);
        panelFrame.setHeight(height * 0.84);

        double centerX = width * 0.50;
        double bulbCenterY = height * 0.48;
        double bulbRadius = Math.min(width, height) * 0.22;

        bulbCircle.setCenterX(centerX);
        bulbCircle.setCenterY(bulbCenterY);
        bulbCircle.setRadius(bulbRadius);

        questionContainer.resizeRelocate(
            centerX - bulbRadius * 0.42,
            bulbCenterY - bulbRadius * 0.62,
            bulbRadius * 0.84,
            bulbRadius * 1.10
        );

        double baseWidth = bulbRadius * 0.58;
        double baseHeight = bulbRadius * 0.42;
        double baseX = centerX - baseWidth / 2.0;
        double baseY = bulbCenterY + bulbRadius * 0.92;

        base.setX(baseX);
        base.setY(baseY);
        base.setWidth(baseWidth);
        base.setHeight(baseHeight);

        bottomTriangle.getPoints().setAll(
            centerX - baseWidth * 0.20, baseY + baseHeight + 12,
            centerX + baseWidth * 0.20, baseY + baseHeight + 12,
            centerX, baseY + baseHeight + 32
        );
    }
}
