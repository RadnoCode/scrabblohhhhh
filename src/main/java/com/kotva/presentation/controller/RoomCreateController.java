package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import com.kotva.presentation.viewmodel.GameLaunchContext;

/**
 * RoomCreateController handles the create-room page.
 * Its current layout and option structure intentionally match the
 * LocalMultiplayerSetup page.
 */
public class RoomCreateController {
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

    private GameLaunchContext buildLaunchContext() {
        return GameLaunchContext.forRoomCreate(
                gameTimes[gameTimeIndex],
                languages[languageIndex],
                playerCounts[playerCountIndex]);
    }

    private void navigateToGame() {
        navigator.showGame(buildLaunchContext());
    }
}
