package com.kotva.presentation.component;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * RoomPanelView draws the reusable online-room frame used by room search
 * and room waiting pages. It replaces the old ticket component with a
 * more neutral room panel component while keeping the same visual idea.
 */
/**
 * Shows room information for LAN screens.
 */
public class RoomPanelView extends Pane {
    private enum Variant {
        SEARCH,
        WAITING
    }

    private final Variant variant;
    private final Rectangle outline;
    private final Rectangle mainPanel;
    private final Rectangle sidePanel;
    private final Line dividerLine;
    private final List<Circle> edgeNotches;
    private final List<Circle> dividerDots;
    private final List<Circle> crossDots;

    private RoomPanelView(Variant variant, double width, double height) {
        this.variant = variant;
        this.outline = new Rectangle();
        this.mainPanel = new Rectangle();
        this.sidePanel = new Rectangle();
        this.dividerLine = new Line();
        this.edgeNotches = new ArrayList<>();
        this.dividerDots = new ArrayList<>();
        this.crossDots = new ArrayList<>();

        getStyleClass().add("room-panel");
        outline.getStyleClass().add("room-panel-outline");
        mainPanel.getStyleClass().add("room-panel-inner-frame");
        sidePanel.getStyleClass().add("room-panel-inner-frame");
        dividerLine.getStyleClass().add("room-panel-divider-line");

        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);

        getChildren().addAll(outline, mainPanel, sidePanel, dividerLine);
    }

    public static RoomPanelView createSearchPanel() {
        return new RoomPanelView(Variant.SEARCH, 480, 230);
    }

    public static RoomPanelView createWaitingPanel() {
        return new RoomPanelView(Variant.WAITING, 500, 280);
    }

    @Override
    protected void layoutChildren() {
        clearDynamicShapes();

        double width = getWidth();
        double height = getHeight();
        double arc = 24;

        outline.setX(0);
        outline.setY(0);
        outline.setWidth(width);
        outline.setHeight(height);
        outline.setArcWidth(arc);
        outline.setArcHeight(arc);

        if (variant == Variant.SEARCH) {
            layoutSearchPanel(width, height);
        } else {
            layoutWaitingPanel(width, height);
        }
    }

    private void layoutSearchPanel(double width, double height) {
        double padding = 22;
        double dividerX = width * 0.70;

        mainPanel.setVisible(true);
        sidePanel.setVisible(true);
        dividerLine.setVisible(true);

        mainPanel.setX(padding);
        mainPanel.setY(padding);
        mainPanel.setWidth(dividerX - padding - 16);
        mainPanel.setHeight(height - padding * 2);

        sidePanel.setX(dividerX + 18);
        sidePanel.setY(padding);
        sidePanel.setWidth(width - sidePanel.getX() - padding);
        sidePanel.setHeight(height - padding * 2);

        dividerLine.setStartX(dividerX);
        dividerLine.setStartY(padding + 6);
        dividerLine.setEndX(dividerX);
        dividerLine.setEndY(height - padding - 6);

        addEdgeCutout(width * 0.50, 0);
        addEdgeCutout(width * 0.50, height);
        addEdgeCutout(0, height * 0.50);
        addEdgeCutout(width, height * 0.50);

        addVerticalDividerDots(dividerX, padding + 18, height - padding - 18, 10);
    }

    private void layoutWaitingPanel(double width, double height) {
        mainPanel.setVisible(false);
        sidePanel.setVisible(false);
        dividerLine.setVisible(false);

        addEdgeDotsAcross(width, 24, 28);
        addEdgeDotsAcross(width, height - 24, 28);
        addEdgeDotsDown(0, height, 22);
        addEdgeDotsDown(width, height, 22);

        double centerX = width / 2;
        double centerY = height / 2;

        addHorizontalCrossDots(28, width - 28, centerY, 11);
        addVerticalCrossDots(centerX, 28, height - 28, 7);
    }

    private void addEdgeDotsAcross(double width, double y, int count) {
        for (int index = 0; index < count; index++) {
            double x = 22 + index * ((width - 44) / (count - 1));
            if (Math.abs(x - (width / 2)) < 12) {
                continue;
            }
            addEdgeCutout(x, y);
        }
    }

    private void addEdgeDotsDown(double x, double height, int count) {
        for (int index = 0; index < count; index++) {
            double y = 22 + index * ((height - 44) / (count - 1));
            addEdgeCutout(x, y);
        }
    }

    private void addVerticalDividerDots(double x, double startY, double endY, int count) {
        for (int index = 0; index < count; index++) {
            double y = startY + index * ((endY - startY) / (count - 1));
            Circle dot = createDot(x, y, 5.5, "room-panel-divider-dot");
            dividerDots.add(dot);
            getChildren().add(dot);
        }
    }

    private void addHorizontalCrossDots(double startX, double endX, double y, int count) {
        for (int index = 0; index < count; index++) {
            double x = startX + index * ((endX - startX) / (count - 1));
            Circle dot = createDot(x, y, 6.4, "room-panel-divider-dot");
            crossDots.add(dot);
            getChildren().add(dot);
        }
    }

    private void addVerticalCrossDots(double x, double startY, double endY, int count) {
        for (int index = 0; index < count; index++) {
            double y = startY + index * ((endY - startY) / (count - 1));
            Circle dot = createDot(x, y, 6.4, "room-panel-divider-dot");
            crossDots.add(dot);
            getChildren().add(dot);
        }
    }

    private void addEdgeCutout(double centerX, double centerY) {
        Circle cutout = createDot(centerX, centerY, 10, "room-panel-cutout");
        edgeNotches.add(cutout);
        getChildren().add(cutout);
    }

    private Circle createDot(double centerX, double centerY, double radius, String styleClass) {
        Circle circle = new Circle(centerX, centerY, radius);
        circle.getStyleClass().add(styleClass);
        return circle;
    }

    private void clearDynamicShapes() {
        getChildren().removeAll(edgeNotches);
        getChildren().removeAll(dividerDots);
        getChildren().removeAll(crossDots);
        edgeNotches.clear();
        dividerDots.clear();
        crossDots.clear();
    }
}
