package com.kotva.application.session;

import com.kotva.mode.DictionaryType;
import com.kotva.mode.GameMode;

/**
 * GameConfig class encapsulates the configuration settings for a game session.
 * It includes the game mode, number of players, dictionary type, and whether time limit is enabled.
 */
public class GameConfig {
    private final GameMode gameMode; //game mode: HOT_SEAT, HUMAN_VS_AI, LAN_MULTIPLAYER
    private final int platerCount; //number of players  
    private final DictionaryType dictionaryType; //type of dictionary: EN, AM
    private final boolean isTimeLimitEnabled; //whether time limit is enabled
   
    public GameConfig(GameMode gameMode, int platerCount, DictionaryType dictionaryType, boolean isTimeLimitEnabled) {
        this.gameMode = gameMode;
        this.platerCount = platerCount;
        this.dictionaryType = dictionaryType;
        this.isTimeLimitEnabled = isTimeLimitEnabled;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public int getPlaterCount() {
        return platerCount;
    }

    public DictionaryType getDictionaryType() {
        return dictionaryType;
    }

    public boolean isTimeLimitEnabled() {
        return isTimeLimitEnabled;
    }

}
