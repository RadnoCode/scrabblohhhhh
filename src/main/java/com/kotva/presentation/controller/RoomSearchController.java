package com.kotva.presentation.controller;

import com.kotva.application.runtime.LanLaunchConfig;
import com.kotva.infrastructure.logging.AppLog;
import com.kotva.lan.LanClientConnector;
import com.kotva.lan.LanLobbyClientSession;
import com.kotva.lan.discovery.LanDiscoveryClientService;
import com.kotva.lan.discovery.UdpLanDiscoveryClientService;
import com.kotva.lan.udp.DiscoveredRoom;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.RoomWaitingContext;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import com.kotva.presentation.viewmodel.RoomViewModel;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
        searchField.setOnAction(event -> {
            joinLobby(resolveManualEndpoint());
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
            String manualEndpoint = resolveManualEndpoint();
            if (!manualEndpoint.isBlank()) {
                joinLobby(manualEndpoint);
                return;
            }
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

    private void joinLobby(String endpoint) {
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

        updateStatus("Joining lobby at " + resolvedEndpoint + "...");
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
                AppLog.logException(
                        RoomSearchController.class,
                        "Failed to join LAN lobby at " + resolvedEndpoint + ".",
                        exception);
                Platform.runLater(
                        () -> updateStatus(formatJoinFailure(resolvedEndpoint, exception)));
            }
        }, "LAN-ClientConnect");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private String resolveManualEndpoint() {
        if (searchField != null && searchField.getText() != null) {
            return searchField.getText().trim();
        }
        return lastSearchQuery;
    }

    private String formatJoinFailure(String endpoint, Exception exception) {
        Throwable cause = rootCause(exception);

        if (cause instanceof UnknownHostException) {
            return "Could not resolve " + endpoint
                    + ". Use a plain host:port LAN address such as 10.190.129.253:5050.";
        }
        if (cause instanceof NoRouteToHostException) {
            return "No route to " + endpoint
                    + ". Verify both devices are on the same LAN/hotspot and that VPN or proxy software is off.";
        }
        if (cause instanceof ConnectException connectException) {
            String message = safeMessage(connectException);
            if (message.toLowerCase().contains("refused")) {
                return "Reached "
                        + endpoint
                        + ", but nothing accepted the connection. Make sure the host opened a room and port 5050 is allowed.";
            }
            return "Failed to connect to " + endpoint + ": " + message;
        }
        if (cause instanceof SocketTimeoutException) {
            return "Timed out while connecting to "
                    + endpoint
                    + ". The host did not finish the LAN handshake in time.";
        }
        if (cause instanceof EOFException) {
            return "Connected to " + endpoint + ", but the host closed the connection during handshake.";
        }
        if (cause instanceof IOException ioException) {
            String message = safeMessage(ioException);
            if (message.contains("Expected LobbyStateMessage")) {
                return "Connected to "
                        + endpoint
                        + ", but it is not a compatible LAN lobby host.";
            }
            if (message.contains("missing required join data")) {
                return "Connected to " + endpoint + ", but the host sent an incomplete lobby handshake.";
            }
        }
        return "Failed to join LAN room at " + endpoint + ": " + safeMessage(cause);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return throwable == null ? "unknown error" : throwable.getClass().getSimpleName();
        }
        return throwable.getMessage();
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
