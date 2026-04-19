package com.kotva.lan;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * represents a game session in the LAN version of the game. It manages the session's state, including the host player, current players, and their connections.
 */
public class LocalGameSession {

    private final String sessionId;
    private final String hostPlayerId;
    private final String hostPlayerName;
    private final int maxPlayers;

    /**
     * playerId -> playerName
     * Uses ConcurrentHashMap to ensure thread-safe operations for adding, removing, and querying players.
     */
    private final Map<String, String> players = new ConcurrentHashMap<>();

    /**
     * playerId -> ClientConnection
     * The host iterates over this map when broadcasting messages.
     */
    private final Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    // Constructor: when a new session is created, the host player automatically joins the session.
    public LocalGameSession(String hostPlayerId, String hostPlayerName, int maxPlayers) {
        this(UUID.randomUUID().toString(), hostPlayerId, hostPlayerName, maxPlayers);
    }

    public LocalGameSession(
            String sessionId,
            String hostPlayerId,
            String hostPlayerName,
            int maxPlayers) {
        this.sessionId = sessionId;
        this.hostPlayerId = hostPlayerId;
        this.hostPlayerName = hostPlayerName;
        this.maxPlayers = maxPlayers;

        // The host player joins the session first
        this.players.put(hostPlayerId, hostPlayerName);
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public String getHostPlayerName() {
        return hostPlayerName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurrentPlayerCount() {
        return players.size();
    }
    
    // if the number of players in the session has reached the maximum allowed, return true. This can be used to prevent new players from joining a full session.
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    // check if a player with the given playerId is already in the session. This can be used to prevent duplicate player IDs when new players try to join.
    public boolean containsPlayer(String playerId) {
        return players.containsKey(playerId);
    }

    /**
     * A new player joins:
     * 1) Add to players
     * 2) Add to connections
     */
    public void addPlayer(String playerId, String playerName, ClientConnection connection) {
        players.put(playerId, playerName);
        connections.put(playerId, connection);
    }

    /**
     * A player leaves:
     * 1) Remove from players
     * 2) Remove from connections
     */
    public void removePlayer(String playerId) {
        players.remove(playerId);
        connections.remove(playerId);
    }

    public Map<String, String> getPlayersReadonly() {
        return Collections.unmodifiableMap(players);
    }

    public List<PlayerBrief> getPlayerBriefs() {
        return players.entrySet().stream()
                .map(e -> new PlayerBrief(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public Map<String, ClientConnection> getConnectionsReadonly() {
        return Collections.unmodifiableMap(connections);
    }

    /**
     * Lightweight player information object, used for initialization messages/lobby messages.
     */
    public record PlayerBrief(String playerId, String playerName) implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
    }
}
