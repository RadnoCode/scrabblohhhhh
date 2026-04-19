package com.kotva.presentation.component;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * TimerView 是计时器显示框组件。
 */
public class TimerView extends StackPane {
    private final Label titleLabel;
    private final Label valueLabel;

    public TimerView(String titleText) {
        this.titleLabel = new Label();
        this.valueLabel = new Label();
        initializeTimer();
        setTitle(titleText);
        setTimeText("--:--");
    }

    private void initializeTimer() {
        // 设置计时器容器自身的尺寸与背景。
        getStyleClass().add("game-timer");
        setPrefSize(120, 156);
        setMinSize(120, 156);
        setMaxSize(120, 156);

        // 标题和数值使用不同样式，便于突出剩余时间。
        titleLabel.getStyleClass().add("game-timer-title");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        valueLabel.getStyleClass().add("game-timer-value");

        // 用 VBox 让标题在上、时间在下。
        VBox content = new VBox(14, titleLabel, valueLabel);
        content.setAlignment(Pos.CENTER);
        getChildren().add(content);
    }

    public void setTitle(String titleText) {
        titleLabel.setText(Objects.requireNonNull(titleText, "titleText cannot be null."));
    }

    public void setTimeText(String timeText) {
        valueLabel.setText(Objects.requireNonNull(timeText, "timeText cannot be null."));
    }
}
