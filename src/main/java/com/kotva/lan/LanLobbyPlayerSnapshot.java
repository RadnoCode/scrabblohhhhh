package com.kotva.lan;

import java.io.Serializable;
import java.util.Objects;

public class LanLobbyPlayerSnapshot implements Serializable {
    private final String playerId;
    private final String playerName;
    private final boolean host;

    public LanLobbyPlayerSnapshot(String playerId, String playerName, boolean host) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
        this.host = host;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isHost() {
        return host;
    }
}
