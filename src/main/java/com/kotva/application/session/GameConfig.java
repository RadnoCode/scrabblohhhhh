package com.kotva.application.session;

import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class GameConfig implements Serializable {
    private final GameMode gameMode;
    private final List<PlayerConfig> players;
    private final DictionaryType dictionaryType;
    private final TimeControlConfig timeControlConfig;
    private final AiDifficulty aiDifficulty;

    public GameConfig(
        GameMode gameMode,
        List<PlayerConfig> players,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig) {
        this(gameMode, players, dictionaryType, timeControlConfig, null);
    }

    public GameConfig(
        GameMode gameMode,
        List<PlayerConfig> players,
        DictionaryType dictionaryType,
        TimeControlConfig timeControlConfig,
        AiDifficulty aiDifficulty) {
        this.gameMode = Objects.requireNonNull(gameMode, "gameMode cannot be null.");
        this.players = List.copyOf(Objects.requireNonNull(players, "players cannot be null."));
        this.dictionaryType = Objects.requireNonNull(dictionaryType, "dictionaryType cannot be null.");
        this.timeControlConfig = timeControlConfig;
        this.aiDifficulty = aiDifficulty;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public List<PlayerConfig> getPlayers() {
        return players;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public DictionaryType getDictionaryType() {
        return dictionaryType;
    }

    public TimeControlConfig getTimeControlConfig() {
        return timeControlConfig;
    }

    public boolean hasTimeControl() {
        return timeControlConfig != null;
    }

    public AiDifficulty getAiDifficulty() {
        return aiDifficulty;
    }
}
