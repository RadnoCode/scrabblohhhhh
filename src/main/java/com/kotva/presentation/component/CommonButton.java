package com.kotva.presentation.component;

import com.kotva.infrastructure.AudioManager;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class CommonButton extends Button {
    private static AudioManager audioManager;

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
        addEventFilter(ActionEvent.ACTION, event -> playClickSound());
    }

    public static void setAudioManager(AudioManager manager) {
        audioManager = manager;
    }

    protected void playClickSound() {
        if (audioManager != null) {
            audioManager.playButtonClick();
        }
    }
}
