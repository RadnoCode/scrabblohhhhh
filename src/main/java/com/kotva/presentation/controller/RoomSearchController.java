package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.RoomPanelView;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.RoomViewModel;
import javafx.scene.control.TextField;

/**
 * RoomSearchController handles the room search page interactions.
 */
public class RoomSearchController {
    private final SceneNavigator navigator;
    private final RoomViewModel viewModel;

    public RoomSearchController(SceneNavigator navigator) {
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

    public void bindSearchField(TextField searchField) {
        searchField.setPromptText(viewModel.getSearchPromptText());
        searchField.setOnAction(event -> {
            String query = searchField.getText();
            System.out.println("Room search: query = " + query);
        });
    }

    public void bindRoomPanelAction(RoomPanelView roomPanelView) {
        roomPanelView.setOnMouseClicked(event -> {
            System.out.println("Room search: room selected.");
            navigator.showRoomWaiting();
        });
    }
}
