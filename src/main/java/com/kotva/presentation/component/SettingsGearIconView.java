package com.kotva.presentation.component;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * SettingsGearIconView draws a simple gear-like icon for the settings page.
 */
public class SettingsGearIconView extends Pane {
    private final Rectangle panelFrame;
    private final Circle outerCircle;
    private final Circle innerCircle;
    private final Rectangle[] teeth;

    public SettingsGearIconView() {
        panelFrame = new Rectangle();
        outerCircle = new Circle();
        innerCircle = new Circle();
        teeth = new Rectangle[8];

        initializeShapes();
        getChildren().add(panelFrame);
        for (Rectangle tooth : teeth) {
            getChildren().add(tooth);
        }
        getChildren().addAll(outerCircle, innerCircle);
    }

    private void initializeShapes() {
        getStyleClass().add("settings-icon");
        panelFrame.getStyleClass().add("settings-panel-frame");
        outerCircle.getStyleClass().add("gear-outer-circle");
        innerCircle.getStyleClass().add("gear-inner-circle");

        for (int i = 0; i < teeth.length; i++) {
            teeth[i] = new Rectangle();
            teeth[i].getStyleClass().add("gear-tooth");
        }
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();

        panelFrame.setX(width * 0.08);
        panelFrame.setY(height * 0.08);
        panelFrame.setWidth(width * 0.84);
        panelFrame.setHeight(height * 0.84);

        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double outerRadius = Math.min(width, height) * 0.22;
        double innerRadius = outerRadius * 0.44;
        double toothWidth = outerRadius * 0.22;
        double toothHeight = outerRadius * 0.52;
        double toothDistance = outerRadius * 1.08;

        outerCircle.setCenterX(centerX);
        outerCircle.setCenterY(centerY);
        outerCircle.setRadius(outerRadius);

        innerCircle.setCenterX(centerX);
        innerCircle.setCenterY(centerY);
        innerCircle.setRadius(innerRadius);

        for (int i = 0; i < teeth.length; i++) {
            double angle = i * 45.0;
            Rectangle tooth = teeth[i];
            tooth.setWidth(toothWidth);
            tooth.setHeight(toothHeight);
            double radians = Math.toRadians(angle);
            double toothCenterX = centerX + Math.sin(radians) * toothDistance;
            double toothCenterY = centerY - Math.cos(radians) * toothDistance;
            tooth.setX(toothCenterX - toothWidth / 2.0);
            tooth.setY(toothCenterY - toothHeight / 2.0);
            tooth.setRotate(angle);
        }
    }
}
