package com.kotva.presentation.controller;

import com.kotva.infrastructure.logging.AppLog;
import com.kotva.lan.GameSessionBroker;
import com.kotva.lan.LanClientConnector;
import com.kotva.lan.LanLobbyClientSession;
import com.kotva.lan.LanLobbySettings;
import com.kotva.lan.LanLobbySnapshot;
import com.kotva.lan.discovery.LanDiscoveryHostService;
import com.kotva.lan.discovery.UdpLanDiscoveryHostService;
import com.kotva.lan.udp.DiscoveredRoom;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.PlayerNameCardView;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.fx.PlayerNameSetupContext;
import com.kotva.presentation.fx.RoomWaitingContext;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javafx.application.Platform;

public class PlayerNameSetupController {
    private static final String HOST_PLAYER_ID = "player-1";
    private static final String BLANK_NAME_MESSAGE = "Nickname cannot be blank.";
    private static final String DUPLICATE_NAME_MESSAGE = "Player nicknames must be unique.";

    private final SceneNavigator navigator;
    private final PlayerNameSetupContext context;

    public PlayerNameSetupController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.context = navigator.getPlayerNameSetupContext();
    }

    public PlayerNameSetupContext getContext() {
        return context;
    }

    public void bindPrimaryAction(
            CommonButton primaryButton,
            List<PlayerNameCardView> playerCards,
            TransientMessageView messageView) {
        primaryButton.setText(context.getConfirmButtonText());
        primaryButton.setOnAction(event -> handlePrimaryAction(primaryButton, playerCards, messageView));
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    private void handlePrimaryAction(
            CommonButton primaryButton,
            List<PlayerNameCardView> playerCards,
            TransientMessageView messageView) {
        List<String> validatedNames = validateNames(playerCards, messageView);
        if (validatedNames == null) {
            return;
        }

        switch (context.getFlow()) {
            case HOT_SEAT -> launchHotSeat(validatedNames);
            case LAN_HOST -> createLanLobby(validatedNames.get(0), messageView);
            case LAN_CLIENT -> joinLanLobby(validatedNames.get(0), primaryButton, messageView);
        }
    }

    private List<String> validateNames(
            List<PlayerNameCardView> playerCards,
            TransientMessageView messageView) {
        if (playerCards == null || playerCards.isEmpty()) {
            messageView.showMessage(BLANK_NAME_MESSAGE);
            return null;
        }

        List<String> names = playerCards.stream()
                .map(PlayerNameCardView::getPlayerName)
                .map(name -> name == null ? "" : name.trim())
                .toList();

        if (names.stream().anyMatch(String::isBlank)) {
            messageView.showMessage(BLANK_NAME_MESSAGE);
            return null;
        }

        if (context.getFlow() == PlayerNameSetupContext.Flow.HOT_SEAT && hasDuplicates(names)) {
            messageView.showMessage(DUPLICATE_NAME_MESSAGE);
            return null;
        }

        return names;
    }

    private boolean hasDuplicates(List<String> names) {
        Set<String> seenNames = new HashSet<>();
        for (String name : names) {
            if (!seenNames.add(name.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void launchHotSeat(List<String> playerNames) {
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showGame(
                GameLaunchContext.forLocalMultiplayer(
                        context.getGameTimeLabel(),
                        context.getStepTimeSecondsLabel(),
                        context.getLanguageLabel(),
                        context.getPlayerCountLabel(),
                        playerNames));
    }

    private void createLanLobby(String hostPlayerName, TransientMessageView messageView) {
        GameSessionBroker broker = null;
        LanDiscoveryHostService discoveryHostService = null;
        try {
            broker = new GameSessionBroker(GameSessionBroker.DEFAULT_PORT);
            LanLobbySettings settings = new LanLobbySettings(
                    context.getRoomTitle(),
                    "British".equalsIgnoreCase(context.getLanguageLabel())
                            ? com.kotva.policy.DictionaryType.BR
                            : com.kotva.policy.DictionaryType.AM,
                    GameLaunchContext.forRoomCreate(
                                    context.getGameTimeLabel(),
                                    context.getStepTimeSecondsLabel(),
                                    context.getLanguageLabel(),
                                    context.getPlayerCountLabel())
                            .getRequest()
                            .getTimeControlConfig(),
                    Integer.parseInt(context.getPlayerCountLabel()));
            broker.createLobby(settings, HOST_PLAYER_ID, hostPlayerName);

            discoveryHostService = new UdpLanDiscoveryHostService();
            String gameTimeDisplay = formatGameTimeDisplay(context.getGameTimeLabel());
            GameSessionBroker discoveryBroker = broker;
            LanLobbySettings discoverySettings = settings;
            String discoveryHostPlayerName = hostPlayerName;
            discoveryHostService.startHosting(
                    () -> buildDiscoveredRoom(
                            discoveryBroker,
                            discoverySettings,
                            discoveryHostPlayerName,
                            gameTimeDisplay));

            navigator.requestNextSceneTitleEntranceAnimation();
            navigator.showRoomWaiting(
                    RoomWaitingContext.forHost(
                            context.getRoomTitle(),
                            gameTimeDisplay,
                            context.getLanguageLabel(),
                            context.getPlayerCountLabel(),
                            broker,
                            discoveryHostService));
        } catch (Exception exception) {
            if (discoveryHostService != null) {
                discoveryHostService.stop();
            }
            if (broker != null) {
                broker.stopServer();
            }
            AppLog.logException(PlayerNameSetupController.class, "Failed to create LAN room.", exception);
            messageView.showMessage("Failed to create LAN room: " + safeMessage(exception));
        }
    }

    private void joinLanLobby(
            String playerName,
            CommonButton primaryButton,
            TransientMessageView messageView) {
        primaryButton.setDisable(true);
        primaryButton.setText("Joining...");
        String endpoint = context.getEndpoint();

        Thread connectionThread = new Thread(() -> {
            try {
                LanLobbyClientSession lobbyClientSession = LanClientConnector.joinLobby(endpoint, playerName);
                LanLobbySnapshot snapshot = lobbyClientSession.getLobbySnapshot();
                String playerCountLabel = String.valueOf(snapshot.getSettings().getMaxPlayers());
                RoomWaitingContext waitingContext = RoomWaitingContext.forClient(
                        snapshot.getSettings().getRoomName(),
                        resolveGameTimeLabel(snapshot),
                        resolveLanguageLabel(snapshot),
                        playerCountLabel,
                        lobbyClientSession);
                Platform.runLater(() -> {
                    navigator.requestNextSceneTitleEntranceAnimation();
                    navigator.showRoomWaiting(waitingContext);
                });
            } catch (Exception exception) {
                AppLog.logException(
                        PlayerNameSetupController.class,
                        "Failed to join LAN lobby at " + endpoint + ".",
                        exception);
                Platform.runLater(() -> {
                    primaryButton.setDisable(false);
                    primaryButton.setText(context.getConfirmButtonText());
                    messageView.showMessage(formatJoinFailure(endpoint, exception));
                });
            }
        }, "LAN-PlayerNameJoin");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private DiscoveredRoom buildDiscoveredRoom(
            GameSessionBroker broker,
            LanLobbySettings settings,
            String hostPlayerName,
            String gameTimeDisplay) {
        LanLobbySnapshot snapshot = broker.getLobbySnapshot();
        int currentPlayers = snapshot == null ? 1 : snapshot.getCurrentPlayerCount();
        return new DiscoveredRoom(
                snapshot == null ? "lobby" : snapshot.getLobbyId(),
                settings.getRoomName(),
                snapshot == null || snapshot.getPlayers().isEmpty()
                        ? hostPlayerName
                        : snapshot.getPlayers().get(0).getPlayerName(),
                "",
                broker.getBoundPort(),
                currentPlayers,
                settings.getMaxPlayers(),
                context.getLanguageLabel(),
                gameTimeDisplay,
                System.currentTimeMillis());
    }

    private String resolveGameTimeLabel(LanLobbySnapshot snapshot) {
        if (snapshot == null || snapshot.getSettings().getTimeControlConfig() == null) {
            return "--";
        }
        long minutes = snapshot.getSettings().getTimeControlConfig().getMainTimeMillis() / 60_000L;
        return minutes + "min";
    }

    private String resolveLanguageLabel(LanLobbySnapshot snapshot) {
        if (snapshot == null) {
            return "--";
        }
        return switch (snapshot.getSettings().getDictionaryType()) {
            case BR -> "British";
            case AM -> "American";
        };
    }

    private String formatGameTimeDisplay(String gameTimeLabel) {
        if (gameTimeLabel == null || gameTimeLabel.isBlank()) {
            return "--";
        }
        String trimmed = gameTimeLabel.trim();
        return trimmed.matches(".*[A-Za-z].*") ? trimmed : trimmed + "min";
    }

    private String formatJoinFailure(String endpoint, Exception exception) {
        Throwable cause = rootCause(exception);

        if (cause instanceof UnknownHostException) {
            return "Could not resolve " + endpoint + ".";
        }
        if (cause instanceof NoRouteToHostException) {
            return "No route to " + endpoint + ".";
        }
        if (cause instanceof ConnectException connectException) {
            String message = safeMessage(connectException);
            if (message.toLowerCase(Locale.ROOT).contains("refused")) {
                return "Reached " + endpoint + ", but the room is unavailable.";
            }
            return "Failed to connect to " + endpoint + ": " + message;
        }
        if (cause instanceof SocketTimeoutException) {
            return "Timed out while connecting to " + endpoint + ".";
        }
        if (cause instanceof EOFException) {
            return "The host closed the connection while joining.";
        }
        if (cause instanceof IOException ioException) {
            String message = safeMessage(ioException);
            if (message.contains("Expected LobbyStateMessage")) {
                return "The selected address is not a compatible LAN lobby host.";
            }
        }
        return "Failed to join LAN room: " + safeMessage(cause);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current == null ? throwable : current;
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return throwable == null ? "unknown error" : throwable.getClass().getSimpleName();
        }
        return throwable.getMessage();
    }
}
