package com.kotva.presentation.controller;

import com.kotva.infrastructure.logging.AppLog;
import com.kotva.lan.LanClientConnector;
import com.kotva.lan.discovery.LanDiscoveryClientService;
import com.kotva.lan.discovery.UdpLanDiscoveryClientService;
import com.kotva.lan.udp.DiscoveredRoom;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.PlayerNameSetupContext;
import com.kotva.presentation.fx.SceneNavigator;
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

    private LanDiscoveryClientService discoveryClientService;
    private ListView<DiscoveredRoom> roomListView;
    private Label statusLabel;
    private TextField searchField;
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
        this.searchField = searchField;
        searchField.setPromptText(viewModel.getSearchPromptText());
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                lastSearchQuery = newValue == null ? "" : newValue.trim());
        searchField.setOnAction(event -> navigateToNicknameSetup(resolveManualEndpoint(), null));
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
                updateStatus(
                    "Selected "
                        + newValue.displayRoomName()
                        + " hosted by "
                        + newValue.hostPlayerName()
                        + " at "
                        + newValue.createEndpoint());
            }
        });
    }

    public void bindStatusLabel(Label statusLabel) {
        this.statusLabel = statusLabel;
        statusLabel.setText(viewModel.getStatusText());
    }

    public void bindJoinAction(CommonButton joinButton) {
        joinButton.setOnAction(event -> {
            String manualEndpoint = resolveManualEndpoint();
            if (!manualEndpoint.isBlank()) {
                navigateToNicknameSetup(manualEndpoint, null);
                return;
            }
            if (selectedRoom != null) {
                navigateToNicknameSetup(selectedRoom.createEndpoint(), selectedRoom);
                return;
            }
            navigateToNicknameSetup(lastSearchQuery, null);
        });
    }

    public void bindRefreshAction(CommonButton refreshButton) {
        refreshButton.setOnAction(event -> restartScanning());
    }

    public void startScanning() {
        restartScanning();
    }

    public void stopScanning() {
        if (discoveryClientService != null) {
            discoveryClientService.stop();
            discoveryClientService = null;
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
            discoveryClientService = new UdpLanDiscoveryClientService();
            discoveryClientService.startScanning(this::handleRoomScanUpdate);
            viewModel.setScanning(true);
        } catch (IOException exception) {
            AppLog.logException(RoomSearchController.class, "Failed to start LAN scanning.", exception);
            updateStatus("Failed to start LAN scanning: " + exception.getMessage());
        }
    }

    private void handleRoomScanUpdate(List<DiscoveredRoom> rooms) {
        Platform.runLater(() -> {
            String previouslySelectedKey =
                    selectedRoom != null ? selectedRoom.uniqueKey() : null;

            viewModel.setRooms(rooms);
            roomItems.setAll(rooms);

            if (previouslySelectedKey != null) {
                DiscoveredRoom restoredSelection = rooms.stream()
                        .filter(room -> room.uniqueKey().equals(previouslySelectedKey))
                        .findFirst()
                        .orElse(null);

                if (restoredSelection != null) {
                    selectedRoom = restoredSelection;
                    if (roomListView != null) {
                        roomListView.getSelectionModel().select(restoredSelection);
                    }
                } else if (roomListView != null) {
                    roomListView.getSelectionModel().clearSelection();
                }
            }
            if (rooms.isEmpty()) {
                updateStatus("No LAN room found. You can still type host:port manually.");
            } else if (selectedRoom == null) {
                updateStatus("Found " + rooms.size() + " LAN room(s). Select one to join.");
            }
        });
    }

    private void navigateToNicknameSetup(String endpoint, DiscoveredRoom roomMetadata) {
        String normalizedEndpoint = LanClientConnector.sanitizeEndpointInput(endpoint);
        if (normalizedEndpoint.isBlank()) {
            updateStatus("Select a LAN room or type host:port first.");
            return;
        }

        final String resolvedEndpoint;
        try {
            resolvedEndpoint = LanClientConnector.normalizeEndpointForDisplay(normalizedEndpoint);
        } catch (IllegalArgumentException exception) {
            updateStatus(exception.getMessage());
            return;
        }

        stopScanning();
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showPlayerNameSetup(
                PlayerNameSetupContext.forLanClient(
                        roomMetadata == null ? "Direct LAN Room" : roomMetadata.displayRoomName(),
                        resolvedEndpoint,
                        roomMetadata == null ? "--" : roomMetadata.timeLabel(),
                        roomMetadata == null ? "--" : roomMetadata.dictionaryLabel(),
                        roomMetadata == null ? "--" : String.valueOf(roomMetadata.maxPlayers())));
    }

    private String resolveManualEndpoint() {
        if (searchField != null && searchField.getText() != null) {
            return searchField.getText().trim();
        }
        return lastSearchQuery;
    }

    private void updateStatus(String message) {
        viewModel.setStatusText(message);
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}
