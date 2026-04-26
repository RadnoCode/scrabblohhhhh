package com.kotva.presentation.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Plays exit animations for option screens.
 */
final class OptionSceneExitAnimationManager {
    private static final Duration TITLE_EXIT_DURATION = Duration.seconds(0.3);
    private static final Duration FEATURED_EXIT_DURATION = Duration.seconds(0.7);
    private static final Duration OPTION_EXIT_DURATION = Duration.seconds(0.4);
    private static final Duration OPTION_STAGGER = Duration.seconds(0.05);
    private static final double ANTICIPATION_PORTION = 0.18;
    private static final double PAUSE_PORTION = 0.10;
    private static final double TITLE_EXIT_OFFSET_Y = 180;
    private static final double FEATURED_EXIT_OFFSET_Y = 420;
    private static final double OPTION_EXIT_OFFSET_X = 420;
    private static final double TITLE_EXIT_SAFE_MARGIN = 32;
    private static final double FEATURED_EXIT_SAFE_MARGIN = 48;
    private static final double OPTION_EXIT_SAFE_MARGIN = 48;
    private static final double TITLE_ANTICIPATION_Y = 20;
    private static final double FEATURED_ANTICIPATION_Y = 18;
    private static final double OPTION_ANTICIPATION_X = 18;

    private final Node interactionLayer;
    private final Node titleNode;
    private final Node featuredNode;
    private final List<Node> orderedOptionNodes;
    private boolean playing;

    OptionSceneExitAnimationManager(
        Node interactionLayer,
        Node titleNode,
        Node featuredNode,
        List<? extends Node> orderedOptionNodes) {
        this.interactionLayer = Objects.requireNonNull(interactionLayer, "interactionLayer cannot be null.");
        this.titleNode = Objects.requireNonNull(titleNode, "titleNode cannot be null.");
        this.featuredNode = Objects.requireNonNull(featuredNode, "featuredNode cannot be null.");
        this.orderedOptionNodes = List.copyOf(
            Objects.requireNonNull(orderedOptionNodes, "orderedOptionNodes cannot be null."));
    }

    void play(Node clickedNode, Runnable onFinished) {
        if (playing) {
            return;
        }
        playing = true;
        interactionLayer.setMouseTransparent(true);

        ParallelTransition featuredAndOptions = new ParallelTransition();
        featuredAndOptions.getChildren().add(createFeaturedExitTransition());

        List<Node> orderedExitNodes = resolveOptionExitOrder(clickedNode);
        for (int optionIndex = 0; optionIndex < orderedExitNodes.size(); optionIndex++) {
            featuredAndOptions.getChildren().add(createOptionExitTransition(orderedExitNodes.get(optionIndex), optionIndex));
        }

        ParallelTransition transition = new ParallelTransition(
            createTitleExitTransition(),
            featuredAndOptions);
        transition.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        transition.playFromStart();
    }

    private List<Node> resolveOptionExitOrder(Node clickedNode) {
        List<Node> orderedNodes = new ArrayList<>(orderedOptionNodes);
        if (clickedNode != null && orderedNodes.remove(clickedNode)) {
            orderedNodes.add(clickedNode);
        }
        return orderedNodes;
    }

    private SequentialTransition createTitleExitTransition() {
        double titleExitTargetY = resolveTitleExitTargetY();
        return createVerticalAnticipationExit(
            titleNode,
            TITLE_EXIT_DURATION,
            titleNode.getTranslateY(),
            titleNode.getTranslateY() + TITLE_ANTICIPATION_Y,
            titleExitTargetY);
    }

    private SequentialTransition createFeaturedExitTransition() {
        double featuredExitTargetY = resolveFeaturedExitTargetY();
        return createVerticalAnticipationExit(
            featuredNode,
            FEATURED_EXIT_DURATION,
            featuredNode.getTranslateY(),
            featuredNode.getTranslateY() - FEATURED_ANTICIPATION_Y,
            featuredExitTargetY);
    }

    private SequentialTransition createOptionExitTransition(Node optionNode, int optionIndex) {
        PauseTransition delay = new PauseTransition(OPTION_STAGGER.multiply(optionIndex));
        double optionExitTargetX = resolveOptionExitTargetX(optionNode);
        SequentialTransition exit = createHorizontalAnticipationExit(
            optionNode,
            OPTION_EXIT_DURATION,
            optionNode.getTranslateX(),
            optionNode.getTranslateX() - OPTION_ANTICIPATION_X,
            optionExitTargetX);
        return new SequentialTransition(delay, exit);
    }

