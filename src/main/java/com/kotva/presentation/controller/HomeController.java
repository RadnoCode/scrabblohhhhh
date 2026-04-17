package com.kotva.presentation.controller;

import com.kotva.infrastructure.AudioManager;
import com.kotva.infrastructure.settings.AppSettings;
import com.kotva.infrastructure.settings.SettingsRepository;
import com.kotva.presentation.component.EnvelopeIconView;
import com.kotva.presentation.component.CommonButton;
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
        EnvelopeIconView envelopeIconView)
    {
        playButton.setOnAction(createPlayHandler());
        tutorialButton.setOnAction(createTutorialHandler());
        settingsButton.setOnAction(createSettingsHandler());
        helpButton.setOnAction(createHelpHandler());
        envelopeIconView.setOnMouseClicked(event -> handleEnvelopeClick());
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

    private void handleEnvelopeClick() {
        audioManager.playUIClick();
        System.out.println("Home page: Envelope icon clicked.");
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
