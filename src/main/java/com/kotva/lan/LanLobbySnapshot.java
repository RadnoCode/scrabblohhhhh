package com.kotva.lan;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class LanLobbySnapshot implements Serializable {
    private final String lobbyId;
    private final LanLobbyPhase phase;
    private final String hostPlayerId;
    private final LanLobbySettings settings;
    private final List<LanLobbyPlayerSnapshot> players;
    private final boolean canStart;

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

    public String getLobbyId() {
        return lobbyId;
    }

    public LanLobbyPhase getPhase() {
        return phase;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public LanLobbySettings getSettings() {
        return settings;
    }

    public List<LanLobbyPlayerSnapshot> getPlayers() {
        return players;
    }

    public boolean canStart() {
        return canStart;
    }

    public int getCurrentPlayerCount() {
        return players.size();
    }
}
