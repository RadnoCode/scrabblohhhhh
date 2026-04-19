package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

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
        firstButton.setOnAction(createFirstHandler());
        secondButton.setOnAction(createSecondHandler());
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    public EventHandler<ActionEvent> createFirstHandler() {
        return event -> navigator.showRoomSearch();
    }

    public EventHandler<ActionEvent> createSecondHandler() {
        return event -> navigator.showRoomCreate();
    }
}
