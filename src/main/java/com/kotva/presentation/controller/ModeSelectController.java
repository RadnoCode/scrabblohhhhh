package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.SetupViewModel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * ModeSelectController handles the GameSetting page interaction.
 * The three buttons are prepared for later detailed game mode pages.
 */
public class ModeSelectController {
    private final SceneNavigator navigator;
    private final SetupViewModel viewModel;

    public ModeSelectController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new SetupViewModel("SCRABBLE", "With Friends", "With Robot", "By LAN");
    }

    public SetupViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(CommonButton withFriendsButton, CommonButton withRobotButton, CommonButton byLanButton) {
        withFriendsButton.setOnAction(createWithFriendsHandler());
        withRobotButton.setOnAction(createWithRobotHandler());
        byLanButton.setOnAction(createByLanHandler());
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    public EventHandler<ActionEvent> createWithFriendsHandler() {
        return event -> handleWithFriendsClick();
    }

    public EventHandler<ActionEvent> createWithRobotHandler() {
        return event -> handleWithRobotClick();
    }

    public EventHandler<ActionEvent> createByLanHandler() {
        return event -> handleByLanClick();
    }

    private void handleWithFriendsClick() {
        navigator.showLocalMultiplayerSetup();
    }

    private void handleWithRobotClick() {
        navigator.showLocalAiSetup();
    }

    private void handleByLanClick() {
        navigator.showOnlineSetup();
    }
}
