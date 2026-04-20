package com.kotva.lan.message;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.lan.LocalGameSession.PlayerBrief;
import java.util.List;

public class GameInitializationMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String sessionId;
    private final String hostPlayerId;
    private final List<PlayerBrief> players;
    private final GameConfig gameConfig;
    private final String localPlayerId;
    private final GameSessionSnapshot initialSnapshot;

    public GameInitializationMessage(String sessionId, String hostPlayerId, List<PlayerBrief> players) {
        this(sessionId, hostPlayerId, players, null, null, null);
    }

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

    public String getSessionId() {
        return sessionId;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public List<PlayerBrief> getPlayers() {
        return players;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public String getLocalPlayerId() {
        return localPlayerId;
    }

    public GameSessionSnapshot getInitialSnapshot() {
        return initialSnapshot;
    }
}
