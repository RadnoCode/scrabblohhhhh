package com.kotva.application.session;

import com.kotva.policy.PlayerType;
import java.util.Objects;

public class PlayerConfig {
    private final String playerName;
    private final PlayerType playerType;

    public PlayerConfig(String playerName, PlayerType playerType) {
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
        this.playerType = Objects.requireNonNull(playerType, "playerType cannot be null.");
    }

    public String getPlayerName() {
        return playerName;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }
}
