package com.kotva.presentation.component;

public interface HomeEnvelopeAnimation {
    void playForward(Runnable onFinished);

    void playBackward(Runnable onFinished);

    void showForwardEndState();
}
