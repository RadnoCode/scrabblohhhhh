package com.kotva.presentation.component;

import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class RackHandoffOverlayView extends Pane {
    private static final Duration DEFAULT_VISIBLE_DURATION = Duration.seconds(5);
    private static final String PROMPT_TEXT = "Please let the next player sit at the computer";

    private final RackView rackView;
    private final Rectangle rackCover;
    private final Label promptLabel;
    private final CommonButton readyButton;
    private final VBox contentBox;
    private final PauseTransition hideTransition;

    public RackHandoffOverlayView(RackView rackView) {
        this.rackView = Objects.requireNonNull(rackView, "rackView cannot be null.");
        this.rackCover = new Rectangle();
        this.promptLabel = new Label(PROMPT_TEXT);
        this.readyButton = new CommonButton("Ready");
        this.readyButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);
        this.contentBox = new VBox(10);
        this.hideTransition = new PauseTransition(DEFAULT_VISIBLE_DURATION);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("rack-handoff-overlay");
        setManaged(false);
        setVisible(false);
        setPickOnBounds(false);

        rackCover.getStyleClass().add("rack-handoff-cover");
        rackCover.setManaged(false);

        promptLabel.getStyleClass().add("rack-handoff-text");
        promptLabel.setWrapText(true);
        promptLabel.setAlignment(Pos.CENTER);

        readyButton.getStyleClass().add("rack-handoff-ready-button");
        readyButton.applyTemplateSize(220);
        readyButton.setOnAction(event -> hideOverlay());

        contentBox.getStyleClass().add("rack-handoff-content");
        contentBox.setManaged(false);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(promptLabel, readyButton);

        getChildren().addAll(rackCover, contentBox);

        hideTransition.setOnFinished(event -> hideOverlay());
    }

    public void showOverlay() {
        Pane parentLayer = requireParentLayer();
        Bounds rackBounds = rackView.localToScene(rackView.getBoundsInLocal());
        if (rackBounds == null) {
            return;
        }

        Point2D topLeft = parentLayer.sceneToLocal(rackBounds.getMinX(), rackBounds.getMinY());
        rackCover.setX(topLeft.getX());
        rackCover.setY(topLeft.getY());
        rackCover.setWidth(rackBounds.getWidth());
        rackCover.setHeight(rackBounds.getHeight());

        applyCss();
        layout();
        contentBox.applyCss();
        contentBox.autosize();

        double contentX = topLeft.getX() + (rackBounds.getWidth() - contentBox.getWidth()) / 2.0;
        double contentY = topLeft.getY() + Math.max(12, (rackBounds.getHeight() - contentBox.getHeight()) / 2.0 - 6);
        contentBox.relocate(contentX, contentY);

        setVisible(true);
        rackView.setMouseTransparent(true);
        toFront();
        hideTransition.stop();
        hideTransition.playFromStart();
    }

    public void hideOverlay() {
        hideTransition.stop();
        setVisible(false);
        rackView.setMouseTransparent(false);
    }

    public boolean isOverlayVisible() {
        return isVisible();
    }

    private Pane requireParentLayer() {
        if (!(getParent() instanceof Pane pane)) {
            throw new IllegalStateException("RackHandoffOverlayView must be added to a Pane.");
        }
        return pane;
    }
}
