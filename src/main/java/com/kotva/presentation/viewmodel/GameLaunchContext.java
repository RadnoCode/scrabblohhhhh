package com.kotva.presentation.viewmodel;

import com.kotva.application.runtime.GameRuntime;
import com.kotva.application.runtime.LanLaunchConfig;
import com.kotva.application.runtime.RuntimeLaunchSpec;
import com.kotva.application.session.TimeControlConfig;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import com.kotva.tutorial.TutorialScriptId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameLaunchContext {
    private static final long DEFAULT_STEP_TIME_MILLIS = 30_000L;

    private final LaunchKind launchKind;
    private final RuntimeLaunchSpec launchSpec;
    private final GameRuntime providedRuntime;
    private final NewGameRequest request;
    private final TutorialScriptId tutorialScriptId;
    private final String modeLabel;
    private final String gameTimeLabel;
    private final String languageLabel;
    private final String playerCountLabel;
    private final String difficultyLabel;
    private final AiDifficulty aiDifficulty;

    public GameLaunchContext(
        LaunchKind launchKind,
        RuntimeLaunchSpec launchSpec,
        GameRuntime providedRuntime,
        NewGameRequest request,
        TutorialScriptId tutorialScriptId,
        String modeLabel,
        String gameTimeLabel,
        String languageLabel,
        String playerCountLabel,
        String difficultyLabel,
        AiDifficulty aiDifficulty) {
        this.launchKind = Objects.requireNonNull(launchKind, "launchKind cannot be null.");
        if (launchKind == LaunchKind.STANDARD_GAME
            && request == null
            && launchSpec == null
            && providedRuntime == null) {
            throw new IllegalArgumentException(
                "Standard game launches require a request, launchSpec, or providedRuntime.");
        }
        if (launchKind == LaunchKind.TUTORIAL && tutorialScriptId == null) {
            throw new IllegalArgumentException(
                "tutorialScriptId cannot be null for tutorial launches.");
        }
        this.launchSpec = launchSpec;
        this.providedRuntime = providedRuntime;
        this.request = request;
        this.tutorialScriptId = tutorialScriptId;
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
        NewGameRequest request = new NewGameRequest(
            GameMode.HOT_SEAT,
            playerCount,
            playerNames,
            mapDictionaryType(languageLabel),
            mapTimeControl(gameTimeLabel));
        return new GameLaunchContext(
            LaunchKind.STANDARD_GAME,
            RuntimeLaunchSpec.forLocal(request),
            null,
            request,
            null,
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
        NewGameRequest request = new NewGameRequest(
            GameMode.HUMAN_VS_AI,
            2,
            playerNames,
            mapDictionaryType(languageLabel),
            mapTimeControl(gameTimeLabel),
            aiDifficulty);
        return new GameLaunchContext(
            LaunchKind.STANDARD_GAME,
            RuntimeLaunchSpec.forLocal(request),
            null,
            request,
            null,
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
        NewGameRequest request = new NewGameRequest(
            GameMode.LAN_MULTIPLAYER,
            playerCount,
            playerNames,
            mapDictionaryType(languageLabel),
            mapTimeControl(gameTimeLabel));

        return new GameLaunchContext(
            LaunchKind.STANDARD_GAME,
            RuntimeLaunchSpec.forLanHost(request),
            null,
            request,
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
            LaunchKind.STANDARD_GAME,
            RuntimeLaunchSpec.forLanClient(lanLaunchConfig),
            null,
            null,
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
            LaunchKind.STANDARD_GAME,
            null,
            Objects.requireNonNull(providedRuntime, "providedRuntime cannot be null."),
            null,
            null,
            modeLabel,
            gameTimeLabel,
            languageLabel,
            playerCountLabel,
            "--",
            null);
    }

    public static GameLaunchContext forTutorial(TutorialScriptId tutorialScriptId) {
        return new GameLaunchContext(
            LaunchKind.TUTORIAL,
            null,
            null,
            null,
            Objects.requireNonNull(tutorialScriptId, "tutorialScriptId cannot be null."),
            "Tutorial",
            "--",
            "North American",
            "1",
            "--",
            null);
    }

    public LaunchKind getLaunchKind() {
        return launchKind;
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
        return request;
    }

    public TutorialScriptId getTutorialScriptId() {
        return tutorialScriptId;
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
