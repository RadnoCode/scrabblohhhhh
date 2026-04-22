package com.kotva.application.runtime;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.infrastructure.network.LanClientTransport;
import java.util.Objects;

public final class LanLaunchConfig {
    private final LanRole role;
    private final GameConfig gameConfig;
    private final String localPlayerId;
    private final GameSessionSnapshot initialSnapshot;
    private final LanClientTransport clientTransport;

    public LanLaunchConfig(
            LanRole role,
            GameConfig gameConfig,
            String localPlayerId,
            GameSessionSnapshot initialSnapshot,
            LanClientTransport clientTransport) {
        this.role = Objects.requireNonNull(role, "role cannot be null.");
        this.gameConfig = Objects.requireNonNull(gameConfig, "gameConfig cannot be null.");
        this.localPlayerId = Objects.requireNonNull(localPlayerId, "localPlayerId cannot be null.");
        this.initialSnapshot = initialSnapshot;
        this.clientTransport = clientTransport;
        if (role == LanRole.CLIENT && initialSnapshot == null) {
            throw new IllegalArgumentException("Client LAN launch requires initialSnapshot.");
        }
        if (role == LanRole.CLIENT && clientTransport == null) {
            throw new IllegalArgumentException("Client LAN launch requires clientTransport.");
        }
    }

    public LanRole getRole() {
        return role;
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

    public LanClientTransport getClientTransport() {
        return clientTransport;
    }
}
