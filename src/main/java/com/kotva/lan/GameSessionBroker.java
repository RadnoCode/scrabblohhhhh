package com.kotva.lan;

import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.GameSession;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.RemoteCommandResult;
import com.kotva.lan.message.CommandRequestMessage;
import com.kotva.lan.message.CommandResultMessage;
import com.kotva.lan.message.GameInitializationMessage;
import com.kotva.lan.message.JoinSessionMessage;
import com.kotva.lan.message.LocalGameMessage;
import com.kotva.lan.message.MessageType;
import com.kotva.lan.message.PlayerJoinedMessage;
import com.kotva.lan.message.SnapshotUpdateMessage;
import com.kotva.policy.SessionStatus;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Host-side LAN broker. It owns the room socket, assigns remote seats, routes
 * command requests to the authoritative host service, and sends viewer-specific
 * snapshots back to each connected client.
 */
public class GameSessionBroker {
    public static final int DEFAULT_PORT = 5050;

    private static final Logger logger = Logger.getLogger(GameSessionBroker.class.getName());

    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile ServerSocket serverSocket;
    private volatile LocalGameSession localGameSession;
    private volatile GameSession authoritativeSession;
    private volatile LanHostService lanHostService;

    public GameSessionBroker(int port) {
        this.port = port;
    }

    public LocalGameSession getLocalGameSession() {
        return localGameSession;
    }

    public synchronized String createSession(
            GameSession session,
            LanHostService lanHostService,
            String hostPlayerId,
            String hostPlayerName) throws IOException {
        if (running.get()) {
            throw new IllegalStateException("Server is already running.");
        }

        this.authoritativeSession = Objects.requireNonNull(session, "session cannot be null.");
        this.lanHostService = Objects.requireNonNull(
                lanHostService,
                "lanHostService cannot be null.");
        Objects.requireNonNull(hostPlayerId, "hostPlayerId cannot be null.");
        Objects.requireNonNull(hostPlayerName, "hostPlayerName cannot be null.");

        this.localGameSession =
                new LocalGameSession(
                        session.getSessionId(),
                        hostPlayerId,
                        hostPlayerName,
                        session.getConfig().getPlayerCount());
        this.serverSocket = new ServerSocket(port);
        running.set(true);

        Thread acceptThread = new Thread(this::acceptLoop, "LAN-AcceptLoop");
        acceptThread.setDaemon(true);
        acceptThread.start();

        logger.info(
                "LAN host broker started on port "
                        + port
                        + " for session "
                        + localGameSession.getSessionId());
        return localGameSession.getSessionId();
    }

    public void broadcastViewerSnapshotsToAllConnectedClients() {
        broadcastViewerSnapshotsToAllConnectedClients(null);
    }

    public void broadcastExcept(String excludedPlayerId, LocalGameMessage message) {
        if (localGameSession == null || message == null) {
            return;
        }
        localGameSession.getConnectionsReadonly().forEach((playerId, connection) -> {
            if (!Objects.equals(playerId, excludedPlayerId)) {
                connection.sendMessage(message);
            }
        });
    }

    public void broadcastToAll(LocalGameMessage message) {
        broadcastExcept(null, message);
    }

