package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SliderButton;
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

    public void bindControls(InputButton nameButton, SliderButton musicButton) {
        nameButton.setInputText(viewModel.getPlayerName());
        musicButton.setSliderValue(viewModel.getMusicVolume());

        nameButton.getTextField()
            .textProperty()
            .addListener((observable, oldValue, newValue) -> viewModel.setPlayerName(newValue));
        musicButton.setOnValueChanged(viewModel::setMusicVolume);
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }
}