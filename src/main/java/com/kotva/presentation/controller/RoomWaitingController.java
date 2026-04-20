package com.kotva.presentation.controller;

import com.kotva.application.runtime.LobbyHostGameRuntime;
import com.kotva.application.runtime.LanLaunchConfig;
import com.kotva.infrastructure.logging.AppLog;
import com.kotva.lan.LanHostGameLaunch;
import com.kotva.lan.LanLobbyPlayerSnapshot;
import com.kotva.lan.LanLobbySnapshot;
import com.kotva.lan.LanSystemNotice;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.RoomWaitingContext;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.fx.UiScheduler;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import com.kotva.presentation.viewmodel.RoomViewModel;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Duration;

/**
 * RoomWaitingController coordinates the LAN waiting room.
 */
public class RoomWaitingController {
    private static final Duration LOBBY_POLL_INTERVAL = Duration.millis(200);
    private static final String JOIN_ADDRESS_PREFIX = "Join from another device: ";

    private final SceneNavigator navigator;
    private final RoomViewModel viewModel;
    private final RoomWaitingContext waitingContext;
    private final ObservableList<String> playerItems;
    private final String hostJoinEndpoint;

    private UiScheduler uiScheduler;
    private Label roomSummaryLabel;
    private Label statusLabel;
    private CommonButton primaryActionButton;
    private boolean clientDisconnectAlertShown;
    private String hostStatusOverride;

