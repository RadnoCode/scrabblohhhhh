package com.kotva.lan;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Snapshot of the current LAN lobby state.
 */
public class LanLobbySnapshot implements Serializable {
    private final String lobbyId;
    private final LanLobbyPhase phase;
    private final String hostPlayerId;
    private final LanLobbySettings settings;
    private final List<LanLobbyPlayerSnapshot> players;
    private final boolean canStart;

    /**
     * Creates a LAN lobby snapshot.
     *
     * @param lobbyId lobby id
     * @param phase lobby phase
     * @param hostPlayerId host player id
     * @param settings lobby settings
     * @param players players currently in the lobby
     * @param canStart whether the host can start the game
     */
    public LanLobbySnapshot(
            String lobbyId,
            LanLobbyPhase phase,
            String hostPlayerId,
            LanLobbySettings settings,
            List<LanLobbyPlayerSnapshot> players,
            boolean canStart) {
        this.lobbyId = Objects.requireNonNull(lobbyId, "lobbyId cannot be null.");
        this.phase = Objects.requireNonNull(phase, "phase cannot be null.");
        this.hostPlayerId = Objects.requireNonNull(hostPlayerId, "hostPlayerId cannot be null.");
        this.settings = Objects.requireNonNull(settings, "settings cannot be null.");
        this.players = List.copyOf(Objects.requireNonNull(players, "players cannot be null."));
        this.canStart = canStart;
    }

    /**
     * Gets the lobby id.
     *
     * @return lobby id
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Gets the lobby phase.
     *
     * @return lobby phase
     */
    public LanLobbyPhase getPhase() {
        return phase;
    }

    /**
     * Gets the host player id.
     *
     * @return host player id
     */
    public String getHostPlayerId() {
        return hostPlayerId;
    }

    /**
     * Gets lobby settings.
     *
     * @return lobby settings
     */
    public LanLobbySettings getSettings() {
        return settings;
    }

    /**
     * Gets lobby players.
     *
     * @return player snapshots
     */
    public List<LanLobbyPlayerSnapshot> getPlayers() {
        return players;
    }

    /**
     * Checks whether the game can be started.
     *
     * @return {@code true} if start is allowed
     */
    public boolean canStart() {
        return canStart;
    }

    /**
     * Gets current player count.
     *
     * @return player count
     */
    public int getCurrentPlayerCount() {
        return players.size();
    }
}
