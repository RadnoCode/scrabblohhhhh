package com.kotva.presentation.scene;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.geometry.Bounds;
import javafx.util.Duration;

final class HomeSelectionAnimationManager {
    enum ButtonKey {
        PLAY,
        TUTORIAL,
        SETTINGS,
        HELP
    }

    private static final Duration TITLE_EXIT_DURATION = Duration.seconds(0.3);
    private static final Duration ENVELOPE_EXIT_DURATION = Duration.seconds(0.7);
    private static final Duration BUTTON_EXIT_DURATION = Duration.seconds(0.4);
    private static final Duration BUTTON_STAGGER = Duration.seconds(0.05);
    private static final double ANTICIPATION_PORTION = 0.18;
    private static final double PAUSE_PORTION = 0.10;
    private static final double TITLE_EXIT_OFFSET_Y = 180;
    private static final double ENVELOPE_EXIT_OFFSET_Y = 420;
    private static final double BUTTON_EXIT_OFFSET_X = 420;
    private static final double TITLE_EXIT_SAFE_MARGIN = 32;
    private static final double ENVELOPE_EXIT_SAFE_MARGIN = 48;
    private static final double BUTTON_EXIT_SAFE_MARGIN = 48;
    private static final double TITLE_ANTICIPATION_Y = 20;
    private static final double ENVELOPE_ANTICIPATION_Y = 18;
    private static final double BUTTON_ANTICIPATION_X = 18;

    private final Node interactionLayer;
    private final Node titleNode;
    private final Node envelopeNode;
    private final Map<ButtonKey, Node> buttonByKey;
    private boolean playing;

    HomeSelectionAnimationManager(
        Node interactionLayer,
        Node titleNode,
        Node envelopeNode,
        Node playButton,
        Node tutorialButton,
        Node settingsButton,
        Node helpButton) {
        this.interactionLayer = Objects.requireNonNull(interactionLayer, "interactionLayer cannot be null.");
        this.titleNode = Objects.requireNonNull(titleNode, "titleNode cannot be null.");
        this.envelopeNode = Objects.requireNonNull(envelopeNode, "envelopeNode cannot be null.");
        this.buttonByKey = new EnumMap<>(ButtonKey.class);
        buttonByKey.put(ButtonKey.PLAY, Objects.requireNonNull(playButton, "playButton cannot be null."));
        buttonByKey.put(ButtonKey.TUTORIAL, Objects.requireNonNull(tutorialButton, "tutorialButton cannot be null."));
        buttonByKey.put(ButtonKey.SETTINGS, Objects.requireNonNull(settingsButton, "settingsButton cannot be null."));
        buttonByKey.put(ButtonKey.HELP, Objects.requireNonNull(helpButton, "helpButton cannot be null."));
    }

    void play(ButtonKey clickedButton, Runnable onFinished) {
        Objects.requireNonNull(clickedButton, "clickedButton cannot be null.");
        if (playing) {
            return;
        }
        playing = true;
        interactionLayer.setMouseTransparent(true);

        ParallelTransition envelopeAndButtons = new ParallelTransition();
        envelopeAndButtons.getChildren().add(createEnvelopeExitTransition());

        List<ButtonKey> orderedExitButtons = resolveButtonExitOrder(clickedButton);
        for (int buttonIndex = 0; buttonIndex < orderedExitButtons.size(); buttonIndex++) {
            envelopeAndButtons.getChildren().add(createButtonExitTransition(orderedExitButtons.get(buttonIndex), buttonIndex));
        }

        ParallelTransition transition = new ParallelTransition(
            createTitleExitTransition(),
            envelopeAndButtons);
        transition.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        transition.playFromStart();
    }

    private List<ButtonKey> resolveButtonExitOrder(ButtonKey clickedButton) {
        List<ButtonKey> orderedButtons = new ArrayList<>(List.of(
            ButtonKey.PLAY,
            ButtonKey.TUTORIAL,
            ButtonKey.SETTINGS,
            ButtonKey.HELP));
        orderedButtons.remove(clickedButton);
        orderedButtons.add(clickedButton);
        return orderedButtons;
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

    private SequentialTransition createEnvelopeExitTransition() {
        double envelopeExitTargetY = resolveEnvelopeExitTargetY();
        return createVerticalAnticipationExit(
            envelopeNode,
            ENVELOPE_EXIT_DURATION,
            envelopeNode.getTranslateY(),
            envelopeNode.getTranslateY() - ENVELOPE_ANTICIPATION_Y,
            envelopeExitTargetY);
    }

    private SequentialTransition createButtonExitTransition(ButtonKey buttonKey, int buttonIndex) {
        Node button = buttonByKey.get(buttonKey);
        PauseTransition delay = new PauseTransition(BUTTON_STAGGER.multiply(buttonIndex));
        double buttonExitTargetX = resolveButtonExitTargetX(button);
        SequentialTransition exit = createHorizontalAnticipationExit(
            button,
            BUTTON_EXIT_DURATION,
            button.getTranslateX(),
            button.getTranslateX() - BUTTON_ANTICIPATION_X,
            buttonExitTargetX);
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

    private double resolveButtonExitTargetX(Node button) {
        if (interactionLayer.getScene() == null) {
            return button.getTranslateX() + BUTTON_EXIT_OFFSET_X;
        }

        Bounds sceneBounds = button.localToScene(button.getBoundsInLocal());
        if (sceneBounds == null) {
            return button.getTranslateX() + BUTTON_EXIT_OFFSET_X;
        }

        double sceneWidth = interactionLayer.getScene().getWidth();
        double requiredShift = (sceneWidth - sceneBounds.getMinX()) + BUTTON_EXIT_SAFE_MARGIN;
        return button.getTranslateX() + Math.max(requiredShift, BUTTON_EXIT_OFFSET_X);
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

    private double resolveEnvelopeExitTargetY() {
        if (interactionLayer.getScene() == null) {
            return envelopeNode.getTranslateY() + ENVELOPE_EXIT_OFFSET_Y;
        }

        Bounds sceneBounds = envelopeNode.localToScene(envelopeNode.getBoundsInLocal());
        if (sceneBounds == null) {
            return envelopeNode.getTranslateY() + ENVELOPE_EXIT_OFFSET_Y;
        }

        double sceneHeight = interactionLayer.getScene().getHeight();
        double requiredShift = (sceneHeight - sceneBounds.getMinY()) + ENVELOPE_EXIT_SAFE_MARGIN;
        return envelopeNode.getTranslateY() + Math.max(requiredShift, ENVELOPE_EXIT_OFFSET_Y);
    }
}
