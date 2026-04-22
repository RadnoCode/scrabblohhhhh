package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TextInputLimiter;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.fx.PlayerNameSetupContext;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;

/**
 * RoomCreateController handles the create-room page.
 * Its current layout and option structure intentionally match the
 * LocalMultiplayerSetup page.
 */
public class RoomCreateController {
    private static final String DEFAULT_ROOM_NAME = "LAN Room";
    private static final String DEFAULT_GAME_TIME_MINUTES = "15";
    private static final String DEFAULT_STEP_TIME_SECONDS = "30";
    private static final int MAX_ROOM_NAME_CODE_POINTS = 18;
    private static final String INVALID_ROOM_NAME_MESSAGE =
        "Room name cannot be blank.";
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
            InputButton roomNameButton,
            InputButton gameTimeButton,
            InputButton stepTimeButton,
            SwitchButton dictionaryButton,
            SwitchButton playerCountButton,
            CommonButton goButton,
            TransientMessageView messageView) {
        roomNameButton.setInputText(DEFAULT_ROOM_NAME);
        TextInputLimiter.limitCodePoints(roomNameButton.getTextField(), MAX_ROOM_NAME_CODE_POINTS);
        gameTimeButton.setInputText(DEFAULT_GAME_TIME_MINUTES);
        stepTimeButton.setInputText(DEFAULT_STEP_TIME_SECONDS);
        dictionaryButton.setCurrentValue(languages[languageIndex]);
        playerCountButton.setCurrentValue(playerCounts[playerCountIndex]);

        dictionaryButton.setOnSwitchAction(this::rotateLanguage);
        playerCountButton.setOnSwitchAction(this::rotatePlayerCount);
        goButton.setOnAction(
            event -> navigateToPlayerNameSetup(roomNameButton, gameTimeButton, stepTimeButton, messageView));
    }

    public void bindBackAction(CommonButton backButton) {
        backButton.setOnAction(event -> navigator.goBack());
    }

    public PlayerNameSetupContext preparePlayerNameSetupContext(
        InputButton roomNameButton,
        InputButton gameTimeButton,
        InputButton stepTimeButton,
        TransientMessageView messageView) {
        String roomNameInput = roomNameButton.getTextField().getText();
        String gameTimeInput = gameTimeButton.getTextField().getText();
        String stepTimeInput = stepTimeButton.getTextField().getText();
        if (roomNameInput == null || roomNameInput.trim().isBlank()) {
            messageView.showMessage(INVALID_ROOM_NAME_MESSAGE);
            return null;
        }
        if (!isValidIntegerInRange(gameTimeInput, 15, 90)) {
            messageView.showMessage(INVALID_GAME_TIME_MESSAGE);
            return null;
        }
        if (!isValidIntegerInRange(stepTimeInput, 0, 180)) {
            messageView.showMessage(INVALID_STEP_TIME_MESSAGE);
            return null;
        }
        return PlayerNameSetupContext.forLanHost(
            roomNameInput.trim(),
            gameTimeInput,
            stepTimeInput,
            languages[languageIndex],
            playerCounts[playerCountIndex]);
    }

    public void navigateToPlayerNameSetup(PlayerNameSetupContext playerNameSetupContext) {
        navigator.requestNextSceneTitleEntranceAnimation();
        navigator.showPlayerNameSetup(playerNameSetupContext);
    }

    private String rotateLanguage() {
        languageIndex = (languageIndex + 1) % languages.length;
        return languages[languageIndex];
    }

    private String rotatePlayerCount() {
        playerCountIndex = (playerCountIndex + 1) % playerCounts.length;
        return playerCounts[playerCountIndex];
    }

    private void navigateToPlayerNameSetup(
            InputButton roomNameButton,
            InputButton gameTimeButton,
            InputButton stepTimeButton,
            TransientMessageView messageView) {
        PlayerNameSetupContext playerNameSetupContext = preparePlayerNameSetupContext(
            roomNameButton,
            gameTimeButton,
            stepTimeButton,
            messageView);
        if (playerNameSetupContext != null) {
            navigateToPlayerNameSetup(playerNameSetupContext);
        }
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
