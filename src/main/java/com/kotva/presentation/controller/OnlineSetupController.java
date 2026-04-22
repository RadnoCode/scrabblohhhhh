package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;

/**
 * OnlineSetupController handles the online setup detail page.
 */
public class OnlineSetupController {
    private final SceneNavigator navigator;
    private final GameBranchSetupViewModel viewModel;

    public OnlineSetupController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new GameBranchSetupViewModel(
                "SCRABBLE",
                "Play By LAN",
                "Search Room",
                "Create Room",
                null);
    }

    public GameBranchSetupViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(CommonButton firstButton, CommonButton secondButton) {
        firstButton.setOnAction(event -> navigateToSearchRoom());
        secondButton.setOnAction(event -> navigateToCreateRoom());
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    public void navigateToSearchRoom() {
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showRoomSearch();
    }

    public void navigateToCreateRoom() {
        navigator.showRoomCreate();
    }
}
