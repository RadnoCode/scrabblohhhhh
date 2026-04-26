package com.kotva.lan.message;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.lan.LocalGameSession.PlayerBrief;
import java.util.List;

/**
 * Message containing initial LAN session information for a joining client.
 */
public class GameInitializationMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String sessionId;
    private final String hostPlayerId;
    private final List<PlayerBrief> players;
    private final GameConfig gameConfig;
    private final String localPlayerId;
    private final GameSessionSnapshot initialSnapshot;

    /**
     * Creates a simple initialization message for lobby state.
     *
     * @param sessionId session id
     * @param hostPlayerId host player id
     * @param players players currently in the session
     */
    public GameInitializationMessage(String sessionId, String hostPlayerId, List<PlayerBrief> players) {
        this(sessionId, hostPlayerId, players, null, null, null);
    }

    /**
     * Creates a full initialization message.
     *
     * @param sessionId session id
     * @param hostPlayerId host player id
     * @param players players currently in the session
     * @param gameConfig game config when already available
     * @param localPlayerId local player id assigned to the client
     * @param initialSnapshot initial game snapshot
     */
    public GameInitializationMessage(
            String sessionId,
            String hostPlayerId,
            List<PlayerBrief> players,
            GameConfig gameConfig,
            String localPlayerId,
            GameSessionSnapshot initialSnapshot) {
        super(MessageType.GAME_INITIALIZATION);
        this.sessionId = sessionId;
        this.hostPlayerId = hostPlayerId;
        this.players = players;
        this.gameConfig = gameConfig;
        this.localPlayerId = localPlayerId;
        this.initialSnapshot = initialSnapshot;
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
     * Gets current players.
     *
     * @return player brief list
     */
    public List<PlayerBrief> getPlayers() {
        return players;
    }

    /**
     * Gets the game config.
     *
     * @return game config, or {@code null}
     */
    public GameConfig getGameConfig() {
        return gameConfig;
    }

    /**
     * Gets the local player id assigned to the client.
     *
     * @return local player id, or {@code null}
     */
    public String getLocalPlayerId() {
        return localPlayerId;
    }

    /**
     * Gets the initial game snapshot.
     *
     * @return initial snapshot, or {@code null}
     */
    public GameSessionSnapshot getInitialSnapshot() {
        return initialSnapshot;
    }
}
