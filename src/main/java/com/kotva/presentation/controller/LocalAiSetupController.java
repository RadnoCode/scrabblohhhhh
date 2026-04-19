package com.kotva.presentation.controller;

import com.kotva.infrastructure.AudioManager;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import com.kotva.presentation.viewmodel.GameLaunchContext;

public class LocalAiSetupController {
    private static final String DEFAULT_GAME_TIME_MINUTES = "15";
    private static final String INVALID_GAME_TIME_MESSAGE =
    "Please enter an integer between 15 and 90 minutes.";

    private final SceneNavigator navigator;
    private final GameBranchSetupViewModel viewModel;
    private final AudioManager audioManager;
    private final String[] dictionaries = {"North American", "British"};
    private final String[] difficulties = {"Easy", "Middle", "Hard"};
    private int dictionaryIndex;
    private int difficultyIndex;

    public LocalAiSetupController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.audioManager = navigator.getAppContext().getAudioManager();
        this.viewModel = new GameBranchSetupViewModel(
            "SCRABBLE",
            "Play With Robot",
            "Select Game Time",
            "Dictionary",
            "Difficulty");
    }

    public GameBranchSetupViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(
        InputButton firstButton,
        SwitchButton secondButton,
        SwitchButton thirdButton,
        CommonButton goButton,
        TransientMessageView messageView) {
        firstButton.setInputText(DEFAULT_GAME_TIME_MINUTES);
        secondButton.setCurrentValue(dictionaries[dictionaryIndex]);
        thirdButton.setCurrentValue(difficulties[difficultyIndex]);

        secondButton.setOnSwitchAction(this::rotateDictionary);
        thirdButton.setOnSwitchAction(this::rotateDifficulty);
        goButton.setOnAction(event -> navigateToGame(firstButton, messageView));
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    private String rotateDictionary() {
        dictionaryIndex = (dictionaryIndex + 1) % dictionaries.length;
        return dictionaries[dictionaryIndex];
    }

    private String rotateDifficulty() {
        difficultyIndex = (difficultyIndex + 1) % difficulties.length;
        return difficulties[difficultyIndex];
    }

    private GameLaunchContext buildLaunchContext(String gameTimeInput) {
        return GameLaunchContext.forLocalAi(
            gameTimeInput,
            dictionaries[dictionaryIndex],
            difficulties[difficultyIndex]);
    }

    private void navigateToGame(InputButton gameTimeButton, TransientMessageView messageView) {
        if (!isValidGameTimeInput(gameTimeButton.getTextField().getText())) {
            messageView.showMessage(INVALID_GAME_TIME_MESSAGE);
            return;
        }
        audioManager.playActionConfirm();
        navigator.showGame(buildLaunchContext(gameTimeButton.getTextField().getText()));
    }

    private boolean isValidGameTimeInput(String rawInput) {
        if (rawInput == null) {
            return false;
        }

        String normalizedInput = rawInput.trim();
        if (!normalizedInput.matches("\\d+")) {
            return false;
        }

        try {
            int minutes = Integer.parseInt(normalizedInput);
            return minutes >= 15 && minutes <= 90;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
