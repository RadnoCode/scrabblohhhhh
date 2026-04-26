package com.kotva.presentation.controller;

import com.kotva.infrastructure.AudioManager;
import com.kotva.infrastructure.settings.AppSettings;
import com.kotva.infrastructure.settings.SettingsRepository;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.HelpEnvelope;
import com.kotva.presentation.component.PlayEnvelope;
import com.kotva.presentation.component.SettingEnvelope;
import com.kotva.presentation.component.TutorialEnvelope;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.HomeViewModel;

/**
 * Controls the home screen.
 */
public class HomeController {
    private final HomeViewModel viewModel;
    private final SceneNavigator navigator;
    private final AudioManager audioManager;
    private final SettingsRepository settingsRepository;
    private boolean tutorialPromptVisible;

    public HomeController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.audioManager = navigator.getAppContext().getAudioManager();
        this.settingsRepository = navigator.getAppContext().getSettingsRepository();
        this.viewModel = new HomeViewModel("SCRABBLE", "Play", "Tutorial", "Settings", "Help");
        this.tutorialPromptVisible = !settingsRepository.load().isTutorialPromptAcknowledged();
    }

    public HomeViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(
        CommonButton playButton,
        CommonButton tutorialButton,
        CommonButton settingsButton,
        CommonButton helpButton,
        PlayEnvelope playEnvelope,
        TutorialEnvelope tutorialEnvelope,
        SettingEnvelope settingEnvelope,
        HelpEnvelope helpEnvelope)
    {
        HomeButtonSequenceManager sequenceManager = new HomeButtonSequenceManager(
            HomeButtonSequenceManager.ButtonKey.PLAY,
            playEnvelope,
            tutorialEnvelope,
            settingEnvelope,
            helpEnvelope);

        playButton.setOnMouseEntered(event -> sequenceManager.onButtonEntered(HomeButtonSequenceManager.ButtonKey.PLAY));
        tutorialButton.setOnMouseEntered(event -> sequenceManager.onButtonEntered(HomeButtonSequenceManager.ButtonKey.TUTORIAL));
        settingsButton.setOnMouseEntered(event -> sequenceManager.onButtonEntered(HomeButtonSequenceManager.ButtonKey.SETTINGS));
        helpButton.setOnMouseEntered(event -> sequenceManager.onButtonEntered(HomeButtonSequenceManager.ButtonKey.HELP));

        playButton.setOnMouseExited(event -> sequenceManager.onButtonExited(HomeButtonSequenceManager.ButtonKey.PLAY));
        tutorialButton.setOnMouseExited(event -> sequenceManager.onButtonExited(HomeButtonSequenceManager.ButtonKey.TUTORIAL));
        settingsButton.setOnMouseExited(event -> sequenceManager.onButtonExited(HomeButtonSequenceManager.ButtonKey.SETTINGS));
        helpButton.setOnMouseExited(event -> sequenceManager.onButtonExited(HomeButtonSequenceManager.ButtonKey.HELP));
    }

    public void navigateToPlay() {
        navigator.showGameSetting();
    }

    public void navigateToSettings() {
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showSettings();
    }

    public void navigateToTutorial() {
        acknowledgeTutorialPrompt();
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showTutorial();
    }

    public void navigateToHelp() {
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showHelp();
    }

    public boolean isTutorialPromptVisible() {
        return tutorialPromptVisible;
    }

    public void startTutorialFromPrompt() {
        acknowledgeTutorialPrompt();
        navigator.showTutorial();
    }

    public void dismissTutorialPrompt() {
        acknowledgeTutorialPrompt();
        tutorialPromptVisible = false;
    }

    private void acknowledgeTutorialPrompt() {
        AppSettings settings = settingsRepository.load();
        settingsRepository.save(
            new AppSettings(
                settings.getMusicVolume(),
                settings.getSfxVolume(),
                true,
                settings.isTutorialCompleted()));
        tutorialPromptVisible = false;
    }
}
