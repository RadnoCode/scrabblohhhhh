package com.kotva.presentation.controller;

import com.kotva.infrastructure.logging.AppLog;
import com.kotva.lan.GameSessionBroker;
import com.kotva.lan.LanLobbySettings;
import com.kotva.lan.LanLobbySnapshot;
import com.kotva.lan.discovery.LanDiscoveryHostService;
import com.kotva.lan.discovery.UdpLanDiscoveryHostService;
import com.kotva.lan.udp.DiscoveredRoom;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.fx.RoomWaitingContext;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import javafx.scene.control.Alert;

/**
 * RoomCreateController handles the create-room page.
 * Its current layout and option structure intentionally match the
 * LocalMultiplayerSetup page.
 */
public class RoomCreateController {
    private static final String HOST_PLAYER_ID = "player-1";
    private static final String HOST_PLAYER_NAME = "Host";
    private static final String DEFAULT_GAME_TIME_MINUTES = "15";
    private static final String DEFAULT_STEP_TIME_SECONDS = "30";
    private static final String INVALID_GAME_TIME_MESSAGE =
        "Please enter an integer between 15 and 90 minutes.";
    private static final String INVALID_STEP_TIME_MESSAGE =
        "Please enter an integer between 0 and 180 seconds.";

    private final SceneNavigator navigator;
    private final GameBranchSetupViewModel viewModel;
    private final String[] languages = {"American", "British"};
    private final String[] playerCounts = {"2", "3", "4"};
    private int languageIndex;
    private int playerCountIndex;

    public RoomCreateController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new GameBranchSetupViewModel(
                "SCRABBLE",
                "Create Room",
                "Select Game Time",
                "Language",
                "Number of Player");
    }

    public GameBranchSetupViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(
            InputButton gameTimeButton,
            InputButton stepTimeButton,
            SwitchButton dictionaryButton,
            SwitchButton playerCountButton,
            CommonButton goButton,
            TransientMessageView messageView) {
        gameTimeButton.setInputText(DEFAULT_GAME_TIME_MINUTES);
        stepTimeButton.setInputText(DEFAULT_STEP_TIME_SECONDS);
        dictionaryButton.setCurrentValue(languages[languageIndex]);
        playerCountButton.setCurrentValue(playerCounts[playerCountIndex]);

        dictionaryButton.setOnSwitchAction(this::rotateLanguage);
        playerCountButton.setOnSwitchAction(this::rotatePlayerCount);
        goButton.setOnAction(event -> navigateToGame(gameTimeButton, stepTimeButton, messageView));
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    private String rotateLanguage() {
        languageIndex = (languageIndex + 1) % languages.length;
        return languages[languageIndex];
    }

    private String rotatePlayerCount() {
        playerCountIndex = (playerCountIndex + 1) % playerCounts.length;
        return playerCounts[playerCountIndex];
    }

    private void navigateToGame(
            InputButton gameTimeButton,
            InputButton stepTimeButton,
            TransientMessageView messageView) {
        String gameTimeInput = gameTimeButton.getTextField().getText();
        String stepTimeInput = stepTimeButton.getTextField().getText();
        if (!isValidIntegerInRange(gameTimeInput, 15, 90)) {
            messageView.showMessage(INVALID_GAME_TIME_MESSAGE);
            return;
        }
        if (!isValidIntegerInRange(stepTimeInput, 0, 180)) {
            messageView.showMessage(INVALID_STEP_TIME_MESSAGE);
            return;
        }

        try {
            GameSessionBroker broker = new GameSessionBroker(GameSessionBroker.DEFAULT_PORT);
            LanLobbySettings settings = new LanLobbySettings(
                    "British".equalsIgnoreCase(languages[languageIndex])
                            ? com.kotva.policy.DictionaryType.BR
                            : com.kotva.policy.DictionaryType.AM,
                    com.kotva.presentation.viewmodel.GameLaunchContext
                            .forRoomCreate(
                                    gameTimeInput,
                                    stepTimeInput,
                                    languages[languageIndex],
                                    playerCounts[playerCountIndex])
                            .getRequest()
                            .getTimeControlConfig(),
                    Integer.parseInt(playerCounts[playerCountIndex]));
            broker.createLobby(settings, HOST_PLAYER_ID, HOST_PLAYER_NAME);

            LanDiscoveryHostService discoveryHostService = new UdpLanDiscoveryHostService();
            String gameTimeDisplay = gameTimeInput + "min";
            discoveryHostService.startHosting(
                () -> buildDiscoveredRoom(broker, settings, gameTimeDisplay));

            navigator.showRoomWaiting(
                    RoomWaitingContext.forHost(
                            "Create Room",
                            gameTimeDisplay,
                            languages[languageIndex],
                            playerCounts[playerCountIndex],
                            broker,
                            discoveryHostService));
        } catch (Exception exception) {
            AppLog.logException(RoomCreateController.class, "Failed to create LAN room.", exception);
            showError("Failed to create LAN room", exception.getMessage());
        }
    }

    private DiscoveredRoom buildDiscoveredRoom(
            GameSessionBroker broker,
            LanLobbySettings settings,
            String gameTimeDisplay) {
        LanLobbySnapshot snapshot = broker.getLobbySnapshot();
        int currentPlayers = snapshot == null ? 1 : snapshot.getCurrentPlayerCount();
        return new DiscoveredRoom(
                snapshot == null ? "lobby" : snapshot.getLobbyId(),
                HOST_PLAYER_NAME,
                "",
                broker.getBoundPort(),
                currentPlayers,
                settings.getMaxPlayers(),
                languages[languageIndex],
                gameTimeDisplay,
                System.currentTimeMillis());
    }

    private void showError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private boolean isValidIntegerInRange(String rawInput, int min, int max) {
        if (rawInput == null) {
            return false;
        }

        String normalizedInput = rawInput.trim();
        if (!normalizedInput.matches("\\d+")) {
            return false;
        }

        try {
            int value = Integer.parseInt(normalizedInput);
            return value >= min && value <= max;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
