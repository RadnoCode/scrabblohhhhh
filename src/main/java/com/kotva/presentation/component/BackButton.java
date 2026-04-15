package com.kotva.presentation.component;

/**
 * BackButton is a smaller reusable button used for page return actions.
 * It keeps the CommonButton interaction behavior, but uses a smaller size.
 */
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
