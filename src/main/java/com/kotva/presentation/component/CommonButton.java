package com.kotva.presentation.component;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

/**
 * CommonButton is the shared normal button component used across the project.
 * It keeps the base button behavior simple and exposes the normal Button event API,
 * so scene logic can directly call setOnAction on it.
 */
public class CommonButton extends Button {
    public CommonButton() {
        initializeButton();
    }

    public CommonButton(String text) {
        super(text);
        initializeButton();
    }

    protected void initializeButton() {
        getStyleClass().add("common-button");

        setFocusTraversable(true);
        setPrefSize(420, 70);
        setMinSize(420, 70);
        setMaxWidth(420);

        /*
         * Request focus when the user presses the button.
         * This allows CSS :focused to draw the white outline requested in the design.
         */
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> requestFocus());
    }
}
