package com.kotva.presentation.component;

/**
 * Defines the animation methods for home envelopes.
 */
public interface HomeEnvelopeAnimation {
    /**
     * Plays the open animation.
     *
     * @param onFinished code to run after the animation ends
     */
    void playForward(Runnable onFinished);

    /**
     * Plays the close animation.
     *
     * @param onFinished code to run after the animation ends
     */
    void playBackward(Runnable onFinished);

    /**
     * Shows the first frame.
     */
    void showForwardStartState();

    /**
     * Shows the last frame.
     */
    void showForwardEndState();
}
