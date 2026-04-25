package com.kotva.lan;

import com.kotva.runtime.LanLaunchConfig;
import com.kotva.runtime.LanRole;
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
    private final AtomicReference<LanSystemNotice> pendingDisconnectNotice;

    private volatile LanLobbySnapshot latestSnapshot;
    private volatile boolean disconnected;
    private volatile boolean closedByUser;
    private volatile LanSystemNotice disconnectNotice;

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
        this.pendingDisconnectNotice = new AtomicReference<>();
        this.disconnected = false;
        this.closedByUser = false;
        this.disconnectNotice = null;
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
        closedByUser = true;
        connection.disconnect();
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public String getDisconnectSummary() {
        return disconnectNotice == null ? "" : disconnectNotice.summary();
    }

    public String getDisconnectDetails() {
        return disconnectNotice == null ? "" : disconnectNotice.details();
    }

    public LanSystemNotice consumeDisconnectNotice() {
        return pendingDisconnectNotice.getAndSet(null);
    }

    void onNetworkMessage(LocalGameMessage message) {
        if (disconnected) {
            return;
        }
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
        if (!closedByUser && !disconnected) {
            disconnected = true;
            disconnectNotice = new LanSystemNotice(
                    "Connection lost to host.",
                    "Lost TCP connection to host.",
                    true);
            pendingDisconnectNotice.set(disconnectNotice);
        }
        connection.disconnect();
    }
}
