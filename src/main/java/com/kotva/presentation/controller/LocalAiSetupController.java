package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import com.kotva.presentation.viewmodel.GameLaunchContext;

/**
 * LocalAiSetupController handles the local AI detail setup page.
 */
public class LocalAiSetupController {
    private final SceneNavigator navigator;
    private final GameBranchSetupViewModel viewModel;
    private final String[] gameTimes = {"15min", "30min", "45min"};
    private final String[] languages = {"American", "British"};
    private final String[] difficulties = {"Easy", "Middle", "Hard"};
    private int gameTimeIndex;
    private int languageIndex;
    private int difficultyIndex;

    public LocalAiSetupController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new GameBranchSetupViewModel(
                "SCRABBLE",
                "Play With Robot",
                "Select Game Time",
                "Language",
                "Difficulty");
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
        thirdButton.setCurrentValue(difficulties[difficultyIndex]);

        firstButton.setOnSwitchAction(this::rotateGameTime);
        secondButton.setOnSwitchAction(this::rotateLanguage);
        thirdButton.setOnSwitchAction(this::rotateDifficulty);
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

    private String rotateDifficulty() {
        difficultyIndex = (difficultyIndex + 1) % difficulties.length;
        return difficulties[difficultyIndex];
    }

    private GameLaunchContext buildLaunchContext() {
        return GameLaunchContext.forLocalAi(
                gameTimes[gameTimeIndex],
                languages[languageIndex],
                difficulties[difficultyIndex]);
    }

    private void navigateToGame() {
        navigator.showGame(buildLaunchContext());
    }
}
