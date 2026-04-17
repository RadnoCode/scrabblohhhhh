package com.kotva.presentation.controller;

import com.kotva.application.runtime.LanLaunchConfig;
import com.kotva.lan.LanClientConnector;
import com.kotva.lan.LanLobbyClientSession;
import com.kotva.lan.udp.DiscoveredRoom;
import com.kotva.lan.udp.LanHostBroadcaster;
import com.kotva.lan.udp.LanRoomScanner;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.RoomWaitingContext;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import com.kotva.presentation.viewmodel.RoomViewModel;
import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * RoomSearchController handles the LAN room discovery page.
 */
public class RoomSearchController {
    private final SceneNavigator navigator;
    private final RoomViewModel viewModel;
    private final ObservableList<DiscoveredRoom> roomItems;

    private LanRoomScanner roomScanner;
    private ListView<DiscoveredRoom> roomListView;
    private Label statusLabel;
    private String lastSearchQuery = "";
    private DiscoveredRoom selectedRoom;

    public RoomSearchController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new RoomViewModel(
                "SCRABBLE",
                "Type host:port or select a LAN room",
                "Waiting for players...");
        this.roomItems = FXCollections.observableArrayList();
    }

    public RoomViewModel getViewModel() {
        return viewModel;
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> {
            stopScanning();
            navigator.goBack();
        });
    }

    public void bindSearchField(TextField searchField) {
        searchField.setPromptText(viewModel.getSearchPromptText());
        searchField.setOnAction(event -> {
            lastSearchQuery = searchField.getText() == null ? "" : searchField.getText().trim();
            joinLobby(lastSearchQuery);
        });
    }

    public void bindRoomList(ListView<DiscoveredRoom> roomListView) {
        this.roomListView = roomListView;
        roomListView.setItems(roomItems);
        roomListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(DiscoveredRoom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayText());
            }
        });
        roomListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedRoom = newValue;
            String selectedText = newValue == null ? "" : newValue.displayText();
            viewModel.setSelectedRoomText(selectedText);
            if (newValue != null) {
                updateStatus("Selected " + newValue.hostPlayerName() + " at " + newValue.createEndpoint());
            }
        });
    }

    public void bindStatusLabel(Label statusLabel) {
        this.statusLabel = statusLabel;
        statusLabel.setText(viewModel.getStatusText());
    }

    public void bindJoinAction(CommonButton joinButton) {
        joinButton.setOnAction(event -> {
            if (selectedRoom != null) {
                joinLobby(selectedRoom.createEndpoint());
                return;
            }
            joinLobby(lastSearchQuery);
        });
    }

    public void bindRefreshAction(CommonButton refreshButton) {
        refreshButton.setOnAction(event -> restartScanning());
    }

    public void startScanning() {
        restartScanning();
    }

    public void stopScanning() {
        if (roomScanner != null) {
            roomScanner.stop();
            roomScanner = null;
        }
        viewModel.setScanning(false);
    }

    private void restartScanning() {
        stopScanning();
        roomItems.clear();
        viewModel.setRooms(List.of());
        selectedRoom = null;
        viewModel.setSelectedRoomText("");
        updateStatus("Scanning LAN rooms...");

        try {
            roomScanner = new LanRoomScanner(LanHostBroadcaster.BROADCAST_PORT);
            roomScanner.startScanning(this::handleRoomScanUpdate);
            viewModel.setScanning(true);
        } catch (IOException exception) {
            updateStatus("Failed to start LAN scanning: " + exception.getMessage());
        }
    }

    private void handleRoomScanUpdate(List<DiscoveredRoom> rooms) {
        Platform.runLater(() -> {
            viewModel.setRooms(rooms);
            roomItems.setAll(rooms);
            if (selectedRoom != null) {
                selectedRoom = rooms.stream()
                        .filter(room -> room.uniqueKey().equals(selectedRoom.uniqueKey()))
                        .findFirst()
                        .orElse(null);
                if (selectedRoom != null && roomListView != null) {
                    roomListView.getSelectionModel().select(selectedRoom);
                }
            }
            if (rooms.isEmpty()) {
                updateStatus("No LAN room found. You can still type host:port manually.");
            } else if (selectedRoom == null) {
                updateStatus("Found " + rooms.size() + " LAN room(s). Select one to join.");
            }
        });
    }

    private void joinLobby(String endpoint) {
        String normalizedEndpoint = endpoint == null ? "" : endpoint.trim();
        if (normalizedEndpoint.isBlank()) {
            updateStatus("Select a LAN room or type host:port first.");
            return;
        }

        updateStatus("Joining lobby at " + normalizedEndpoint + "...");
        Thread connectionThread = new Thread(() -> {
            try {
                LanLobbyClientSession lobbyClientSession =
                        LanClientConnector.joinLobby(normalizedEndpoint, "Guest");
                String playerCountLabel =
                        String.valueOf(
                                lobbyClientSession.getLobbySnapshot().getSettings().getMaxPlayers());
                Platform.runLater(() -> {
                    stopScanning();
                    navigator.showRoomWaiting(
                            RoomWaitingContext.forClient(
                                    "Search Room",
                                    resolveGameTimeLabel(lobbyClientSession.getLobbySnapshot()),
                                    resolveLanguageLabel(lobbyClientSession.getLobbySnapshot()),
                                    playerCountLabel,
                                    lobbyClientSession));
                });
            } catch (Exception exception) {
                Platform.runLater(
                        () -> updateStatus("Failed to join LAN room: " + exception.getMessage()));
            }
        }, "LAN-ClientConnect");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void updateStatus(String message) {
        viewModel.setStatusText(message);
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private String resolveGameTimeLabel(LanLaunchConfig lanLaunchConfig) {
        if (lanLaunchConfig.getGameConfig().getTimeControlConfig() == null) {
            return "--";
        }
        long minutes =
                lanLaunchConfig.getGameConfig().getTimeControlConfig().getMainTimeMillis() / 60_000L;
        return minutes + "min";
    }

    private String resolveLanguageLabel(LanLaunchConfig lanLaunchConfig) {
        return switch (lanLaunchConfig.getGameConfig().getDictionaryType()) {
            case BR -> "British";
            case AM -> "American";
        };
    }

    private String resolveGameTimeLabel(com.kotva.lan.LanLobbySnapshot snapshot) {
        if (snapshot.getSettings().getTimeControlConfig() == null) {
            return "--";
        }
        long minutes = snapshot.getSettings().getTimeControlConfig().getMainTimeMillis() / 60_000L;
        return minutes + "min";
    }

    private String resolveLanguageLabel(com.kotva.lan.LanLobbySnapshot snapshot) {
        return switch (snapshot.getSettings().getDictionaryType()) {
            case BR -> "British";
            case AM -> "American";
        };
    }
}
