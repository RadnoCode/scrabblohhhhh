package com.kotva.lan;

import com.kotva.runtime.LanLaunchConfig;
import com.kotva.runtime.LanRole;
import com.kotva.lan.message.GameStartMessage;
import com.kotva.lan.message.LobbyStateMessage;
import com.kotva.lan.message.LocalGameMessage;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client-side object used while a player is waiting in a LAN lobby.
 */
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

    /**
     * Creates a lobby client session.
     *
     * @param localPlayerId local player id assigned by the host
     * @param initialSnapshot initial lobby snapshot
     * @param connection TCP connection to the host
     * @param transport transport reused after the game starts
     */
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

    /**
     * Gets the local player id.
     *
     * @return local player id
     */
    public String getLocalPlayerId() {
        return localPlayerId;
    }

    /**
     * Gets the latest lobby snapshot.
     *
     * @return lobby snapshot
     */
    public LanLobbySnapshot getLobbySnapshot() {
        return latestSnapshot;
    }

    /**
     * Checks whether the host has sent a game start config.
     *
     * @return {@code true} if a start config is pending
     */
    public boolean hasPendingStartLaunchConfig() {
        return pendingStartLaunchConfig.get() != null;
    }

    /**
     * Consumes the pending game start config.
     *
     * @return start launch config, or {@code null}
     */
    public LanLaunchConfig consumeStartLaunchConfig() {
        return pendingStartLaunchConfig.getAndSet(null);
    }

    /**
     * Disconnects from the lobby by user request.
     */
    public void disconnect() {
        closedByUser = true;
        connection.disconnect();
    }

    /**
     * Checks whether this lobby client is disconnected.
     *
     * @return {@code true} if disconnected
     */
    public boolean isDisconnected() {
        return disconnected;
    }

    /**
     * Gets the disconnect summary.
     *
     * @return disconnect summary
     */
    public String getDisconnectSummary() {
        return disconnectNotice == null ? "" : disconnectNotice.summary();
    }

    /**
     * Gets the disconnect details.
     *
     * @return disconnect details
     */
    public String getDisconnectDetails() {
        return disconnectNotice == null ? "" : disconnectNotice.details();
    }

    /**
     * Consumes the pending disconnect notice.
     *
     * @return notice, or {@code null}
     */
    public LanSystemNotice consumeDisconnectNotice() {
        return pendingDisconnectNotice.getAndSet(null);
    }

    /**
     * Handles a message received from the host.
     *
     * @param message host message
     */
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

    /**
     * Handles connection loss.
     */
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
