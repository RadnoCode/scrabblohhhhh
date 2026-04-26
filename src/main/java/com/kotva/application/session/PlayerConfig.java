package com.kotva.application.session;

import com.kotva.policy.PlayerType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Configuration for one player before a game starts.
 */
public class PlayerConfig implements Serializable {
    private final String playerName;
    private final PlayerType playerType;

    /**
     * Creates a player config.
     *
     * @param playerName display name
     * @param playerType human, AI, or network player type
     */
    public PlayerConfig(String playerName, PlayerType playerType) {
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
        this.playerType = Objects.requireNonNull(playerType, "playerType cannot be null.");
    }

    /**
     * Gets the player name.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the player type.
     *
     * @return player type
     */
    public PlayerType getPlayerType() {
        return playerType;
    }
}
