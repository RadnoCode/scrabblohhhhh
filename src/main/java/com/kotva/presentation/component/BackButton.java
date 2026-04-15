package com.kotva.presentation.component;

public class BackButton extends CommonButton {

    public BackButton() {
        super("Back");
        initializeBackButton();
    }

    private void initializeBackButton() {
        getStyleClass().add("back-button");
        setPrefSize(100, 30);
        setMinSize(100, 30);
        setMaxSize(100, 30);
    }
}