    public void stopServer() {
        running.set(false);
        if (localGameSession != null) {
            localGameSession.getConnectionsReadonly().values().forEach(ClientConnection::disconnect);
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException exception) {
                logger.warning("Error closing server socket: " + exception.getMessage());
            }
        }
        serverSocket = null;
        localGameSession = null;
        authoritativeSession = null;
        lanHostService = null;
        logger.info("LAN host broker stopped.");
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleNewClient(clientSocket);
            } catch (SocketException exception) {
                if (running.get()) {
                    logger.warning("Accept loop socket error: " + exception.getMessage());
                }
            } catch (IOException exception) {
                if (running.get()) {
                    logger.warning("Accept loop I/O error: " + exception.getMessage());
                }
            }
        }
    }

    private void handleNewClient(Socket clientSocket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            Object firstMessage = in.readObject();
            if (!(firstMessage instanceof JoinSessionMessage)) {
                logger.warning("Rejected client without JoinSessionMessage handshake.");
                safeClose(clientSocket);
                return;
            }

            AssignedSeat assignedSeat = resolveNextRemoteSeat();
            if (assignedSeat == null) {
                logger.warning("Rejected client because no LAN seat is available.");
                safeClose(clientSocket);
                return;
            }

            ClientConnection connection =
                    new ClientConnection(assignedSeat.playerId(), clientSocket, in, out);
            localGameSession.addPlayer(
                    assignedSeat.playerId(),
                    assignedSeat.playerName(),
                    connection);

            connection.sendMessage(buildInitializationMessage(assignedSeat));
            broadcastExcept(
                    assignedSeat.playerId(),
                    new PlayerJoinedMessage(assignedSeat.playerId(), assignedSeat.playerName()));
            connection.startListening(
                    message -> onClientMessage(assignedSeat.playerId(), message),
                    () -> onClientDisconnect(assignedSeat.playerId()));

            logger.info(
                    "Remote LAN player joined as "
                            + assignedSeat.playerId()
                            + " ("
                            + assignedSeat.playerName()
                            + ").");
        } catch (IOException exception) {
            logger.warning("Failed to accept LAN client: " + exception.getMessage());
            safeClose(clientSocket);
        } catch (ClassNotFoundException exception) {
            logger.warning("Unknown class during LAN handshake: " + exception.getMessage());
            safeClose(clientSocket);
        }
    }

    private GameInitializationMessage buildInitializationMessage(AssignedSeat assignedSeat) {
        return new GameInitializationMessage(
                localGameSession.getSessionId(),
                localGameSession.getHostPlayerId(),
                localGameSession.getPlayerBriefs(),
                authoritativeSession.getConfig(),
                assignedSeat.playerId(),
                requireLanHostService().snapshotForViewer(assignedSeat.playerId()));
    }

    private AssignedSeat resolveNextRemoteSeat() {
        if (localGameSession == null || authoritativeSession == null) {
            return null;
        }
        if (localGameSession.isFull()) {
            return null;
        }

        for (int index = 0; index < authoritativeSession.getConfig().getPlayers().size(); index++) {
            String candidatePlayerId = "player-" + (index + 1);
            if (Objects.equals(candidatePlayerId, localGameSession.getHostPlayerId())) {
                continue;
            }
            if (localGameSession.containsPlayer(candidatePlayerId)) {
                continue;
            }
            return new AssignedSeat(
                    candidatePlayerId,
                    authoritativeSession.getConfig().getPlayers().get(index).getPlayerName());
        }
        return null;
    }

    private void onClientMessage(String playerId, LocalGameMessage message) {
        if (message == null) {
            return;
        }

        MessageType type = message.getType();
        switch (type) {
            case COMMAND_REQUEST -> handleCommandRequest(playerId, (CommandRequestMessage) message);
            case PLAYER_ACTION -> broadcastExcept(playerId, message);
            default -> logger.info("Unhandled LAN message type: " + type);
        }
    }

    private void handleCommandRequest(String playerId, CommandRequestMessage message) {
        CommandEnvelope commandEnvelope =
                Objects.requireNonNull(message, "message cannot be null.").getCommandEnvelope();
        ClientConnection connection =
                localGameSession == null
                        ? null
                        : localGameSession.getConnectionsReadonly().get(playerId);
        if (connection == null) {
            return;
        }

        if (!Objects.equals(playerId, commandEnvelope.getPlayerId())) {
            connection.sendMessage(
                    new CommandResultMessage(
                            buildRejectedCommandResult(
                                    playerId,
                                    commandEnvelope,
                                    "Connection playerId does not match command playerId.")));
            return;
        }

        RemoteCommandResult result = requireLanHostService().handle(commandEnvelope);
        connection.sendMessage(new CommandResultMessage(result));
        if (result.success()) {
            broadcastViewerSnapshotsToAllConnectedClients(playerId);
        }
    }

    private RemoteCommandResult buildRejectedCommandResult(
            String playerId,
            CommandEnvelope commandEnvelope,
            String message) {
        return new RemoteCommandResult(
                commandEnvelope == null ? null : commandEnvelope.getCommandId(),
                false,
                message,
                0,
                authoritativeSession.getGameState().getCurrentPlayer().getPlayerId(),
                authoritativeSession.getSessionStatus() == SessionStatus.COMPLETED,
                authoritativeSession.getTurnCoordinator().getSettlementResult(),
                requireLanHostService().snapshotForViewer(playerId));
    }

    private void broadcastViewerSnapshotsToAllConnectedClients(String excludedPlayerId) {
        if (localGameSession == null || lanHostService == null) {
            return;
        }
        localGameSession.getConnectionsReadonly().forEach((playerId, connection) -> {
            if (Objects.equals(playerId, excludedPlayerId)) {
                return;
            }
            connection.sendMessage(
                    new SnapshotUpdateMessage(requireLanHostService().snapshotForViewer(playerId)));
        });
    }

    private void onClientDisconnect(String playerId) {
        if (localGameSession == null) {
            return;
        }
        ClientConnection connection = localGameSession.getConnectionsReadonly().get(playerId);
        if (connection != null && !connection.isClosed()) {
            connection.disconnect();
        }
        localGameSession.removePlayer(playerId);
        logger.info("LAN player disconnected: " + playerId);
    }

    private LanHostService requireLanHostService() {
        return Objects.requireNonNull(lanHostService, "lanHostService cannot be null.");
    }

    private void safeClose(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException exception) {
            logger.warning("Failed to close socket: " + exception.getMessage());
        }
    }

    private record AssignedSeat(String playerId, String playerName) {
    }
}
