package com.kotva.presentation.controller;

import com.kotva.lan.GameSessionBroker;
import com.kotva.lan.LanLobbySettings;
import com.kotva.lan.LanLobbySnapshot;
import com.kotva.lan.udp.DiscoveredRoom;
import com.kotva.lan.udp.LanHostBroadcaster;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.SwitchButton;
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

    private final SceneNavigator navigator;
    private final GameBranchSetupViewModel viewModel;
    private final String[] gameTimes = {"15min", "30min", "45min"};
    private final String[] languages = {"American", "British"};
    private final String[] playerCounts = {"2", "3", "4"};
    private int gameTimeIndex;
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
            SwitchButton firstButton,
            SwitchButton secondButton,
            SwitchButton thirdButton,
            CommonButton goButton) {
        firstButton.setCurrentValue(gameTimes[gameTimeIndex]);
        secondButton.setCurrentValue(languages[languageIndex]);
        thirdButton.setCurrentValue(playerCounts[playerCountIndex]);

        firstButton.setOnSwitchAction(this::rotateGameTime);
        secondButton.setOnSwitchAction(this::rotateLanguage);
        thirdButton.setOnSwitchAction(this::rotatePlayerCount);
        goButton.setOnAction(event -> navigateToGame());
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    private String rotateGameTime() {
        gameTimeIndex = (gameTimeIndex + 1) % gameTimes.length;
        return gameTimes[gameTimeIndex];
    }

    private String rotateLanguage() {
        languageIndex = (languageIndex + 1) % languages.length;
        return languages[languageIndex];
    }

    private String rotatePlayerCount() {
        playerCountIndex = (playerCountIndex + 1) % playerCounts.length;
        return playerCounts[playerCountIndex];
    }

    private void navigateToGame() {
        try {
            GameSessionBroker broker = new GameSessionBroker(GameSessionBroker.DEFAULT_PORT);
            LanLobbySettings settings = new LanLobbySettings(
                    "British".equalsIgnoreCase(languages[languageIndex])
                            ? com.kotva.policy.DictionaryType.BR
                            : com.kotva.policy.DictionaryType.AM,
                    com.kotva.presentation.viewmodel.GameLaunchContext
                            .forRoomCreate(gameTimes[gameTimeIndex], languages[languageIndex], playerCounts[playerCountIndex])
                            .getRequest()
                            .getTimeControlConfig(),
                    Integer.parseInt(playerCounts[playerCountIndex]));
            broker.createLobby(settings, HOST_PLAYER_ID, HOST_PLAYER_NAME);

            LanHostBroadcaster broadcaster = new LanHostBroadcaster();
            broadcaster.startBroadcasting(() -> buildDiscoveredRoom(broker, settings));

            navigator.showRoomWaiting(
                    RoomWaitingContext.forHost(
                            "Create Room",
                            gameTimes[gameTimeIndex],
                            languages[languageIndex],
                            playerCounts[playerCountIndex],
                            broker,
                            broadcaster));
        } catch (Exception exception) {
            showError("Failed to create LAN room", exception.getMessage());
        }
    }

    private DiscoveredRoom buildDiscoveredRoom(
            GameSessionBroker broker,
            LanLobbySettings settings) {
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
                gameTimes[gameTimeIndex],
                System.currentTimeMillis());
    }

    private void showError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
