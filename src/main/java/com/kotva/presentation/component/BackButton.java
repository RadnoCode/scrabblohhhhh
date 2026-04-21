package com.kotva.presentation.component;

public class BackButton extends CommonButton {

    public BackButton() {
        super("Back");
        initializeBackButton();
    }

    private void initializeBackButton() {
        setTemplateEnabled(false);
        getStyleClass().add("back-button");
        applyFixedSize(100, 30);
    }
}
