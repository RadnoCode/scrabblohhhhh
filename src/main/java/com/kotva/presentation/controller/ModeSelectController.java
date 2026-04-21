package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.SetupViewModel;

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
        withFriendsButton.setOnAction(event -> navigateToWithFriends());
        withRobotButton.setOnAction(event -> navigateToWithRobot());
        byLanButton.setOnAction(event -> navigateToByLan());
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    public void navigateToWithFriends() {
        navigator.showLocalMultiplayerSetup();
    }

    public void navigateToWithRobot() {
        navigator.showLocalAiSetup();
    }

    public void navigateToByLan() {
        navigator.showOnlineSetup();
    }
}
