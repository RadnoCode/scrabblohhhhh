package com.kotva.presentation.controller;

import com.kotva.presentation.component.HomeEnvelopeAnimation;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Controls the home button show animation.
 */
final class HomeButtonSequenceManager {
    enum ButtonKey {
        PLAY,
        TUTORIAL,
        SETTINGS,
        HELP
    }

    private final Map<ButtonKey, HomeEnvelopeAnimation> envelopeByButton = new EnumMap<>(ButtonKey.class);
    private ButtonKey previousButton;
    private ButtonKey currentButton;
    private ButtonKey pendingButton;
    private long transitionToken;

    HomeButtonSequenceManager(
        ButtonKey initialButton,
        HomeEnvelopeAnimation playEnvelope,
        HomeEnvelopeAnimation tutorialEnvelope,
        HomeEnvelopeAnimation settingEnvelope,
        HomeEnvelopeAnimation helpEnvelope) {
        envelopeByButton.put(ButtonKey.PLAY, Objects.requireNonNull(playEnvelope));
        envelopeByButton.put(ButtonKey.TUTORIAL, Objects.requireNonNull(tutorialEnvelope));
        envelopeByButton.put(ButtonKey.SETTINGS, Objects.requireNonNull(settingEnvelope));
        envelopeByButton.put(ButtonKey.HELP, Objects.requireNonNull(helpEnvelope));
        this.currentButton = initialButton;
    }

    void onButtonEntered(ButtonKey buttonKey) {
        Objects.requireNonNull(buttonKey, "buttonKey cannot be null.");

        transitionToken++;
        long token = transitionToken;

        if (currentButton == null) {
            currentButton = buttonKey;
            pendingButton = null;
            playForward(buttonKey, token);
            return;
        }

        if (buttonKey == currentButton) {
            pendingButton = null;
            envelopeByButton.get(buttonKey).showForwardEndState();
            playForward(buttonKey, token);
            return;
        }

        pendingButton = buttonKey;
        HomeEnvelopeAnimation outgoingEnvelope = envelopeByButton.get(currentButton);
        outgoingEnvelope.showForwardEndState();
        outgoingEnvelope.playBackward(() -> {
            if (transitionToken != token || pendingButton == null) {
                return;
            }

            ButtonKey outgoingButton = currentButton;
            previousButton = outgoingButton;
            currentButton = pendingButton;
            pendingButton = null;
            playForward(currentButton, token);
        });
    }

    void onButtonExited(ButtonKey buttonKey) {
        if (buttonKey == null || buttonKey != currentButton || pendingButton != null) {
            return;
        }
        envelopeByButton.get(buttonKey).showForwardEndState();
    }

    ButtonKey getPreviousButton() {
        return previousButton;
    }

    ButtonKey getCurrentButton() {
        return currentButton;
    }

    private void playForward(ButtonKey buttonKey, long token) {
        HomeEnvelopeAnimation envelopeAnimation = envelopeByButton.get(buttonKey);
        envelopeAnimation.playForward(() -> {
            if (transitionToken == token && pendingButton == null && currentButton == buttonKey) {
                envelopeAnimation.showForwardEndState();
            }
        });
    }
}
