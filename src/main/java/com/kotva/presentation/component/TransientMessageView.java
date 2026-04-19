package com.kotva.presentation.component;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class TransientMessageView extends StackPane {
    private static final Duration DEFAULT_VISIBLE_DURATION = Duration.seconds(5);

    private final Label messageLabel;
    private final PauseTransition hideTransition;

    public TransientMessageView() {
        this.messageLabel = new Label();
        this.hideTransition = new PauseTransition(DEFAULT_VISIBLE_DURATION);
        initialize();
        clear();
    }

    private void initialize() {
        getStyleClass().add("transient-message");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(12, 18, 12, 18));
        setManaged(true);
        setMouseTransparent(true);

        messageLabel.getStyleClass().add("transient-message-label");
        messageLabel.setWrapText(true);
        getChildren().add(messageLabel);

        hideTransition.setOnFinished(event -> clear());
    }

    public void showMessage(String message) {
        messageLabel.setText(message);
        setVisible(true);
        setOpacity(1.0);
        hideTransition.stop();
        hideTransition.playFromStart();
    }

    public void clear() {
        hideTransition.stop();
        messageLabel.setText("");
        setVisible(true);
        setOpacity(0.0);
    }
}