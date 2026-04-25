package com.kotva.application.setup;

import com.kotva.application.session.TimeControlConfig;
import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.GameRuleset;
import java.util.List;

public class NewGameRequest {
    private final GameMode gameMode;
    private final int playerCount;
    private final List<String> playerNames;
    private final DictionaryType dictionaryType;
    private final TimeControlConfig timeControlConfig;
    private final AiDifficulty aiDifficulty;
    private final GameRuleset ruleset;
    private final Integer targetScore;

    public NewGameRequest(
        GameMode gameMode,
        int playerCount,
        List<String> playerNames,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig) {
        this(gameMode, playerCount, playerNames, dictionaryType, timeControlConfig, null);
    }

    public NewGameRequest(
        GameMode gameMode,
        int playerCount,
        List<String> playerNames,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig,
        GameRuleset ruleset,
        Integer targetScore) {
        this(
            gameMode,
            playerCount,
            playerNames,
            dictionaryType,
            timeControlConfig,
            null,
            ruleset,
            targetScore);
    }

    public NewGameRequest(
        GameMode gameMode,
        int playerCount,
        List<String> playerNames,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig,
        AiDifficulty aiDifficulty) {
        this(
            gameMode,
            playerCount,
            playerNames,
            dictionaryType,
            timeControlConfig,
            aiDifficulty,
            GameRuleset.TRADITIONAL_SCRABBLE,
            null);
    }

    public NewGameRequest(
        GameMode gameMode,
        int playerCount,
        List<String> playerNames,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig,
        AiDifficulty aiDifficulty,
        GameRuleset ruleset,
        Integer targetScore) {
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.playerNames = playerNames == null ? List.of() : List.copyOf(playerNames);
        this.dictionaryType = dictionaryType;
        this.timeControlConfig = timeControlConfig;
        this.aiDifficulty = aiDifficulty;
        this.ruleset = ruleset == null ? GameRuleset.TRADITIONAL_SCRABBLE : ruleset;
        this.targetScore = targetScore;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public DictionaryType getDictionaryType() {
        return dictionaryType;
    }

    public TimeControlConfig getTimeControlConfig() {
        return timeControlConfig;
    }

    public AiDifficulty getAiDifficulty() {
        return aiDifficulty;
    }

    public GameRuleset getRuleset() {
        return ruleset;
    }

    public Integer getTargetScore() {
        return targetScore;
    }
}
