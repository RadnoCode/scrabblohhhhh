package com.kotva.presentation.controller;

import com.kotva.infrastructure.AudioManager;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.infrastructure.save.SaveGameArchive;
import com.kotva.infrastructure.save.SaveGameRepository;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.fx.PlayerNameSetupContext;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import com.kotva.runtime.GameRuntime;

public class LocalMultiplayerSetupController {
    private static final String DEFAULT_GAME_TIME_MINUTES = "15";
    private static final String DEFAULT_STEP_TIME_SECONDS = "30";
    private static final String DEFAULT_TARGET_SCORE = "100";
    private static final String INVALID_GAME_TIME_MESSAGE =
        "Please enter an integer between 15 and 90 minutes.";
    private static final String INVALID_STEP_TIME_MESSAGE =
        "Please enter an integer between 0 and 180 seconds.";
    private static final String INVALID_TARGET_SCORE_MESSAGE =
        "Please enter a target score between 1 and 9999.";

    private final SceneNavigator navigator;
    private final GameBranchSetupViewModel viewModel;
    private final AudioManager audioManager;
    private final SaveGameRepository saveGameRepository;
    private final String[] rulesets = {"Traditional Scrabble", "Scribble"};
    private final String[] dictionaries = {"North American", "British"};
    private final String[] playerCounts = {"2", "3", "4"};
    private int rulesetIndex;
    private int dictionaryIndex;
    private int playerCountIndex;

    public LocalMultiplayerSetupController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.audioManager = navigator.getAppContext().getAudioManager();
        this.saveGameRepository = navigator.getAppContext().getSaveGameRepository();
        this.viewModel = new GameBranchSetupViewModel(
            "SCRABBLE",
            "Play With Friends",
            "Enter Game Time",
            "Dictionary",
            "Number of Player");
    }

    public GameBranchSetupViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        SwitchButton rulesetButton,
        InputButton targetScoreButton,
        SwitchButton dictionaryButton,
        SwitchButton playerCountButton,
        CommonButton goButton,
        CommonButton loadButton,
        TransientMessageView messageView) {
        gameTimeButton.setInputText(DEFAULT_GAME_TIME_MINUTES);
        stepTimeButton.setInputText(DEFAULT_STEP_TIME_SECONDS);
        targetScoreButton.setInputText(DEFAULT_TARGET_SCORE);
        rulesetButton.setCurrentValue(rulesets[rulesetIndex]);
        dictionaryButton.setCurrentValue(dictionaries[dictionaryIndex]);
        playerCountButton.setCurrentValue(playerCounts[playerCountIndex]);
        syncTargetScoreVisibility(targetScoreButton);

        rulesetButton.setOnSwitchAction(() -> rotateRuleset(targetScoreButton));
        dictionaryButton.setOnSwitchAction(this::rotateDictionary);
        playerCountButton.setOnSwitchAction(this::rotatePlayerCount);
        goButton.setOnAction(event ->
            navigateToGame(gameTimeButton, stepTimeButton, targetScoreButton, messageView));
        loadButton.setOnAction(event -> navigateToSavedGame(messageView));
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    public boolean validateGameSetup(
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        InputButton targetScoreButton,
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
        if (isCurrentRulesetScribble()
            && !isValidTargetScoreInput(targetScoreButton.getTextField().getText())) {
            messageView.showMessage(INVALID_TARGET_SCORE_MESSAGE);
            return false;
        }
        return true;
    }

    public void navigateToPlayerNameSetup(
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        InputButton targetScoreButton) {
        String gameTimeInput = gameTimeButton.getTextField().getText();
        String stepTimeInput = stepTimeButton.getTextField().getText();
        String targetScoreInput = targetScoreButton.getTextField().getText();
        audioManager.playActionConfirm();
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showPlayerNameSetup(
            buildPlayerNameSetupContext(gameTimeInput, stepTimeInput, targetScoreInput));
    }

    public void navigateToSavedGame(TransientMessageView messageView) {
        try {
            SaveGameArchive archive = saveGameRepository.loadHotSeat();
            GameSession session = archive.getSession();
            GameRuntime runtime =
                navigator.getAppContext().getGameRuntimeFactory().createHotSeatFromSave(session);
            GameConfig config = session.getConfig();
            audioManager.playActionConfirm();
            navigator.requestNextSceneTitleEntranceAnimation();
            navigator.showGame(GameLaunchContext.forProvidedRuntime(
                runtime,
                resolveModeLabel(config),
                resolveGameTimeLabel(config),
                resolveLanguageLabel(config),
                Integer.toString(config.getPlayerCount())));
        } catch (RuntimeException exception) {
            messageView.showMessage(exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Unable to load saved game."
                : exception.getMessage());
        }
    }

    private String rotateDictionary() {
        dictionaryIndex = (dictionaryIndex + 1) % dictionaries.length;
        return dictionaries[dictionaryIndex];
    }

    private String rotateRuleset(InputButton targetScoreButton) {
        rulesetIndex = (rulesetIndex + 1) % rulesets.length;
        syncTargetScoreVisibility(targetScoreButton);
        return rulesets[rulesetIndex];
    }

    private String rotatePlayerCount() {
        playerCountIndex = (playerCountIndex + 1) % playerCounts.length;
        return playerCounts[playerCountIndex];
    }

    private PlayerNameSetupContext buildPlayerNameSetupContext(
        String gameTimeInput,
        String stepTimeInput,
        String targetScoreInput) {
        return PlayerNameSetupContext.forHotSeat(
            gameTimeInput,
            stepTimeInput,
            dictionaries[dictionaryIndex],
            playerCounts[playerCountIndex],
            rulesets[rulesetIndex],
            isCurrentRulesetScribble() ? targetScoreInput : null);
    }

    private String resolveModeLabel(GameConfig config) {
        return config.isScribbleRuleset() ? "Scribble" : "Local Multiplayer";
    }

    private String resolveGameTimeLabel(GameConfig config) {
        if (config.getTimeControlConfig() == null) {
            return "--";
        }
        return Long.toString(config.getTimeControlConfig().getMainTimeMillis() / 60_000L);
    }

    private String resolveLanguageLabel(GameConfig config) {
        return switch (config.getDictionaryType()) {
        case BR -> "British";
        case AM -> "North American";
        };
    }

    private void navigateToGame(
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        InputButton targetScoreButton,
        TransientMessageView messageView) {
        if (!validateGameSetup(gameTimeButton, stepTimeButton, targetScoreButton, messageView)) {
            return;
        }
        navigateToPlayerNameSetup(gameTimeButton, stepTimeButton, targetScoreButton);
    }

    private boolean isValidGameTimeInput(String rawInput) {
        return isValidIntegerInRange(rawInput, 15, 90);
    }

    private boolean isValidStepTimeInput(String rawInput) {
        return isValidIntegerInRange(rawInput, 0, 180);
    }

    private boolean isValidTargetScoreInput(String rawInput) {
        return isValidIntegerInRange(rawInput, 1, 9999);
    }

    private boolean isCurrentRulesetScribble() {
        return "Scribble".equals(rulesets[rulesetIndex]);
    }

    private void syncTargetScoreVisibility(InputButton targetScoreButton) {
        boolean visible = isCurrentRulesetScribble();
        targetScoreButton.setVisible(visible);
        targetScoreButton.setManaged(visible);
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
