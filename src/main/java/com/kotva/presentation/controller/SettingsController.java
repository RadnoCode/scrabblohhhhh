package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.SettingsViewModel;

public class SettingsController {
    private final SceneNavigator navigator;
    private final SettingsViewModel viewModel;

    public SettingsController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new SettingsViewModel("Player 1", 60.0, "SCR-2026-0415");
    }

    public SettingsViewModel getViewModel() {
        return viewModel;
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }
}
