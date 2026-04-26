package com.kotva.application.setup;

import com.kotva.application.session.TimeControlConfig;
import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.GameRuleset;
import java.util.List;

/**
 * Carries the options selected before starting a new game.
 */
public class NewGameRequest {
    private final GameMode gameMode;
    private final int playerCount;
    private final List<String> playerNames;
    private final DictionaryType dictionaryType;
    private final TimeControlConfig timeControlConfig;
    private final AiDifficulty aiDifficulty;
    private final GameRuleset ruleset;
    private final Integer targetScore;

    /**
     * Creates a new game request with default rules.
     *
     * @param gameMode selected game mode
     * @param playerCount number of players
     * @param playerNames player names from setup
     * @param dictionaryType dictionary to use
     * @param timeControlConfig time control settings
     */
    public NewGameRequest(
        GameMode gameMode,
        int playerCount,
        List<String> playerNames,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig) {
        this(gameMode, playerCount, playerNames, dictionaryType, timeControlConfig, null);
    }

    /**
     * Creates a new game request with a ruleset and optional target score.
     *
     * @param gameMode selected game mode
     * @param playerCount number of players
     * @param playerNames player names from setup
     * @param dictionaryType dictionary to use
     * @param timeControlConfig time control settings
     * @param ruleset game ruleset
     * @param targetScore target score, or {@code null} when not used
     */
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

    /**
     * Creates a new game request with AI difficulty.
     *
     * @param gameMode selected game mode
     * @param playerCount number of players
     * @param playerNames player names from setup
     * @param dictionaryType dictionary to use
     * @param timeControlConfig time control settings
     * @param aiDifficulty AI difficulty, or {@code null} for non-AI games
     */
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

    /**
     * Creates a complete new game request.
     *
     * @param gameMode selected game mode
     * @param playerCount number of players
     * @param playerNames player names from setup
     * @param dictionaryType dictionary to use
     * @param timeControlConfig time control settings
     * @param aiDifficulty AI difficulty, or {@code null} for non-AI games
     * @param ruleset game ruleset
     * @param targetScore target score, or {@code null} when not used
     */
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

    /**
     * Gets the selected game mode.
     *
     * @return game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Gets the number of players.
     *
     * @return player count
     */
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Gets the player names.
     *
     * @return player name list
     */
    public List<String> getPlayerNames() {
        return playerNames;
    }

    /**
     * Gets the selected dictionary.
     *
     * @return dictionary type
     */
    public DictionaryType getDictionaryType() {
        return dictionaryType;
    }

    /**
     * Gets the time control settings.
     *
     * @return time control config
     */
    public TimeControlConfig getTimeControlConfig() {
        return timeControlConfig;
    }

    /**
     * Gets the selected AI difficulty.
     *
     * @return AI difficulty, or {@code null} if not selected
     */
    public AiDifficulty getAiDifficulty() {
        return aiDifficulty;
    }

    /**
     * Gets the selected ruleset.
     *
     * @return ruleset
     */
    public GameRuleset getRuleset() {
        return ruleset;
    }

    /**
     * Gets the target score for score-based games.
     *
     * @return target score, or {@code null} when not used
     */
    public Integer getTargetScore() {
        return targetScore;
    }
}
