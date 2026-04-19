package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SliderButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.SettingsViewModel;

/**
 * SettingsController manages current settings page values.
 * It connects the special components with the SettingsViewModel.
 */
public class SettingsController {
    private final SceneNavigator navigator;
    private final SettingsViewModel viewModel;

    public SettingsController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new SettingsViewModel(
                "Player 1",
                new String[]{"English", "Chinese", "Japanese"},
                0,
                60.0,
                "SCR-2026-0415");
    }

    public SettingsViewModel getViewModel() {
        return viewModel;
    }

    public void bindControls(InputButton nameButton, SwitchButton languageButton, SliderButton musicButton) {
        nameButton.setInputText(viewModel.getPlayerName());
        languageButton.setCurrentValue(viewModel.getCurrentLanguage());
        musicButton.setSliderValue(viewModel.getMusicVolume());

        nameButton.getTextField().textProperty().addListener((observable, oldValue, newValue) -> viewModel.setPlayerName(newValue));
        languageButton.setOnSwitchAction(() -> viewModel.rotateLanguage());
        musicButton.setOnValueChanged(value -> viewModel.setMusicVolume(value));
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }
}
