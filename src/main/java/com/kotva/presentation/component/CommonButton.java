package com.kotva.presentation.component;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

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

        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> requestFocus());
    }
}