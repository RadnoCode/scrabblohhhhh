package com.kotva.presentation.viewmodel;

import com.kotva.application.session.TimeControlConfig;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameLaunchContext {
    private static final long DEFAULT_STEP_TIME_MILLIS = 30_000L;

    private final NewGameRequest request;
    private final String modeLabel;
    private final String gameTimeLabel;
    private final String languageLabel;
    private final String playerCountLabel;
    private final String difficultyLabel;
    private final AiDifficulty aiDifficulty;

    public GameLaunchContext(
        NewGameRequest request,
        String modeLabel,
        String gameTimeLabel,
        String languageLabel,
        String playerCountLabel,
        String difficultyLabel,
        AiDifficulty aiDifficulty) {
        this.request = Objects.requireNonNull(request, "request cannot be null.");
        this.modeLabel = Objects.requireNonNull(modeLabel, "modeLabel cannot be null.");
        this.gameTimeLabel = Objects.requireNonNull(gameTimeLabel, "gameTimeLabel cannot be null.");
        this.languageLabel = Objects.requireNonNull(languageLabel, "languageLabel cannot be null.");
        this.playerCountLabel = Objects.requireNonNull(playerCountLabel, "playerCountLabel cannot be null.");
        this.difficultyLabel = Objects.requireNonNull(difficultyLabel, "difficultyLabel cannot be null.");
        this.aiDifficulty = aiDifficulty;
    }

    public static GameLaunchContext defaultContext() {
        return forLocalMultiplayer("15min", "North American", "4");
    }

    public static GameLaunchContext forLocalMultiplayer(
        String gameTimeLabel, String languageLabel, String playerCountLabel) {
        int playerCount = Integer.parseInt(playerCountLabel);
        List<String> playerNames = buildSequentialNames("Player ", playerCount);
        return new GameLaunchContext(
            new NewGameRequest(
            GameMode.HOT_SEAT,
            playerCount,
            playerNames,
            mapDictionaryType(languageLabel),
            mapTimeControl(gameTimeLabel)),
            "Local Multiplayer",
            gameTimeLabel,
            languageLabel,
            playerCountLabel,
            "--",
            null);
    }

    public static GameLaunchContext forLocalAi(
        String gameTimeLabel, String languageLabel, String difficultyLabel) {
        AiDifficulty aiDifficulty = AiDifficulty.fromSetupLabel(difficultyLabel);
        List<String> playerNames = List.of("Player", difficultyLabel + " Bot");
        return new GameLaunchContext(
            new NewGameRequest(
            GameMode.HUMAN_VS_AI,
            2,
            playerNames,
            mapDictionaryType(languageLabel),
            mapTimeControl(gameTimeLabel),
            aiDifficulty),
            "Local AI",
            gameTimeLabel,
            languageLabel,
            "2",
            aiDifficulty.getSetupLabel(),
            aiDifficulty);
    }

    public static GameLaunchContext forRoomCreate(
        String gameTimeLabel, String languageLabel, String playerCountLabel) {
        int playerCount = Integer.parseInt(playerCountLabel);
        List<String> playerNames = new ArrayList<>();
        playerNames.add("Host");
        for (int index = 1; index < playerCount; index++) {
            playerNames.add("Guest " + index);
        }

        return new GameLaunchContext(
            new NewGameRequest(
            GameMode.LAN_MULTIPLAYER,
            playerCount,
            playerNames,
            mapDictionaryType(languageLabel),
            mapTimeControl(gameTimeLabel)),
            "Create Room",
            gameTimeLabel,
            languageLabel,
            playerCountLabel,
            "--",
            null);
    }

    public NewGameRequest getRequest() {
        return request;
    }

    public String getModeLabel() {
        return modeLabel;
    }

    public String getGameTimeLabel() {
        return gameTimeLabel;
    }

    public String getLanguageLabel() {
        return languageLabel;
    }

    public String getPlayerCountLabel() {
        return playerCountLabel;
    }

    public String getDifficultyLabel() {
        return difficultyLabel;
    }

    public AiDifficulty getAiDifficulty() {
        return aiDifficulty;
    }

    private static DictionaryType mapDictionaryType(String languageLabel) {
        return "British".equalsIgnoreCase(languageLabel) ? DictionaryType.BR : DictionaryType.AM;
    }

    private static TimeControlConfig mapTimeControl(String gameTimeLabel) {
        int minutes = parseGameTimeMinutes(gameTimeLabel);
        return new TimeControlConfig(minutes * 60L * 1000L, DEFAULT_STEP_TIME_MILLIS);
    }

    private static int parseGameTimeMinutes(String gameTimeLabel) {
        if (gameTimeLabel == null) {
            return 15;
        }

        String digitsOnly = gameTimeLabel.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return 15;
        }

        try {
            int minutes = Integer.parseInt(digitsOnly);
            return minutes > 0 ? minutes : 15;
        } catch (NumberFormatException exception) {
            return 15;
        }
    }

    private static List<String> buildSequentialNames(String prefix, int count) {
        List<String> names = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            names.add(prefix + index);
        }
        return names;
    }
}