    public RoomWaitingController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.waitingContext = navigator.getRoomWaitingContext();
        this.viewModel = new RoomViewModel(
                "SCRABBLE",
                "Search room...",
                "Waiting for players...");
        this.playerItems = FXCollections.observableArrayList();
        this.hostJoinEndpoint = resolveHostJoinEndpoint();
        this.clientDisconnectAlertShown = false;
        this.hostStatusOverride = "";
    }

    public RoomViewModel getViewModel() {
        return viewModel;
    }

    public void bindPlayerList(ListView<String> playerListView) {
        playerListView.setItems(playerItems);
        playerListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });
    }

    public void bindRoomSummaryLabel(Label roomSummaryLabel) {
        this.roomSummaryLabel = roomSummaryLabel;
        roomSummaryLabel.setText(buildStaticRoomSummary());
    }

    public void bindStatusLabel(Label statusLabel) {
        this.statusLabel = statusLabel;
        statusLabel.setText(waitingContext == null
                ? "No waiting room is active."
                : waitingContext.isHost()
                        ? "Waiting for players to join..."
                        : "Connected to lobby. Waiting for host...");
    }

    public void bindJoinAddressLabel(Label joinAddressLabel) {
        if (waitingContext == null || !waitingContext.isHost() || hostJoinEndpoint.isBlank()) {
            joinAddressLabel.setVisible(false);
            joinAddressLabel.setManaged(false);
            joinAddressLabel.setText("");
            return;
        }

        joinAddressLabel.setText(JOIN_ADDRESS_PREFIX + hostJoinEndpoint);
    }

    public void bindPrimaryAction(CommonButton primaryActionButton) {
        this.primaryActionButton = primaryActionButton;
        if (waitingContext == null) {
            primaryActionButton.setText("Unavailable");
            primaryActionButton.setDisable(true);
            return;
        }
        if (waitingContext.isHost()) {
            primaryActionButton.setText("Start Game");
            primaryActionButton.setDisable(true);
            primaryActionButton.setOnAction(event -> startGameAsHost());
        } else {
            primaryActionButton.setText("Waiting...");
            primaryActionButton.setDisable(true);
        }
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> {
            shutdown();
            if (waitingContext != null) {
                waitingContext.closeForExit();
            }
            navigator.goBack();
        });
    }

    public void startMonitoring() {
        if (waitingContext == null) {
            return;
        }
        refreshFromContext();
        uiScheduler = new UiScheduler(LOBBY_POLL_INTERVAL, this::refreshFromContext);
        uiScheduler.start();
    }

    public void shutdown() {
        if (uiScheduler != null) {
            uiScheduler.stop();
            uiScheduler = null;
        }
    }

    private void refreshFromContext() {
        if (waitingContext == null) {
            return;
        }
        if (waitingContext.isHost()) {
            LanLobbySnapshot snapshot = waitingContext.requireHostBroker().getLobbySnapshot();
            renderLobbySnapshot(snapshot, true);
            consumeHostSystemNotices();
            return;
        }

        com.kotva.lan.LanLobbyClientSession clientSession = waitingContext.requireClientSession();
        LanLobbySnapshot snapshot = clientSession.getLobbySnapshot();
        renderLobbySnapshot(snapshot, false);
        LanSystemNotice disconnectNotice = clientSession.consumeDisconnectNotice();
        if (disconnectNotice != null) {
            applyClientDisconnectState(disconnectNotice);
            return;
        }
        if (clientSession.isDisconnected()) {
            applyClientDisconnectState(
                    new LanSystemNotice(
                            clientSession.getDisconnectSummary(),
                            clientSession.getDisconnectDetails(),
                            true));
            return;
        }
        if (clientSession.hasPendingStartLaunchConfig()) {
            LanLaunchConfig lanLaunchConfig = clientSession.consumeStartLaunchConfig();
            if (lanLaunchConfig != null) {
                shutdown();
                navigator.showGame(
                        GameLaunchContext.forLanClient(
                                lanLaunchConfig,
                                waitingContext.getRoomTitle(),
                                waitingContext.getGameTimeLabel(),
                                waitingContext.getLanguageLabel(),
                                waitingContext.getPlayerCountLabel()));
            }
        }
    }

    private void renderLobbySnapshot(LanLobbySnapshot snapshot, boolean hostView) {
        if (snapshot == null) {
            updateStatus("Lobby is unavailable.");
            playerItems.setAll(List.of());
            if (primaryActionButton != null && hostView) {
                primaryActionButton.setDisable(true);
            }
            return;
        }

        playerItems.setAll(snapshot.getPlayers().stream()
                .map(this::formatPlayerEntry)
                .toList());

        if (roomSummaryLabel != null) {
            roomSummaryLabel.setText(
                    buildStaticRoomSummary()
                            + " | Players "
                            + snapshot.getCurrentPlayerCount()
                            + "/"
                            + snapshot.getSettings().getMaxPlayers());
        }

        if (hostView) {
            updateStatus(resolveHostStatus(snapshot));
            if (primaryActionButton != null) {
                primaryActionButton.setDisable(!snapshot.canStart());
            }
        } else {
            updateStatus(
                    snapshot.getPhase().name().equals("WAITING_FOR_PLAYERS")
                            ? "Waiting for host to start the game..."
                            : "Host is starting the game...");
        }
    }

    private void startGameAsHost() {
        if (waitingContext == null || !waitingContext.isHost()) {
            return;
        }
        try {
            LanHostGameLaunch hostGameLaunch =
                    waitingContext.requireHostBroker().startGame(
                            navigator.getAppContext().getGameSetupService(),
                            navigator.getAppContext().getGameApplicationService());
            if (waitingContext.getHostDiscoveryService() != null) {
                waitingContext.getHostDiscoveryService().stop();
            }
            shutdown();
            navigator.showGame(
                    GameLaunchContext.forProvidedRuntime(
                            new LobbyHostGameRuntime(
                                    navigator.getAppContext().getGameApplicationService(),
                                    hostGameLaunch.session(),
                                    hostGameLaunch.lanHostService(),
                                    waitingContext.requireHostBroker()),
                            waitingContext.getRoomTitle(),
                            waitingContext.getGameTimeLabel(),
                            waitingContext.getLanguageLabel(),
                            waitingContext.getPlayerCountLabel()));
        } catch (Exception exception) {
            AppLog.logException(RoomWaitingController.class, "Failed to start LAN game from waiting room.", exception);
            updateStatus("Failed to start the game: " + exception.getMessage());
        }
    }

    private String buildStaticRoomSummary() {
        if (waitingContext == null) {
            return "No waiting room";
        }
        return waitingContext.getRoomTitle()
                + " | "
                + waitingContext.getLanguageLabel()
                + " | "
                + waitingContext.getGameTimeLabel()
                + " | "
                + waitingContext.getPlayerCountLabel() + " players";
    }

    private String formatPlayerEntry(LanLobbyPlayerSnapshot player) {
        return player.getPlayerName() + (player.isHost() ? " (Host)" : "");
    }

    private void updateStatus(String message) {
        viewModel.setStatusText(message);
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private String resolveHostJoinEndpoint() {
        if (waitingContext == null || !waitingContext.isHost()) {
            return "";
        }
        return waitingContext.requireHostBroker().getAdvertisedJoinEndpoint();
    }

    private void consumeHostSystemNotices() {
        List<LanSystemNotice> notices = waitingContext.requireHostBroker().drainSystemNotices();
        if (!notices.isEmpty()) {
            hostStatusOverride = notices.get(notices.size() - 1).summary();
            updateStatus(hostStatusOverride);
        }
    }

    private String resolveHostStatus(LanLobbySnapshot snapshot) {
        if (hostStatusOverride != null && !hostStatusOverride.isBlank()) {
            return hostStatusOverride;
        }
        return snapshot.canStart()
                ? "Players are ready. Start when you want."
                : "Need at least 2 players before starting.";
    }

    private void applyClientDisconnectState(LanSystemNotice notice) {
        updateStatus(notice.summary().isBlank() ? "Connection lost to host." : notice.summary());
        if (primaryActionButton != null) {
            primaryActionButton.setText("Disconnected");
            primaryActionButton.setDisable(true);
        }
        if (!clientDisconnectAlertShown) {
            clientDisconnectAlertShown = true;
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(resolveAlertHeader(notice));
            alert.setContentText(notice.details());
            alert.showAndWait();
        }
    }

    private String resolveAlertHeader(LanSystemNotice notice) {
        return notice.summary().isBlank() ? "Connection lost to host." : notice.summary();
    }
}
