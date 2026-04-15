package com.kotva.presentation.controller;

import com.kotva.application.runtime.LanLaunchConfig;
import com.kotva.lan.LanClientConnector;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.RoomPanelView;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import com.kotva.presentation.viewmodel.RoomViewModel;
import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 * RoomSearchController handles the room search page interactions.
 */
public class RoomSearchController {
    private final SceneNavigator navigator;
    private final RoomViewModel viewModel;
    private String lastSearchQuery = "";

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
            lastSearchQuery = searchField.getText();
            connectToRoom(lastSearchQuery);
        });
    }

    public void bindRoomPanelAction(RoomPanelView roomPanelView) {
        roomPanelView.setOnMouseClicked(event -> connectToRoom(lastSearchQuery));
    }

    private void connectToRoom(String endpoint) {
        Thread connectionThread = new Thread(() -> {
            try {
                LanLaunchConfig lanLaunchConfig = LanClientConnector.connect(endpoint);
                GameLaunchContext launchContext =
                        GameLaunchContext.forLanClient(
                                lanLaunchConfig,
                                "Search Room",
                                resolveGameTimeLabel(lanLaunchConfig),
                                resolveLanguageLabel(lanLaunchConfig),
                                String.valueOf(lanLaunchConfig.getGameConfig().getPlayerCount()));
                Platform.runLater(() -> navigator.showGame(launchContext));
            } catch (Exception exception) {
                System.err.println("Failed to join LAN room: " + exception.getMessage());
            }
        }, "LAN-ClientConnect");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private String resolveGameTimeLabel(LanLaunchConfig lanLaunchConfig) {
        if (lanLaunchConfig.getGameConfig().getTimeControlConfig() == null) {
            return "--";
        }
        long minutes = lanLaunchConfig.getGameConfig().getTimeControlConfig().getMainTimeMillis() / 60_000L;
        return minutes + "min";
    }

    private String resolveLanguageLabel(LanLaunchConfig lanLaunchConfig) {
        return switch (lanLaunchConfig.getGameConfig().getDictionaryType()) {
            case BR -> "British";
            case AM -> "American";
        };
    }
}
