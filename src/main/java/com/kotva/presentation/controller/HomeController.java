package com.kotva.presentation.controller;

import com.kotva.presentation.component.EnvelopeIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.HomeViewModel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class HomeController {
    private final HomeViewModel viewModel;
    private final SceneNavigator navigator;

    public HomeController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new HomeViewModel("SCRABBLE", "Play", "Settings", "Help");
    }

    public HomeViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(
        CommonButton playButton,
        CommonButton settingsButton,
        CommonButton helpButton,
        EnvelopeIconView envelopeIconView)
    {
        playButton.setOnAction(createPlayHandler());
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

    public EventHandler<ActionEvent> createHelpHandler() {
        return event -> handleHelpClick();
    }

    private void handlePlayClick() {
        navigator.showGameSetting();
    }

    private void handleSettingsClick() {
        navigator.showSettings();
    }

    private void handleHelpClick() {
        navigator.showHelp();
    }

    private void handleEnvelopeClick() {
        System.out.println("Home page: Envelope icon clicked.");
    }
}