package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class PlayerInfoCardView extends StackPane {
    public static final double CARD_WIDTH = 288;
    public static final double CARD_HEIGHT = 96;
    private static final double AVATAR_WIDTH = 82;
    private static final double AVATAR_HEIGHT = 60;
    private static final double AVATAR_X = 10;
    private static final double AVATAR_Y = 10;
    private static final double NAME_X = 89;
    private static final double NAME_Y = 29;
    private static final double NAME_WIDTH = 108;
    private static final double SCORE_VALUE_X = 141;
    private static final double SCORE_VALUE_Y = 53;
    private static final double STEP_MARK_X = 90;
    private static final double STEP_MARK_Y = 70;

    private final Label avatarLabel;
    private final Label playerNameLabel;
    private final Label playerIdLabel;
    private final Label playerScoreLabel;
    private final Label stepMarkTitleLabel;
    private final Label stepMarkValueLabel;

    public PlayerInfoCardView() {
        this.avatarLabel = new Label();
        this.playerNameLabel = new Label();
        this.playerIdLabel = new Label();
        this.playerScoreLabel = new Label();
        this.stepMarkTitleLabel = new Label("Step Mark:");
        this.stepMarkValueLabel = new Label();
        initializeCard();
        clear();
    }

    private void initializeCard() {
        getStyleClass().add("game-player-card");
        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMinSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        setPadding(new Insets(0));

        StackPane avatarPane = new StackPane(avatarLabel);
        avatarPane.getStyleClass().add("game-player-avatar");
        avatarPane.setPrefSize(AVATAR_WIDTH, AVATAR_HEIGHT);
        avatarPane.setMinSize(AVATAR_WIDTH, AVATAR_HEIGHT);
        avatarPane.setMaxSize(AVATAR_WIDTH, AVATAR_HEIGHT);
        avatarPane.relocate(AVATAR_X, AVATAR_Y);

        avatarLabel.getStyleClass().add("game-player-avatar-text");

        playerNameLabel.getStyleClass().add("game-player-name");
        playerIdLabel.getStyleClass().add("game-player-meta");
        playerScoreLabel.getStyleClass().add("game-player-score");
        stepMarkTitleLabel.getStyleClass().add("game-player-step-mark-title");
        stepMarkValueLabel.getStyleClass().add("game-player-step-mark-value");
        playerNameLabel.setPrefWidth(NAME_WIDTH);
        playerNameLabel.setMinWidth(NAME_WIDTH);
        playerNameLabel.setMaxWidth(NAME_WIDTH);
        playerNameLabel.relocate(NAME_X, NAME_Y);
        playerIdLabel.setManaged(false);
        playerIdLabel.setVisible(false);

        playerScoreLabel.relocate(SCORE_VALUE_X, SCORE_VALUE_Y);
        stepMarkTitleLabel.relocate(STEP_MARK_X, STEP_MARK_Y);
        stepMarkValueLabel.relocate(STEP_MARK_X + 53, STEP_MARK_Y);

        Pane content = new Pane(
            avatarPane,
            playerNameLabel,
            playerScoreLabel,
            stepMarkTitleLabel,
            stepMarkValueLabel);
        content.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        getChildren().add(content);
    }

    public void setPlayer(
        String playerName,
        String playerId,
        int score,
        String stepMarkText,
        boolean currentTurn,
        boolean active) {
        playerNameLabel.setText(playerName);
        playerIdLabel.setText(playerId);
        playerScoreLabel.setText(String.valueOf(score));
        stepMarkValueLabel.setText(stepMarkText);
        avatarLabel.setText(buildInitials(playerName));
        updateStateClasses(currentTurn, active);
    }

    public void clear() {
        playerNameLabel.setText("");
        playerIdLabel.setText("");
        playerScoreLabel.setText("");
        stepMarkValueLabel.setText("--");
        avatarLabel.setText("");
        getStyleClass().removeAll("game-player-card-current", "game-player-card-inactive");
        if (!getStyleClass().contains("game-player-card-empty")) {
            getStyleClass().add("game-player-card-empty");
        }
    }

    private void updateStateClasses(boolean currentTurn, boolean active) {
        getStyleClass().removeAll("game-player-card-current", "game-player-card-inactive", "game-player-card-empty");
        if (currentTurn) {
            getStyleClass().add("game-player-card-current");
        }
        if (!active) {
            getStyleClass().add("game-player-card-inactive");
        }
    }

    private String buildInitials(String playerName) {
        String[] parts = playerName.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "--";
        }

        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}
