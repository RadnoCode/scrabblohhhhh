package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * PlayerInfoCardView 是玩家信息卡组件。
 */
public class PlayerInfoCardView extends StackPane {
    private final Label avatarLabel;
    private final Label playerNameLabel;
    private final Label playerIdLabel;
    private final Label playerScoreLabel;

    public PlayerInfoCardView() {
        this.avatarLabel = new Label();
        this.playerNameLabel = new Label();
        this.playerIdLabel = new Label();
        this.playerScoreLabel = new Label();
        initializeCard();
        clear();
    }

    private void initializeCard() {
        // 先设置卡片本体的圆角面板尺寸。
        getStyleClass().add("game-player-card");
        setPrefSize(236, 124);
        setMinSize(236, 124);
        setMaxSize(236, 124);
        setPadding(new Insets(16, 18, 16, 18));

        // 左侧头像区域本质上也是一个小的 StackPane。
        StackPane avatarPane = new StackPane(avatarLabel);
        avatarPane.getStyleClass().add("game-player-avatar");
        avatarPane.setPrefSize(62, 62);
        avatarPane.setMinSize(62, 62);
        avatarPane.setMaxSize(62, 62);

        avatarLabel.getStyleClass().add("game-player-avatar-text");

        playerNameLabel.getStyleClass().add("game-player-name");
        playerIdLabel.getStyleClass().add("game-player-meta");
        playerScoreLabel.getStyleClass().add("game-player-score");

        // scoreSpacer 用来把右侧“领先占位”顶到最右边。
        Region scoreSpacer = new Region();
        HBox.setHgrow(scoreSpacer, Priority.ALWAYS);

        // 这个小占位块后续可以替换成真正的领先标识。
        Region leaderPlaceholder = new Region();
        leaderPlaceholder.getStyleClass().add("game-player-lead-placeholder");
        leaderPlaceholder.setPrefSize(22, 14);
        leaderPlaceholder.setMinSize(22, 14);
        leaderPlaceholder.setMaxSize(22, 14);

        HBox scoreRow = new HBox(10, playerScoreLabel, scoreSpacer, leaderPlaceholder);
        scoreRow.setAlignment(Pos.CENTER_LEFT);

        VBox textColumn = new VBox(8, playerNameLabel, playerIdLabel, scoreRow);
        textColumn.setAlignment(Pos.CENTER_LEFT);

        // 最终整张卡片由“头像 + 文本列”组成。
        HBox content = new HBox(16, avatarPane, textColumn);
        content.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(content);
    }

    public void setPlayer(String playerName, String playerId, int score, boolean currentTurn, boolean active) {
        // 写入玩家基础信息。
        playerNameLabel.setText(playerName);
        playerIdLabel.setText(playerId);
        playerScoreLabel.setText("Score  " + score);
        avatarLabel.setText(buildInitials(playerName));
        // 再根据当前轮次和是否存活切换卡片状态样式。
        updateStateClasses(currentTurn, active);
    }

    public void clear() {
        // 清空文案，让这个卡位回到“空占位”视觉。
        playerNameLabel.setText("");
        playerIdLabel.setText("");
        playerScoreLabel.setText("");
        avatarLabel.setText("");
        getStyleClass().removeAll("game-player-card-current", "game-player-card-inactive");
        if (!getStyleClass().contains("game-player-card-empty")) {
            getStyleClass().add("game-player-card-empty");
        }
    }

    private void updateStateClasses(boolean currentTurn, boolean active) {
        // 每次先把几种互斥状态类全部移除。
        getStyleClass().removeAll("game-player-card-current", "game-player-card-inactive", "game-player-card-empty");
        if (currentTurn) {
            getStyleClass().add("game-player-card-current");
        }
        if (!active) {
            getStyleClass().add("game-player-card-inactive");
        }
    }

    private String buildInitials(String playerName) {
        // 用名字首字母生成头像缩写。
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
