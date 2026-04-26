package com.kotva.presentation.component;

/**
 * A common back button.
 */
public class BackButton extends CommonButton {
    private static final String BACK_BUTTON_IMAGE_PATH = "/images/buttons/back.png";
    private static final double BACK_BUTTON_WIDTH = 100;
    private static final double BACK_BUTTON_HEIGHT = BACK_BUTTON_WIDTH * 550.0 / 930.0;

    public BackButton() {
        super();
        initializeBackButton();
    }

    private void initializeBackButton() {
        setTemplateEnabled(false);
        setText(null);
        setCustomBackgroundImage(BACK_BUTTON_IMAGE_PATH);
        getStyleClass().add("back-button");
        applyButtonSize(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT);
    }
}
