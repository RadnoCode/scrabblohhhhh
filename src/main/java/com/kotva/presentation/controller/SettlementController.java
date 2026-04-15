package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import java.util.Objects;

public class SettlementController {
    private final SceneNavigator navigator;

    public SettlementController(SceneNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator cannot be null.");
    }

    public void bindActions(CommonButton goBackButton) {
        Objects.requireNonNull(goBackButton, "goBackButton cannot be null.");
        goBackButton.setOnAction(event -> navigator.showGameSetting());
    }
}