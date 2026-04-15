package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.RoomViewModel;

/**
 * RoomWaitingController handles the simple waiting room page.
 */
public class RoomWaitingController {
    private final SceneNavigator navigator;
    private final RoomViewModel viewModel;

    public RoomWaitingController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new RoomViewModel(
                "SCRABBLE",
                "Search room...",
                "Waiting for players...");
    }

    public RoomViewModel getViewModel() {
        return viewModel;
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }
}
