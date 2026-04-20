package com.kotva.lan.message;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSessionSnapshot;
import java.util.Objects;

public class GameStartMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String localPlayerId;
    private final GameConfig gameConfig;
    private final GameSessionSnapshot initialSnapshot;

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

    public String getLocalPlayerId() {
        return localPlayerId;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public GameSessionSnapshot getInitialSnapshot() {
        return initialSnapshot;
    }
}
