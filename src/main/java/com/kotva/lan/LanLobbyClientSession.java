package com.kotva.lan;

import com.kotva.application.runtime.LanLaunchConfig;
import com.kotva.application.runtime.LanRole;
import com.kotva.lan.message.GameStartMessage;
import com.kotva.lan.message.LobbyStateMessage;
import com.kotva.lan.message.LocalGameMessage;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class LanLobbyClientSession {
    private final String localPlayerId;
    private final ClientConnection connection;
    private final SocketLanClientTransport transport;
    private final AtomicReference<LanLaunchConfig> pendingStartLaunchConfig;

    private volatile LanLobbySnapshot latestSnapshot;

    LanLobbyClientSession(
            String localPlayerId,
            LanLobbySnapshot initialSnapshot,
            ClientConnection connection,
            SocketLanClientTransport transport) {
        this.localPlayerId = Objects.requireNonNull(localPlayerId, "localPlayerId cannot be null.");
        this.latestSnapshot = Objects.requireNonNull(initialSnapshot, "initialSnapshot cannot be null.");
        this.connection = Objects.requireNonNull(connection, "connection cannot be null.");
        this.transport = Objects.requireNonNull(transport, "transport cannot be null.");
        this.pendingStartLaunchConfig = new AtomicReference<>();
    }

    public String getLocalPlayerId() {
        return localPlayerId;
    }

    public LanLobbySnapshot getLobbySnapshot() {
        return latestSnapshot;
    }

    public boolean hasPendingStartLaunchConfig() {
        return pendingStartLaunchConfig.get() != null;
    }

    public LanLaunchConfig consumeStartLaunchConfig() {
        return pendingStartLaunchConfig.getAndSet(null);
    }

    public void disconnect() {
        connection.disconnect();
    }

    void onNetworkMessage(LocalGameMessage message) {
        if (message instanceof LobbyStateMessage lobbyStateMessage) {
            latestSnapshot = lobbyStateMessage.getSnapshot();
            return;
        }

        if (message instanceof GameStartMessage gameStartMessage) {
            pendingStartLaunchConfig.set(
                    new LanLaunchConfig(
                            LanRole.CLIENT,
                            gameStartMessage.getGameConfig(),
                            localPlayerId,
                            gameStartMessage.getInitialSnapshot(),
                            transport));
            return;
        }

        transport.onNetworkMessage(message);
    }

    void onDisconnect() {
        connection.disconnect();
    }
}
