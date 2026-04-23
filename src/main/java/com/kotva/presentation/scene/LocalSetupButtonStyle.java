package com.kotva.presentation.scene;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import javafx.geometry.Pos;

final class LocalSetupButtonStyle {
    static final String DARK_BACKGROUND = "local-themed-setup-button-dark";
    static final String MEDIUM_BACKGROUND = "local-themed-setup-button-medium";
    static final String LIGHT_BACKGROUND = "local-themed-setup-button-light";
    static final String PLAYER_BACKGROUND = "local-themed-setup-button-player";

    private static final double DEFAULT_SETUP_BUTTON_WIDTH = 420.0;
    private static final double DEFAULT_SETUP_BUTTON_HEIGHT = 68.0;
    private static final double SETUP_INPUT_WIDTH = 144;
    private static final double SETUP_INPUT_HEIGHT = 32;
    private static final double SWITCH_TRIGGER_WIDTH = 176;
    private static final double SWITCH_TRIGGER_HEIGHT = 32;
    private static final double CONTINUE_BUTTON_WIDTH = DEFAULT_SETUP_BUTTON_HEIGHT * 591.0 / 238.0;

    private LocalSetupButtonStyle() {
    }

    static void configureInputButton(InputButton button, String backgroundStyleClass) {
        configureBaseButton(button, backgroundStyleClass);
        button.setInputFieldTone(InputButton.InputFieldTone.DARK_SURFACE);
        button.setInputFieldSize(SETUP_INPUT_WIDTH, SETUP_INPUT_HEIGHT);
    }

    static void configureSwitchButton(SwitchButton button, String backgroundStyleClass) {
        configureBaseButton(button, backgroundStyleClass);
        button.setSwitchTriggerTone(SwitchButton.SwitchTriggerTone.DARK_SURFACE);
        button.setSwitchTriggerSize(SWITCH_TRIGGER_WIDTH, SWITCH_TRIGGER_HEIGHT);
    }

    static void configureContinueButton(CommonButton button) {
        configureBaseButton(button, "local-themed-continue-button");
        button.applyButtonSize(CONTINUE_BUTTON_WIDTH, DEFAULT_SETUP_BUTTON_HEIGHT);
        button.setButtonContentAlignment(Pos.CENTER);
    }

    private static void configureBaseButton(CommonButton button, String backgroundStyleClass) {
        button.setTemplateBackgroundEnabled(false);
        button.getStyleClass().addAll("local-themed-setup-button", backgroundStyleClass);
        button.applyButtonSize(DEFAULT_SETUP_BUTTON_WIDTH, DEFAULT_SETUP_BUTTON_HEIGHT);
    }
}
