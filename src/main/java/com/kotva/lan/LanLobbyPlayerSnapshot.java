package com.kotva.lan;

import java.io.Serializable;
import java.util.Objects;

/**
 * Snapshot of one player shown in a LAN lobby.
 */
public class LanLobbyPlayerSnapshot implements Serializable {
    private final String playerId;
    private final String playerName;
    private final boolean host;

    /**
     * Creates a lobby player snapshot.
     *
     * @param playerId player id
     * @param playerName player display name
     * @param host whether this player is the host
     */
    public LanLobbyPlayerSnapshot(String playerId, String playerName, boolean host) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
        this.host = host;
    }

    /**
     * Gets the player id.
     *
     * @return player id
     */
    public String getPlayerId() {
        return playerId;
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
     * Checks whether this player is the host.
     *
     * @return {@code true} if host
     */
    public boolean isHost() {
        return host;
    }
}
