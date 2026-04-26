package com.kotva.application.session;

import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.GameRuleset;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Final game configuration used to create a game session.
 */
public class GameConfig implements Serializable {
    private final GameMode gameMode;
    private final List<PlayerConfig> players;
    private final DictionaryType dictionaryType;
    private final TimeControlConfig timeControlConfig;
    private final AiDifficulty aiDifficulty;
    private final GameRuleset ruleset;
    private final Integer targetScore;

    /**
     * Creates a game config without AI difficulty.
     *
     * @param gameMode selected game mode
     * @param players player configurations
     * @param dictionaryType dictionary to use
     * @param timeControlConfig time control settings
     */
    public GameConfig(
        GameMode gameMode,
        List<PlayerConfig> players,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig) {
        this(gameMode, players, dictionaryType, timeControlConfig, null);
    }

    /**
     * Creates a game config with AI difficulty.
     *
     * @param gameMode selected game mode
     * @param players player configurations
     * @param dictionaryType dictionary to use
     * @param timeControlConfig time control settings
     * @param aiDifficulty AI difficulty, or {@code null}
     */
    public GameConfig(
        GameMode gameMode,
        List<PlayerConfig> players,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig,
        AiDifficulty aiDifficulty) {
        this(
            gameMode,
            players,
            dictionaryType,
            timeControlConfig,
            aiDifficulty,
            GameRuleset.TRADITIONAL_SCRABBLE,
            null);
    }

    /**
     * Creates a complete game config.
     *
     * @param gameMode selected game mode
     * @param players player configurations
     * @param dictionaryType dictionary to use
     * @param timeControlConfig time control settings
     * @param aiDifficulty AI difficulty, or {@code null}
     * @param ruleset ruleset to use
     * @param targetScore target score, or {@code null} when not used
     */
    public GameConfig(
        GameMode gameMode,
        List<PlayerConfig> players,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig,
        AiDifficulty aiDifficulty,
        GameRuleset ruleset,
        Integer targetScore) {
        this.gameMode = Objects.requireNonNull(gameMode, "gameMode cannot be null.");
        this.players = List.copyOf(Objects.requireNonNull(players, "players cannot be null."));
        this.dictionaryType = Objects.requireNonNull(dictionaryType, "dictionaryType cannot be null.");
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
     * Gets all player configs.
     *
     * @return player config list
     */
    public List<PlayerConfig> getPlayers() {
        return players;
    }

    /**
     * Gets the number of players.
     *
     * @return player count
     */
    public int getPlayerCount() {
        return players.size();
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
     * Gets time control settings.
     *
     * @return time control config, or {@code null}
     */
    public TimeControlConfig getTimeControlConfig() {
        return timeControlConfig;
    }

    /**
     * Checks whether this game has time control.
     *
     * @return {@code true} if a time config exists
     */
    public boolean hasTimeControl() {
        return timeControlConfig != null;
    }

    /**
     * Gets the AI difficulty.
     *
     * @return AI difficulty, or {@code null}
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
     * Checks whether the game uses Scribble rules.
     *
     * @return {@code true} for Scribble rules
     */
    public boolean isScribbleRuleset() {
        return ruleset == GameRuleset.SCRIBBLE;
    }

    /**
     * Checks whether the tile bag should be infinite.
     *
     * @return {@code true} when the ruleset uses an infinite tile bag
     */
    public boolean hasInfiniteTileBag() {
        return isScribbleRuleset();
    }

    /**
     * Gets the target score.
     *
     * @return target score, or {@code null}
     */
    public Integer getTargetScore() {
        return targetScore;
    }
}
