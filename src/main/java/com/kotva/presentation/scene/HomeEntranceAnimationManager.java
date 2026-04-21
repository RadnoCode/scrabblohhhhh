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

final class HomeEntranceAnimationManager {
    private static final Duration ENVELOPE_DURATION = Duration.seconds(0.7);
    private static final Duration BUTTON_DURATION = Duration.seconds(0.4);
    private static final Duration BUTTON_STAGGER = Duration.seconds(0.1);
    private static final Duration TITLE_DURATION = Duration.seconds(0.3);
    private static final double APPROACH_PORTION = 0.84;
    private static final double ENVELOPE_ENTRY_OFFSET_Y = 420;
    private static final double BUTTON_ENTRY_OFFSET_X = 420;
    private static final double TITLE_ENTRY_OFFSET_Y = 180;
    private static final double ENVELOPE_ENTRY_SAFE_MARGIN = 48;
    private static final double BUTTON_ENTRY_SAFE_MARGIN = 48;
    private static final double TITLE_ENTRY_SAFE_MARGIN = 32;
    private static final double ENVELOPE_OVERSHOOT_Y = 18;
    private static final double BUTTON_OVERSHOOT_X = 16;

    private final Node triggerNode;
    private final Node titleNode;
    private final Node envelopeNode;
    private final List<Node> orderedButtons;
    private final double titleRestingTranslateY;
    private final double envelopeRestingTranslateY;
    private final List<Double> buttonRestingTranslateX;
    private double titleEntryStartTranslateY;
    private double envelopeEntryStartTranslateY;
    private List<Double> buttonEntryStartTranslateX;
    private boolean played;

    HomeEntranceAnimationManager(
        Node triggerNode,
        Node titleNode,
        Node envelopeNode,
        List<? extends Node> orderedButtons) {
        this.triggerNode = Objects.requireNonNull(triggerNode, "triggerNode cannot be null.");
        this.titleNode = Objects.requireNonNull(titleNode, "titleNode cannot be null.");
        this.envelopeNode = Objects.requireNonNull(envelopeNode, "envelopeNode cannot be null.");
        this.orderedButtons = List.copyOf(Objects.requireNonNull(orderedButtons, "orderedButtons cannot be null."));
        this.titleRestingTranslateY = titleNode.getTranslateY();
        this.envelopeRestingTranslateY = envelopeNode.getTranslateY();
        this.buttonRestingTranslateX = this.orderedButtons.stream()
            .map(Node::getTranslateX)
            .toList();
    }

    void install() {
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

        ParallelTransition envelopeAndButtons = new ParallelTransition();
        envelopeAndButtons.getChildren().add(createEnvelopeTransition());
        for (int buttonIndex = 0; buttonIndex < orderedButtons.size(); buttonIndex++) {
            envelopeAndButtons.getChildren().add(createButtonTransition(buttonIndex));
        }

        SequentialTransition entranceSequence = new SequentialTransition(
            envelopeAndButtons,
            createTitleTransition());
        entranceSequence.playFromStart();
    }

    private void prepareInitialState() {
        titleEntryStartTranslateY = resolveTitleEntryStartTranslateY();
        envelopeEntryStartTranslateY = resolveEnvelopeEntryStartTranslateY();
        buttonEntryStartTranslateX = orderedButtons.stream()
            .map(this::resolveButtonEntryStartTranslateX)
            .toList();

        envelopeNode.setTranslateY(envelopeEntryStartTranslateY);
        for (int buttonIndex = 0; buttonIndex < orderedButtons.size(); buttonIndex++) {
            orderedButtons.get(buttonIndex).setTranslateX(buttonEntryStartTranslateX.get(buttonIndex));
        }
        titleNode.setTranslateY(titleEntryStartTranslateY);
    }

    private SequentialTransition createEnvelopeTransition() {
        return createVerticalEntranceWithOvershoot(
            envelopeNode,
            ENVELOPE_DURATION,
            envelopeEntryStartTranslateY,
            envelopeRestingTranslateY - ENVELOPE_OVERSHOOT_Y,
            envelopeRestingTranslateY);
    }

