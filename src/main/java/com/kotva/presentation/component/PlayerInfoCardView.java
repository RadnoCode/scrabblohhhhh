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
        setPrefSize(236, 140);
        setMinSize(236, 140);
        setMaxSize(236, 140);
        setPadding(new Insets(14, 18, 14, 18));

        StackPane avatarPane = new StackPane(avatarLabel);
        avatarPane.getStyleClass().add("game-player-avatar");
        avatarPane.setPrefSize(62, 62);
        avatarPane.setMinSize(62, 62);
        avatarPane.setMaxSize(62, 62);

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
        leaderPlaceholder.setPrefSize(22, 14);
        leaderPlaceholder.setMinSize(22, 14);
        leaderPlaceholder.setMaxSize(22, 14);

        HBox scoreRow = new HBox(10, playerScoreLabel, scoreSpacer, leaderPlaceholder);
        scoreRow.setAlignment(Pos.CENTER_LEFT);

        HBox stepMarkRow = new HBox(6, stepMarkTitleLabel, stepMarkValueLabel);
        stepMarkRow.setAlignment(Pos.CENTER_LEFT);

        VBox textColumn = new VBox(6, playerNameLabel, playerIdLabel, scoreRow, stepMarkRow);
        textColumn.setAlignment(Pos.CENTER_LEFT);

        HBox content = new HBox(16, avatarPane, textColumn);
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
