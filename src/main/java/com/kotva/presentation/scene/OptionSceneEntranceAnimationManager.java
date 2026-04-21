package com.kotva.presentation.scene;

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

final class OptionSceneEntranceAnimationManager {
    static final String OPTION_ENTRANCE_INSTALLED_KEY = "option-entrance-installed";
    private static final Duration FEATURED_DURATION = Duration.seconds(0.7);
    private static final Duration OPTION_DURATION = Duration.seconds(0.4);
    private static final Duration OPTION_STAGGER = Duration.seconds(0.05);
    private static final Duration TITLE_DURATION = Duration.seconds(0.3);
    private static final Duration TITLE_START_DELAY = Duration.seconds(0.05);
    private static final double ENTRY_APPROACH_PORTION = 0.72;
    private static final double PAUSE_PORTION = 0.10;
    private static final double FEATURED_ENTRY_OFFSET_Y = 420;
    private static final double OPTION_ENTRY_OFFSET_X = 420;
    private static final double TITLE_ENTRY_OFFSET_Y = 180;
    private static final double FEATURED_ENTRY_SAFE_MARGIN = 48;
    private static final double OPTION_ENTRY_SAFE_MARGIN = 48;
    private static final double TITLE_ENTRY_SAFE_MARGIN = 32;
    private static final double FEATURED_OVERSHOOT_Y = 18;
    private static final double OPTION_OVERSHOOT_X = 16;
    private static final double TITLE_OVERSHOOT_Y = 20;

    private final Node triggerNode;
    private final Node titleNode;
    private final Node featuredNode;
    private final List<Node> orderedOptionNodes;
    private final double titleRestingTranslateY;
    private final double featuredRestingTranslateY;
    private final List<Double> optionRestingTranslateX;
    private double titleEntryStartTranslateY;
    private double featuredEntryStartTranslateY;
    private List<Double> optionEntryStartTranslateX;
    private boolean played;

    OptionSceneEntranceAnimationManager(
        Node triggerNode,
        Node titleNode,
        Node featuredNode,
        List<? extends Node> orderedOptionNodes) {
        this.triggerNode = Objects.requireNonNull(triggerNode, "triggerNode cannot be null.");
        this.titleNode = Objects.requireNonNull(titleNode, "titleNode cannot be null.");
        this.featuredNode = Objects.requireNonNull(featuredNode, "featuredNode cannot be null.");
        this.orderedOptionNodes = List.copyOf(
            Objects.requireNonNull(orderedOptionNodes, "orderedOptionNodes cannot be null."));
        this.titleRestingTranslateY = titleNode.getTranslateY();
        this.featuredRestingTranslateY = featuredNode.getTranslateY();
        this.optionRestingTranslateX = this.orderedOptionNodes.stream()
            .map(Node::getTranslateX)
            .toList();
    }

    void install() {
        triggerNode.getProperties().put(OPTION_ENTRANCE_INSTALLED_KEY, Boolean.TRUE);
        triggerNode.sceneProperty().addListener((observable, previousScene, currentScene) -> {
            if (currentScene != null) {
                playIfNeeded();
            }
        });
        if (triggerNode.getScene() != null) {
            playIfNeeded();
        }
    }

    private void playIfNeeded() {
        if (played) {
            return;
        }
        played = true;

        prepareInitialState();

        ParallelTransition featuredAndOptions = new ParallelTransition();
        featuredAndOptions.getChildren().add(createFeaturedTransition());
        for (int optionIndex = 0; optionIndex < orderedOptionNodes.size(); optionIndex++) {
            featuredAndOptions.getChildren().add(createOptionTransition(optionIndex));
        }

        SequentialTransition delayedTitleEntrance = new SequentialTransition(
            new PauseTransition(TITLE_START_DELAY),
            createTitleTransition());

        ParallelTransition entranceSequence = new ParallelTransition(
            featuredAndOptions,
            delayedTitleEntrance);
        entranceSequence.playFromStart();
    }

    private void prepareInitialState() {
        titleEntryStartTranslateY = resolveTitleEntryStartTranslateY();
        featuredEntryStartTranslateY = resolveFeaturedEntryStartTranslateY();
        optionEntryStartTranslateX = orderedOptionNodes.stream()
            .map(this::resolveOptionEntryStartTranslateX)
            .toList();

        featuredNode.setTranslateY(featuredEntryStartTranslateY);
        for (int optionIndex = 0; optionIndex < orderedOptionNodes.size(); optionIndex++) {
            orderedOptionNodes.get(optionIndex).setTranslateX(optionEntryStartTranslateX.get(optionIndex));
        }
        titleNode.setTranslateY(titleEntryStartTranslateY);
    }

    private SequentialTransition createFeaturedTransition() {
        return createVerticalEntranceWithOvershoot(
            featuredNode,
            FEATURED_DURATION,
            featuredEntryStartTranslateY,
            featuredRestingTranslateY - FEATURED_OVERSHOOT_Y,
            featuredRestingTranslateY);
    }

