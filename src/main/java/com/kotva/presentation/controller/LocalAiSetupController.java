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
    private static final String DEFAULT_STEP_TIME_SECONDS = "30";
    private static final String INVALID_GAME_TIME_MESSAGE =
        "Please enter an integer between 15 and 90 minutes.";
    private static final String INVALID_STEP_TIME_MESSAGE =
        "Please enter an integer between 0 and 180 seconds.";

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
            "Enter Game Time",
            "Dictionary",
            "Difficulty");
    }

    public GameBranchSetupViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        SwitchButton dictionaryButton,
        SwitchButton difficultyButton,
        CommonButton goButton,
        TransientMessageView messageView) {
        gameTimeButton.setInputText(DEFAULT_GAME_TIME_MINUTES);
        stepTimeButton.setInputText(DEFAULT_STEP_TIME_SECONDS);
        dictionaryButton.setCurrentValue(dictionaries[dictionaryIndex]);
        difficultyButton.setCurrentValue(difficulties[difficultyIndex]);

        dictionaryButton.setOnSwitchAction(this::rotateDictionary);
        difficultyButton.setOnSwitchAction(this::rotateDifficulty);
        goButton.setOnAction(event -> navigateToGame(gameTimeButton, stepTimeButton, messageView));
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    public boolean validateGameSetup(
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        TransientMessageView messageView) {
        String gameTimeInput = gameTimeButton.getTextField().getText();
        String stepTimeInput = stepTimeButton.getTextField().getText();
        if (!isValidGameTimeInput(gameTimeInput)) {
            messageView.showMessage(INVALID_GAME_TIME_MESSAGE);
            return false;
        }
        if (!isValidStepTimeInput(stepTimeInput)) {
            messageView.showMessage(INVALID_STEP_TIME_MESSAGE);
            return false;
        }
        return true;
    }

    public void navigateToGame(InputButton gameTimeButton, InputButton stepTimeButton) {
        String gameTimeInput = gameTimeButton.getTextField().getText();
        String stepTimeInput = stepTimeButton.getTextField().getText();
        audioManager.playActionConfirm();
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showGame(buildLaunchContext(gameTimeInput, stepTimeInput));
    }

    private String rotateDictionary() {
        dictionaryIndex = (dictionaryIndex + 1) % dictionaries.length;
        return dictionaries[dictionaryIndex];
    }

    private String rotateDifficulty() {
        difficultyIndex = (difficultyIndex + 1) % difficulties.length;
        return difficulties[difficultyIndex];
    }

    private GameLaunchContext buildLaunchContext(String gameTimeInput, String stepTimeInput) {
        return GameLaunchContext.forLocalAi(
            gameTimeInput,
            stepTimeInput,
            dictionaries[dictionaryIndex],
            difficulties[difficultyIndex]);
    }

    private void navigateToGame(
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        TransientMessageView messageView) {
        if (!validateGameSetup(gameTimeButton, stepTimeButton, messageView)) {
            return;
        }
        navigateToGame(gameTimeButton, stepTimeButton);
    }

    private boolean isValidGameTimeInput(String rawInput) {
        return isValidIntegerInRange(rawInput, 15, 90);
    }

    private boolean isValidStepTimeInput(String rawInput) {
        return isValidIntegerInRange(rawInput, 0, 180);
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
