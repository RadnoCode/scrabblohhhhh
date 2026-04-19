package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * TileView 是字母块的基础 JavaFX 组件。
 * 它既可以显示真实字母，也可以显示空槽位。
 */
public class TileView extends StackPane {
    private final double tileSize;
    private final Label letterLabel;
    private final Label scoreLabel;

    public TileView() {
        this(56);
    }

    public TileView(double tileSize) {
        this.tileSize = tileSize;
        this.letterLabel = new Label();
        this.scoreLabel = new Label();
        initializeTile();
    }

    private void initializeTile() {
        // 给 Tile 自身挂基础样式。
        getStyleClass().add("game-tile");
        // 棋盘上的小 Tile 需要单独挂一个小尺寸样式。
        if (tileSize <= 36) {
            getStyleClass().add("game-tile-small");
        }
        // 固定 Tile 尺寸，避免布局阶段被压扁。
        setPrefSize(tileSize, tileSize);
        setMinSize(tileSize, tileSize);
        setMaxSize(tileSize, tileSize);

        // 分别给字母和角标分数挂样式。
        letterLabel.getStyleClass().add("game-tile-letter");
        scoreLabel.getStyleClass().add("game-tile-score");
        if (tileSize <= 36) {
            letterLabel.getStyleClass().add("game-tile-letter-small");
            scoreLabel.getStyleClass().add("game-tile-score-small");
        }

        // 字母居中，分数贴右下角。
        StackPane.setAlignment(letterLabel, Pos.CENTER);
        StackPane.setAlignment(scoreLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(scoreLabel, new Insets(0, 7, 5, 0));

        // 把两个文字节点加入 Tile 容器，并初始化为空状态。
        getChildren().addAll(letterLabel, scoreLabel);
        clearTile();
    }

    public void setTile(GameViewModel.TileModel tileModel) {
        // 允许外部直接传 TileModel，便于 renderer 复用。
        if (tileModel == null || tileModel.isEmpty()) {
            clearTile();
            return;
        }
        setTile(tileModel.getLetter(), tileModel.getScore());
    }

    public void setTile(String letter, int score) {
        // 写入字母和分数文本。
        letterLabel.setText(letter);
        scoreLabel.setText(Integer.toString(score));
        // 从空槽样式切换到已填充样式。
        getStyleClass().remove("game-tile-empty");
        if (!getStyleClass().contains("game-tile-filled")) {
            getStyleClass().add("game-tile-filled");
        }
    }

    public void clearTile() {
        // 清空文本。
        letterLabel.setText("");
        scoreLabel.setText("");
        // 从已填充样式切回空槽样式。
        getStyleClass().remove("game-tile-filled");
        if (!getStyleClass().contains("game-tile-empty")) {
            getStyleClass().add("game-tile-empty");
        }
    }

    public double getTileSize() {
        return tileSize;
    }
}