    private SequentialTransition createVerticalAnticipationExit(
        Node node,
        Duration totalDuration,
        double startY,
        double anticipationY,
        double endY) {
        TranslateTransition anticipation = new TranslateTransition(totalDuration.multiply(ANTICIPATION_PORTION), node);
        anticipation.setFromY(startY);
        anticipation.setToY(anticipationY);
        anticipation.setInterpolator(Interpolator.SPLINE(0.30, 0.0, 0.45, 1.0));

        PauseTransition pause = new PauseTransition(totalDuration.multiply(PAUSE_PORTION));

        TranslateTransition departure = new TranslateTransition(
            totalDuration.multiply(1.0 - ANTICIPATION_PORTION - PAUSE_PORTION),
            node);
        departure.setFromY(anticipationY);
        departure.setToY(endY);
        departure.setInterpolator(Interpolator.SPLINE(0.35, 0.0, 0.75, 1.0));
        return new SequentialTransition(anticipation, pause, departure);
    }

    private SequentialTransition createHorizontalAnticipationExit(
        Node node,
        Duration totalDuration,
        double startX,
        double anticipationX,
        double endX) {
        TranslateTransition anticipation = new TranslateTransition(totalDuration.multiply(ANTICIPATION_PORTION), node);
        anticipation.setFromX(startX);
        anticipation.setToX(anticipationX);
        anticipation.setInterpolator(Interpolator.SPLINE(0.30, 0.0, 0.45, 1.0));

        PauseTransition pause = new PauseTransition(totalDuration.multiply(PAUSE_PORTION));

        TranslateTransition departure = new TranslateTransition(
            totalDuration.multiply(1.0 - ANTICIPATION_PORTION - PAUSE_PORTION),
            node);
        departure.setFromX(anticipationX);
        departure.setToX(endX);
        departure.setInterpolator(Interpolator.SPLINE(0.35, 0.0, 0.75, 1.0));
        return new SequentialTransition(anticipation, pause, departure);
    }

    private double resolveOptionExitTargetX(Node optionNode) {
        if (interactionLayer.getScene() == null) {
            return optionNode.getTranslateX() + OPTION_EXIT_OFFSET_X;
        }

        Bounds sceneBounds = optionNode.localToScene(optionNode.getBoundsInLocal());
        if (sceneBounds == null) {
            return optionNode.getTranslateX() + OPTION_EXIT_OFFSET_X;
        }

        double sceneWidth = interactionLayer.getScene().getWidth();
        double requiredShift = (sceneWidth - sceneBounds.getMinX()) + OPTION_EXIT_SAFE_MARGIN;
        return optionNode.getTranslateX() + Math.max(requiredShift, OPTION_EXIT_OFFSET_X);
    }

    private double resolveTitleExitTargetY() {
        if (interactionLayer.getScene() == null) {
            return titleNode.getTranslateY() - TITLE_EXIT_OFFSET_Y;
        }

        Bounds sceneBounds = titleNode.localToScene(titleNode.getBoundsInLocal());
        if (sceneBounds == null) {
            return titleNode.getTranslateY() - TITLE_EXIT_OFFSET_Y;
        }

        double requiredShift = sceneBounds.getMaxY() + TITLE_EXIT_SAFE_MARGIN;
        return titleNode.getTranslateY() - Math.max(requiredShift, TITLE_EXIT_OFFSET_Y);
    }

    private double resolveFeaturedExitTargetY() {
        if (interactionLayer.getScene() == null) {
            return featuredNode.getTranslateY() + FEATURED_EXIT_OFFSET_Y;
        }

        Bounds sceneBounds = featuredNode.localToScene(featuredNode.getBoundsInLocal());
        if (sceneBounds == null) {
            return featuredNode.getTranslateY() + FEATURED_EXIT_OFFSET_Y;
        }

        double sceneHeight = interactionLayer.getScene().getHeight();
        double requiredShift = (sceneHeight - sceneBounds.getMinY()) + FEATURED_EXIT_SAFE_MARGIN;
        return featuredNode.getTranslateY() + Math.max(requiredShift, FEATURED_EXIT_OFFSET_Y);
    }
}
