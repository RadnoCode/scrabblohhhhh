package com.kotva.lan;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Stores the lobby-side player list and client connections for a LAN session.
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

    /**
     * Creates a local LAN session with a generated session id.
     *
     * @param hostPlayerId host player id
     * @param hostPlayerName host player name
     * @param maxPlayers maximum number of players
     */
    public LocalGameSession(String hostPlayerId, String hostPlayerName, int maxPlayers) {
        this(UUID.randomUUID().toString(), hostPlayerId, hostPlayerName, maxPlayers);
    }

    /**
     * Creates a local LAN session.
     *
     * @param sessionId session id
     * @param hostPlayerId host player id
     * @param hostPlayerName host player name
     * @param maxPlayers maximum number of players
     */
    public LocalGameSession(
            String sessionId,
            String hostPlayerId,
            String hostPlayerName,
            int maxPlayers) {
        this.sessionId = sessionId;
        this.hostPlayerId = hostPlayerId;
        this.hostPlayerName = hostPlayerName;
        this.maxPlayers = maxPlayers;

        this.players.put(hostPlayerId, hostPlayerName);
    }

    /**
     * Gets the session id.
     *
     * @return session id
     */
    public String getSessionId() {
        return sessionId;
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
     * Gets the host player name.
     *
     * @return host player name
     */
    public String getHostPlayerName() {
        return hostPlayerName;
    }

    /**
     * Gets maximum player count.
     *
     * @return max players
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Gets current player count.
     *
     * @return current player count
     */
    public int getCurrentPlayerCount() {
        return players.size();
    }

    /**
     * Checks whether the session is full.
     *
     * @return {@code true} if full
     */
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    /**
     * Checks whether a player id already exists.
     *
     * @param playerId player id
     * @return {@code true} if the player exists
     */
    public boolean containsPlayer(String playerId) {
        return players.containsKey(playerId);
    }

    /**
     * Adds a player and its client connection.
     *
     * @param playerId player id
     * @param playerName player display name
     * @param connection client connection
     */
    public void addPlayer(String playerId, String playerName, ClientConnection connection) {
        players.put(playerId, playerName);
        connections.put(playerId, connection);
    }

    /**
     * Removes a player and its client connection.
     *
     * @param playerId player id
     */
    public void removePlayer(String playerId) {
        players.remove(playerId);
        connections.remove(playerId);
    }

    /**
     * Gets a read-only view of players.
     *
     * @return player id to name map
     */
    public Map<String, String> getPlayersReadonly() {
        return Collections.unmodifiableMap(players);
    }

    /**
     * Gets lightweight player information for messages.
     *
     * @return player brief list
     */
    public List<PlayerBrief> getPlayerBriefs() {
        return players.entrySet().stream()
                .map(e -> new PlayerBrief(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Gets a read-only view of client connections.
     *
     * @return player id to connection map
     */
    public Map<String, ClientConnection> getConnectionsReadonly() {
        return Collections.unmodifiableMap(connections);
    }

    /**
     * Lightweight player information used in initialization and lobby messages.
     *
     * @param playerId player id
     * @param playerName player display name
     */
    public record PlayerBrief(String playerId, String playerName) implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
    }
}
