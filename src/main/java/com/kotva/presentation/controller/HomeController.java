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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

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
        playButton.setOnAction(createPlayHandler());
        tutorialButton.setOnAction(createTutorialHandler());
        settingsButton.setOnAction(createSettingsHandler());
        helpButton.setOnAction(createHelpHandler());

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

    public EventHandler<ActionEvent> createPlayHandler() {
        return event -> handlePlayClick();
    }

    public EventHandler<ActionEvent> createSettingsHandler() {
        return event -> handleSettingsClick();
    }

    public EventHandler<ActionEvent> createTutorialHandler() {
        return event -> handleTutorialClick();
    }

    public EventHandler<ActionEvent> createHelpHandler() {
        return event -> handleHelpClick();
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

    private void handlePlayClick() {
        navigator.showGameSetting();
    }

    private void handleTutorialClick() {
        acknowledgeTutorialPrompt();
        navigator.showTutorial();
    }

    private void handleSettingsClick() {
        navigator.showSettings();
    }

    private void handleHelpClick() {
        navigator.showHelp();
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