    private SequentialTransition createButtonTransition(int buttonIndex) {
        Node button = orderedButtons.get(buttonIndex);
        PauseTransition delay = new PauseTransition(BUTTON_STAGGER.multiply(buttonIndex));
        SequentialTransition entrance = createHorizontalEntranceWithOvershoot(
            button,
            BUTTON_DURATION,
            buttonEntryStartTranslateX.get(buttonIndex),
            buttonRestingTranslateX.get(buttonIndex) - BUTTON_OVERSHOOT_X,
            buttonRestingTranslateX.get(buttonIndex));
        return new SequentialTransition(delay, entrance);
    }

    private TranslateTransition createTitleTransition() {
        TranslateTransition transition = new TranslateTransition(TITLE_DURATION, titleNode);
        transition.setFromY(titleEntryStartTranslateY);
        transition.setToY(titleRestingTranslateY);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
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

    private double resolveEnvelopeEntryStartTranslateY() {
        if (triggerNode.getScene() == null) {
            return envelopeRestingTranslateY + ENVELOPE_ENTRY_OFFSET_Y;
        }

        Bounds sceneBounds = envelopeNode.localToScene(envelopeNode.getBoundsInLocal());
        if (sceneBounds == null) {
            return envelopeRestingTranslateY + ENVELOPE_ENTRY_OFFSET_Y;
        }

        double sceneHeight = triggerNode.getScene().getHeight();
        double requiredShift = (sceneHeight - sceneBounds.getMinY()) + ENVELOPE_ENTRY_SAFE_MARGIN;
        return envelopeRestingTranslateY + Math.max(requiredShift, ENVELOPE_ENTRY_OFFSET_Y);
    }

    private double resolveButtonEntryStartTranslateX(Node button) {
        if (triggerNode.getScene() == null) {
            return button.getTranslateX() + BUTTON_ENTRY_OFFSET_X;
        }

        Bounds sceneBounds = button.localToScene(button.getBoundsInLocal());
        if (sceneBounds == null) {
            return button.getTranslateX() + BUTTON_ENTRY_OFFSET_X;
        }

        double sceneWidth = triggerNode.getScene().getWidth();
        double requiredShift = (sceneWidth - sceneBounds.getMinX()) + BUTTON_ENTRY_SAFE_MARGIN;
        return button.getTranslateX() + Math.max(requiredShift, BUTTON_ENTRY_OFFSET_X);
    }

    private SequentialTransition createVerticalEntranceWithOvershoot(
        Node node,
        Duration totalDuration,
        double startY,
        double overshootY,
        double endY) {
        TranslateTransition approach = new TranslateTransition(totalDuration.multiply(APPROACH_PORTION), node);
        approach.setFromY(startY);
        approach.setToY(overshootY);
        approach.setInterpolator(Interpolator.SPLINE(0.18, 0.82, 0.22, 1.0));

        TranslateTransition settle = new TranslateTransition(totalDuration.multiply(1.0 - APPROACH_PORTION), node);
        settle.setFromY(overshootY);
        settle.setToY(endY);
        settle.setInterpolator(Interpolator.SPLINE(0.20, 0.0, 0.20, 1.0));
        return new SequentialTransition(approach, settle);
    }

    private SequentialTransition createHorizontalEntranceWithOvershoot(
        Node node,
        Duration totalDuration,
        double startX,
        double overshootX,
        double endX) {
        TranslateTransition approach = new TranslateTransition(totalDuration.multiply(APPROACH_PORTION), node);
        approach.setFromX(startX);
        approach.setToX(overshootX);
        approach.setInterpolator(Interpolator.SPLINE(0.18, 0.82, 0.22, 1.0));

        TranslateTransition settle = new TranslateTransition(totalDuration.multiply(1.0 - APPROACH_PORTION), node);
        settle.setFromX(overshootX);
        settle.setToX(endX);
        settle.setInterpolator(Interpolator.SPLINE(0.20, 0.0, 0.20, 1.0));
        return new SequentialTransition(approach, settle);
    }
}
