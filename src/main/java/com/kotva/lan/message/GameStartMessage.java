package com.kotva.lan.message;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSessionSnapshot;
import java.util.Objects;

/**
 * Message sent by the host when the LAN game starts.
 */
public class GameStartMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String localPlayerId;
    private final GameConfig gameConfig;
    private final GameSessionSnapshot initialSnapshot;

    /**
     * Creates a game start message.
     *
     * @param localPlayerId player id assigned to the receiving client
     * @param gameConfig game config
     * @param initialSnapshot first game snapshot for the client
     */
    public GameStartMessage(
            String localPlayerId,
            GameConfig gameConfig,
            GameSessionSnapshot initialSnapshot) {
        super(MessageType.GAME_START);
        this.localPlayerId = Objects.requireNonNull(localPlayerId, "localPlayerId cannot be null.");
        this.gameConfig = Objects.requireNonNull(gameConfig, "gameConfig cannot be null.");
        this.initialSnapshot = Objects.requireNonNull(
                initialSnapshot,
                "initialSnapshot cannot be null.");
    }

    /**
     * Gets the local player id.
     *
     * @return local player id
     */
    public String getLocalPlayerId() {
        return localPlayerId;
    }

    /**
     * Gets the game config.
     *
     * @return game config
     */
    public GameConfig getGameConfig() {
        return gameConfig;
    }

    /**
     * Gets the initial game snapshot.
     *
     * @return initial snapshot
     */
    public GameSessionSnapshot getInitialSnapshot() {
        return initialSnapshot;
    }
}
