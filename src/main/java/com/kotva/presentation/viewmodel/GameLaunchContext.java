package com.kotva.presentation.viewmodel;

import com.kotva.application.runtime.GameRuntime;
import com.kotva.application.runtime.LanLaunchConfig;
import com.kotva.application.runtime.RuntimeLaunchSpec;
import com.kotva.application.session.TimeControlConfig;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GameLaunchContext bridges setup-page selections into the game page.
 * It keeps the backend request and the UI-facing labels together so the game
 * page can both start a real session and prepare future rendering.
 */
public class GameLaunchContext {
    private static final long DEFAULT_STEP_TIME_MILLIS = 30_000L;

    private final RuntimeLaunchSpec launchSpec;
    private final GameRuntime providedRuntime;
    private final String modeLabel;
    private final String gameTimeLabel;
    private final String languageLabel;
    private final String playerCountLabel;
    private final String difficultyLabel;
    private final AiDifficulty aiDifficulty;

    public GameLaunchContext(
            RuntimeLaunchSpec launchSpec,
            GameRuntime providedRuntime,
            String modeLabel,
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel,
            String difficultyLabel,
            AiDifficulty aiDifficulty) {
        if (launchSpec == null && providedRuntime == null) {
            throw new IllegalArgumentException(
                    "Either launchSpec or providedRuntime must be supplied.");
        }
        this.launchSpec = launchSpec;
        this.providedRuntime = providedRuntime;
        this.modeLabel = Objects.requireNonNull(modeLabel, "modeLabel cannot be null.");
        this.gameTimeLabel = Objects.requireNonNull(gameTimeLabel, "gameTimeLabel cannot be null.");
        this.languageLabel = Objects.requireNonNull(languageLabel, "languageLabel cannot be null.");
        this.playerCountLabel = Objects.requireNonNull(playerCountLabel, "playerCountLabel cannot be null.");
        this.difficultyLabel = Objects.requireNonNull(difficultyLabel, "difficultyLabel cannot be null.");
        this.aiDifficulty = aiDifficulty;
    }

    public static GameLaunchContext defaultContext() {
        return forLocalMultiplayer("15min", "American", "4");
    }

    public static GameLaunchContext forLocalMultiplayer(String gameTimeLabel, String languageLabel, String playerCountLabel) {
        int playerCount = Integer.parseInt(playerCountLabel);
        List<String> playerNames = buildSequentialNames("Player ", playerCount);
        return new GameLaunchContext(
                RuntimeLaunchSpec.forLocal(
                        new NewGameRequest(
                                GameMode.HOT_SEAT,
                                playerCount,
                                playerNames,
                                mapDictionaryType(languageLabel),
                                mapTimeControl(gameTimeLabel))),
                null,
                "Local Multiplayer",
                gameTimeLabel,
                languageLabel,
                playerCountLabel,
                "--",
                null);
    }

    public static GameLaunchContext forLocalAi(String gameTimeLabel, String languageLabel, String difficultyLabel) {
        AiDifficulty aiDifficulty = AiDifficulty.fromSetupLabel(difficultyLabel);
        List<String> playerNames = List.of("Player", difficultyLabel + " Bot");
        return new GameLaunchContext(
                RuntimeLaunchSpec.forLocal(
                        new NewGameRequest(
                                GameMode.HUMAN_VS_AI,
                                2,
                                playerNames,
                                mapDictionaryType(languageLabel),
                                mapTimeControl(gameTimeLabel),
                                aiDifficulty)),
                null,
                "Local AI",
                gameTimeLabel,
                languageLabel,
                "2",
                aiDifficulty.getSetupLabel(),
                aiDifficulty);
    }

    public static GameLaunchContext forRoomCreate(String gameTimeLabel, String languageLabel, String playerCountLabel) {
        int playerCount = Integer.parseInt(playerCountLabel);
        List<String> playerNames = new ArrayList<>();
        playerNames.add("Host");
        for (int index = 1; index < playerCount; index++) {
            playerNames.add("Guest " + index);
        }

        return new GameLaunchContext(
                RuntimeLaunchSpec.forLanHost(
                        new NewGameRequest(
                                GameMode.LAN_MULTIPLAYER,
                                playerCount,
                                playerNames,
                                mapDictionaryType(languageLabel),
                                mapTimeControl(gameTimeLabel))),
                null,
                "Create Room",
                gameTimeLabel,
                languageLabel,
                playerCountLabel,
                "--",
                null);
    }

    public static GameLaunchContext forLanClient(
            LanLaunchConfig lanLaunchConfig,
            String modeLabel,
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel) {
        Objects.requireNonNull(lanLaunchConfig, "lanLaunchConfig cannot be null.");
        return new GameLaunchContext(
                RuntimeLaunchSpec.forLanClient(lanLaunchConfig),
                null,
                modeLabel,
                gameTimeLabel,
                languageLabel,
                playerCountLabel,
                "--",
                null);
    }

    public static GameLaunchContext forProvidedRuntime(
            GameRuntime providedRuntime,
            String modeLabel,
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel) {
        return new GameLaunchContext(
                null,
                Objects.requireNonNull(providedRuntime, "providedRuntime cannot be null."),
                modeLabel,
                gameTimeLabel,
                languageLabel,
                playerCountLabel,
                "--",
                null);
    }

    public RuntimeLaunchSpec getLaunchSpec() {
        return launchSpec;
    }

    public boolean hasProvidedRuntime() {
        return providedRuntime != null;
    }

    public GameRuntime requireProvidedRuntime() {
        return Objects.requireNonNull(providedRuntime, "providedRuntime cannot be null.");
    }

    public NewGameRequest getRequest() {
        return launchSpec != null && launchSpec.hasRequest() ? launchSpec.requireRequest() : null;
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
        int minutes = switch (gameTimeLabel) {
            case "30min" -> 30;
            case "45min" -> 45;
            default -> 15;
        };

        return new TimeControlConfig(minutes * 60L * 1000L, DEFAULT_STEP_TIME_MILLIS);
    }

    private static List<String> buildSequentialNames(String prefix, int count) {
        List<String> names = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            names.add(prefix + index);
        }
        return names;
    }
}