    private SequentialTransition createOptionTransition(int optionIndex) {
        Node optionNode = orderedOptionNodes.get(optionIndex);
        PauseTransition delay = new PauseTransition(OPTION_STAGGER.multiply(optionIndex));
        SequentialTransition entrance = createHorizontalEntranceWithOvershoot(
            optionNode,
            OPTION_DURATION,
            optionEntryStartTranslateX.get(optionIndex),
            optionRestingTranslateX.get(optionIndex) - OPTION_OVERSHOOT_X,
            optionRestingTranslateX.get(optionIndex));
        return new SequentialTransition(delay, entrance);
    }

    private SequentialTransition createTitleTransition() {
        return createVerticalEntranceWithOvershoot(
            titleNode,
            TITLE_DURATION,
            titleEntryStartTranslateY,
            titleRestingTranslateY + TITLE_OVERSHOOT_Y,
            titleRestingTranslateY);
    }

    private double resolveTitleEntryStartTranslateY() {
        if (triggerNode.getScene() == null) {
            return titleRestingTranslateY - TITLE_ENTRY_OFFSET_Y;
        }

        Bounds sceneBounds = titleNode.localToScene(titleNode.getBoundsInLocal());
        if (sceneBounds == null) {
            return titleRestingTranslateY - TITLE_ENTRY_OFFSET_Y;
        }

        double requiredShift = sceneBounds.getMaxY() + TITLE_ENTRY_SAFE_MARGIN;
        return titleRestingTranslateY - Math.max(requiredShift, TITLE_ENTRY_OFFSET_Y);
    }

    private double resolveFeaturedEntryStartTranslateY() {
        if (triggerNode.getScene() == null) {
            return featuredRestingTranslateY + FEATURED_ENTRY_OFFSET_Y;
        }

        Bounds sceneBounds = featuredNode.localToScene(featuredNode.getBoundsInLocal());
        if (sceneBounds == null) {
            return featuredRestingTranslateY + FEATURED_ENTRY_OFFSET_Y;
        }

        double sceneHeight = triggerNode.getScene().getHeight();
        double requiredShift = (sceneHeight - sceneBounds.getMinY()) + FEATURED_ENTRY_SAFE_MARGIN;
        return featuredRestingTranslateY + Math.max(requiredShift, FEATURED_ENTRY_OFFSET_Y);
    }

    private double resolveOptionEntryStartTranslateX(Node optionNode) {
        if (triggerNode.getScene() == null) {
            return optionNode.getTranslateX() + OPTION_ENTRY_OFFSET_X;
        }

        Bounds sceneBounds = optionNode.localToScene(optionNode.getBoundsInLocal());
        if (sceneBounds == null) {
            return optionNode.getTranslateX() + OPTION_ENTRY_OFFSET_X;
        }

        double sceneWidth = triggerNode.getScene().getWidth();
        double requiredShift = (sceneWidth - sceneBounds.getMinX()) + OPTION_ENTRY_SAFE_MARGIN;
        return optionNode.getTranslateX() + Math.max(requiredShift, OPTION_ENTRY_OFFSET_X);
    }

    private SequentialTransition createVerticalEntranceWithOvershoot(
        Node node,
        Duration totalDuration,
        double startY,
        double overshootY,
        double endY) {
        TranslateTransition approach = new TranslateTransition(totalDuration.multiply(ENTRY_APPROACH_PORTION), node);
        approach.setFromY(startY);
        approach.setToY(overshootY);
        approach.setInterpolator(Interpolator.SPLINE(0.35, 0.0, 0.75, 1.0));

        PauseTransition pause = new PauseTransition(totalDuration.multiply(PAUSE_PORTION));

        TranslateTransition settle = new TranslateTransition(
            totalDuration.multiply(1.0 - ENTRY_APPROACH_PORTION - PAUSE_PORTION),
            node);
        settle.setFromY(overshootY);
        settle.setToY(endY);
        settle.setInterpolator(Interpolator.SPLINE(0.30, 0.0, 0.45, 1.0));
        return new SequentialTransition(approach, pause, settle);
    }

    private SequentialTransition createHorizontalEntranceWithOvershoot(
        Node node,
        Duration totalDuration,
        double startX,
        double overshootX,
        double endX) {
        TranslateTransition approach = new TranslateTransition(totalDuration.multiply(ENTRY_APPROACH_PORTION), node);
        approach.setFromX(startX);
        approach.setToX(overshootX);
        approach.setInterpolator(Interpolator.SPLINE(0.35, 0.0, 0.75, 1.0));

        PauseTransition pause = new PauseTransition(totalDuration.multiply(PAUSE_PORTION));

        TranslateTransition settle = new TranslateTransition(
            totalDuration.multiply(1.0 - ENTRY_APPROACH_PORTION - PAUSE_PORTION),
            node);
        settle.setFromX(overshootX);
        settle.setToX(endX);
        settle.setInterpolator(Interpolator.SPLINE(0.30, 0.0, 0.45, 1.0));
        return new SequentialTransition(approach, pause, settle);
    }
}
