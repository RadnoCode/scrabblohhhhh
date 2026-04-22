package com.kotva.presentation.controller;

import com.kotva.infrastructure.AudioManager;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.fx.PlayerNameSetupContext;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;

public class LocalMultiplayerSetupController {
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
    private final String[] playerCounts = {"2", "3", "4"};
    private int dictionaryIndex;
    private int playerCountIndex;

    public LocalMultiplayerSetupController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.audioManager = navigator.getAppContext().getAudioManager();
        this.viewModel = new GameBranchSetupViewModel(
            "SCRABBLE",
            "Play With Friends",
            "Select Game Time",
            "Dictionary",
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
        dictionaryButton.setCurrentValue(dictionaries[dictionaryIndex]);
        playerCountButton.setCurrentValue(playerCounts[playerCountIndex]);

        dictionaryButton.setOnSwitchAction(this::rotateDictionary);
        playerCountButton.setOnSwitchAction(this::rotatePlayerCount);
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

    public void navigateToPlayerNameSetup(InputButton gameTimeButton, InputButton stepTimeButton) {
        String gameTimeInput = gameTimeButton.getTextField().getText();
        String stepTimeInput = stepTimeButton.getTextField().getText();
        audioManager.playActionConfirm();
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showPlayerNameSetup(buildPlayerNameSetupContext(gameTimeInput, stepTimeInput));
    }

    private String rotateDictionary() {
        dictionaryIndex = (dictionaryIndex + 1) % dictionaries.length;
        return dictionaries[dictionaryIndex];
    }

    private String rotatePlayerCount() {
        playerCountIndex = (playerCountIndex + 1) % playerCounts.length;
        return playerCounts[playerCountIndex];
    }

    private PlayerNameSetupContext buildPlayerNameSetupContext(
        String gameTimeInput,
        String stepTimeInput) {
        return PlayerNameSetupContext.forHotSeat(
            gameTimeInput,
            stepTimeInput,
            dictionaries[dictionaryIndex],
            playerCounts[playerCountIndex]);
    }

    private void navigateToGame(
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        TransientMessageView messageView) {
        if (!validateGameSetup(gameTimeButton, stepTimeButton, messageView)) {
            return;
        }
        navigateToPlayerNameSetup(gameTimeButton, stepTimeButton);
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
