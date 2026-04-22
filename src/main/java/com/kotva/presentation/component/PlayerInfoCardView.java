package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PlayerInfoCardView extends StackPane {
    private static final double CARD_WIDTH = 288;
    private static final double CARD_HEIGHT = 96;
    private static final double AVATAR_SIZE = 56;

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
        setPadding(new Insets(10, 16, 10, 14));

        StackPane avatarPane = new StackPane(avatarLabel);
        avatarPane.getStyleClass().add("game-player-avatar");
        avatarPane.setPrefSize(AVATAR_SIZE, AVATAR_SIZE);
        avatarPane.setMinSize(AVATAR_SIZE, AVATAR_SIZE);
        avatarPane.setMaxSize(AVATAR_SIZE, AVATAR_SIZE);

        avatarLabel.getStyleClass().add("game-player-avatar-text");

        playerNameLabel.getStyleClass().add("game-player-name");
        playerIdLabel.getStyleClass().add("game-player-meta");
        playerScoreLabel.getStyleClass().add("game-player-score");
        stepMarkTitleLabel.getStyleClass().add("game-player-step-mark-title");
        stepMarkValueLabel.getStyleClass().add("game-player-step-mark-value");

        Region scoreSpacer = new Region();
        HBox.setHgrow(scoreSpacer, Priority.ALWAYS);

        Region leaderPlaceholder = new Region();
        leaderPlaceholder.getStyleClass().add("game-player-lead-placeholder");
        leaderPlaceholder.setPrefSize(18, 12);
        leaderPlaceholder.setMinSize(18, 12);
        leaderPlaceholder.setMaxSize(18, 12);

        HBox scoreRow = new HBox(4, playerScoreLabel, scoreSpacer, leaderPlaceholder);
        scoreRow.setAlignment(Pos.CENTER_LEFT);

        HBox stepMarkRow = new HBox(2, stepMarkTitleLabel, stepMarkValueLabel);
        stepMarkRow.setAlignment(Pos.CENTER_LEFT);

        VBox textColumn = new VBox(1, playerNameLabel, playerIdLabel, scoreRow, stepMarkRow);
        textColumn.setAlignment(Pos.CENTER_LEFT);

        HBox content = new HBox(18, avatarPane, textColumn);
        content.setAlignment(Pos.CENTER_LEFT);
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
        playerScoreLabel.setText("Score  " + score);
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
