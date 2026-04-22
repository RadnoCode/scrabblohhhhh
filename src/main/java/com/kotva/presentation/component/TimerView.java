package com.kotva.presentation.component;

import javafx.geometry.Insets;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class TimerView extends StackPane {
    public enum Variant {
        STEP("game-timer-step"),
        TOTAL("game-timer-total");

        private final String styleClassName;

        Variant(String styleClassName) {
            this.styleClassName = styleClassName;
        }

        private String getStyleClassName() {
            return styleClassName;
        }
    }

    private static final double TIMER_WIDTH = 144;
    private static final double TIMER_HEIGHT = 96;

    private String titleText;
    private final Label valueLabel;
    private final Variant variant;

    public TimerView(String titleText) {
        this(titleText, Variant.STEP);
    }

    public TimerView(String titleText, Variant variant) {
        this.valueLabel = new Label();
        this.variant = Objects.requireNonNull(variant, "variant cannot be null.");
        initializeTimer();
        setTitle(titleText);
        setTimeText("--:--");
    }

    private void initializeTimer() {
        getStyleClass().addAll("game-timer", variant.getStyleClassName());
        setPrefSize(TIMER_WIDTH, TIMER_HEIGHT);
        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setAlignment(Pos.BOTTOM_CENTER);

        valueLabel.getStyleClass().add("game-timer-value");
        StackPane.setMargin(valueLabel, new Insets(0, 0, 18, 0));
        getChildren().add(valueLabel);
    }

    public void setTitle(String titleText) {
        this.titleText = Objects.requireNonNull(titleText, "titleText cannot be null.");
        updateAccessibleText();
    }

    public void setTimeText(String timeText) {
        valueLabel.setText(Objects.requireNonNull(timeText, "timeText cannot be null."));
        updateAccessibleText();
    }

    private void updateAccessibleText() {
        if (titleText != null) {
            setAccessibleText(titleText + ": " + valueLabel.getText());
        }
    }
}
