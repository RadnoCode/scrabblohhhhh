package com.kotva.presentation.controller;

import com.kotva.infrastructure.AudioManager;
import com.kotva.infrastructure.settings.AppSettings;
import com.kotva.infrastructure.settings.SettingsRepository;
import com.kotva.lan.LanHostAddressResolver;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.SliderButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.SettingsViewModel;

import java.net.InetAddress;

/**
 * Controls the settings screen.
 */
public class SettingsController {
    private static final String NO_IP_ADDRESS_TEXT = "No IP Adress";

    private final SceneNavigator navigator;
    private final AudioManager audioManager;
    private final SettingsRepository settingsRepository;
    private final SettingsViewModel viewModel;

    public SettingsController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.audioManager = navigator.getAppContext().getAudioManager();
        this.settingsRepository = navigator.getAppContext().getSettingsRepository();
        AppSettings settings = settingsRepository.load();
        audioManager.setSFXVolume(settings.getSfxVolume());
        this.viewModel =
            new SettingsViewModel("Player 1", settings.getSfxVolume() * 100.0, resolveLocalIpAddress());
    }

    public SettingsViewModel getViewModel() {
        return viewModel;
    }

    public void bindSoundEffectSlider(SliderButton soundEffectButton) {
        soundEffectButton.setSliderValue(viewModel.getSoundEffectVolume());
        soundEffectButton.setOnValueChanged(this::updateSoundEffectVolume);
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    private void updateSoundEffectVolume(double value) {
        double normalizedVolume = normalizeSliderValue(value);
        viewModel.setSoundEffectVolume(normalizedVolume * 100.0);
        audioManager.setSFXVolume(normalizedVolume);

        AppSettings settings = settingsRepository.load();
        settingsRepository.save(
            new AppSettings(
                settings.getMusicVolume(),
                normalizedVolume,
                settings.isTutorialPromptAcknowledged(),
                settings.isTutorialCompleted()));
    }

    private static double normalizeSliderValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value / 100.0));
    }

    private static String resolveLocalIpAddress() {
        InetAddress address = LanHostAddressResolver.resolvePreferredIpv4Address();
        if (address == null || address.getHostAddress() == null || address.getHostAddress().isBlank()) {
            return NO_IP_ADDRESS_TEXT;
        }
        return address.getHostAddress();
    }
}
