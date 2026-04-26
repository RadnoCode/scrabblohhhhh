package com.kotva.lan;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.PlayerConfig;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.RemoteCommandResult;
import com.kotva.lan.message.CommandRequestMessage;
import com.kotva.lan.message.CommandResultMessage;
import com.kotva.lan.message.GameInitializationMessage;
import com.kotva.lan.message.GameStartMessage;
import com.kotva.lan.message.JoinSessionMessage;
import com.kotva.lan.message.LobbyStateMessage;
import com.kotva.lan.message.LocalGameMessage;
import com.kotva.lan.message.MessageType;
import com.kotva.lan.message.PlayerDisconnectedMessage;
import com.kotva.lan.message.PlayerJoinedMessage;
import com.kotva.lan.message.SnapshotUpdateMessage;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Host-side LAN broker that manages the lobby, TCP clients, and game messages.
 */
public class GameSessionBroker {
    public static final int DEFAULT_PORT = 5050;
    private static final int HANDSHAKE_TIMEOUT_MILLIS = 4_000;

    private static final Logger logger = Logger.getLogger(GameSessionBroker.class.getName());

    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Queue<LanSystemNotice> pendingSystemNotices = new ConcurrentLinkedQueue<>();

    private volatile ServerSocket serverSocket;
    private volatile InetAddress listeningAddress;
    private volatile LanSystemNotice blockingSystemNotice;
    private volatile LocalGameSession localGameSession;
    private volatile GameSession authoritativeSession;
    private volatile LanHostService lanHostService;
    private volatile LanLobbySettings lobbySettings;
    private volatile LanLobbyPhase lobbyPhase;
    private volatile BrokerMode brokerMode;

    /**
     * Creates a broker for a TCP port.
     *
     * @param port port to bind
     */
    public GameSessionBroker(int port) {
        this.port = port;
        this.lobbyPhase = LanLobbyPhase.CLOSED;
        this.brokerMode = null;
    }

    /**
     * Gets the local LAN session.
     *
     * @return local game session, or {@code null}
     */
    public LocalGameSession getLocalGameSession() {
        return localGameSession;
    }

    /**
     * Gets the actual bound port.
     *
     * @return bound port
     */
    public int getBoundPort() {
        return serverSocket == null ? port : serverSocket.getLocalPort();
    }

    /**
     * Gets the server listening address.
     *
     * @return listening address, or {@code null}
     */
    public InetAddress getListeningAddress() {
        return listeningAddress;
    }

    /**
     * Gets the preferred host address to advertise.
     *
     * @return host IPv4 address, or empty string
     */
    public String getAdvertisedHostAddress() {
        InetAddress address = LanHostAddressResolver.resolvePreferredIpv4Address();
        return address == null ? "" : address.getHostAddress();
    }

    /**
     * Gets the join endpoint advertised to clients.
     *
     * @return host:port endpoint
     */
    public String getAdvertisedJoinEndpoint() {
        return LanHostAddressResolver.resolveJoinEndpoint(getBoundPort());
    }

    /**
     * Drains queued system notices.
     *
     * @return system notices
     */
    public List<LanSystemNotice> drainSystemNotices() {
        List<LanSystemNotice> notices = new ArrayList<>();
        LanSystemNotice notice;
        while ((notice = pendingSystemNotices.poll()) != null) {
            notices.add(notice);
        }
        return notices;
    }

    /**
     * Gets the blocking system notice.
     *
     * @return blocking notice, or {@code null}
     */
    public LanSystemNotice getBlockingSystemNotice() {
        return blockingSystemNotice;
    }

    /**
     * Checks whether a blocking system notice exists.
     *
     * @return {@code true} if a blocking notice exists
     */
    public boolean hasBlockingSystemNotice() {
        return blockingSystemNotice != null;
    }

    /**
     * Gets the current lobby snapshot.
     *
     * @return lobby snapshot, or {@code null}
     */
    public LanLobbySnapshot getLobbySnapshot() {
        if (localGameSession == null || lobbySettings == null) {
            return null;
        }
        return buildLobbySnapshot();
    }

    /**
     * Creates a direct-to-game LAN session.
     *
     * @param session authoritative game session
     * @param lanHostService host command service
     * @param hostPlayerId host player id
     * @param hostPlayerName host player name
     * @return session id
     * @throws IOException if the server socket cannot be opened
     */
    public synchronized String createSession(
            GameSession session,
            LanHostService lanHostService,
            String hostPlayerId,
            String hostPlayerName) throws IOException {
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(lanHostService, "lanHostService cannot be null.");
        this.authoritativeSession = session;
        this.lanHostService = lanHostService;
        this.lobbySettings =
                new LanLobbySettings(
                        "LAN Match",
                        session.getConfig().getDictionaryType(),
                        session.getConfig().getTimeControlConfig(),
                        session.getConfig().getPlayerCount());
        pendingSystemNotices.clear();
        blockingSystemNotice = null;
        this.brokerMode = BrokerMode.DIRECT_SESSION;
        this.lobbyPhase = LanLobbyPhase.STARTED;

        return initializeServer(
                new LocalGameSession(
                        session.getSessionId(),
                        hostPlayerId,
                        hostPlayerName,
                        session.getConfig().getPlayerCount()));
    }

    /**
     * Creates a lobby that waits for players before starting.
     *
     * @param settings lobby settings
     * @param hostPlayerId host player id
     * @param hostPlayerName host player name
     * @return lobby session id
     * @throws IOException if the server socket cannot be opened
     */
    public synchronized String createLobby(
            LanLobbySettings settings,
            String hostPlayerId,
            String hostPlayerName) throws IOException {
        Objects.requireNonNull(settings, "settings cannot be null.");
        Objects.requireNonNull(hostPlayerId, "hostPlayerId cannot be null.");
        Objects.requireNonNull(hostPlayerName, "hostPlayerName cannot be null.");

        this.authoritativeSession = null;
        this.lanHostService = null;
        this.lobbySettings = settings;
        pendingSystemNotices.clear();
        blockingSystemNotice = null;
        this.brokerMode = BrokerMode.LOBBY;
        this.lobbyPhase = LanLobbyPhase.WAITING_FOR_PLAYERS;

        return initializeServer(
                new LocalGameSession(
                        hostPlayerId,
                        normalizePlayerName(hostPlayerName),
                        settings.getMaxPlayers()));
    }

    /**
     * Starts the game from a waiting lobby.
     *
     * @param gameSetupService service used to create the game
     * @param gameApplicationService service used to execute gameplay commands
     * @return host game launch data
     */
    public synchronized LanHostGameLaunch startGame(
            GameSetupService gameSetupService,
            GameApplicationService gameApplicationService) {
        if (brokerMode != BrokerMode.LOBBY) {
            throw new IllegalStateException("startGame is only available for lobby-mode brokers.");
        }
        if (lobbyPhase != LanLobbyPhase.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Lobby is no longer waiting for players.");
        }
        if (localGameSession == null || localGameSession.getCurrentPlayerCount() < 2) {
            throw new IllegalStateException("At least two players are required to start.");
        }

        GameConfig gameConfig = buildLobbyGameConfig();
        GameSession session =
                Objects.requireNonNull(gameSetupService, "gameSetupService cannot be null.")
                        .startNewGame(gameConfig);
        LanHostService hostService =
                new LanHostService(
                        session,
                        Objects.requireNonNull(
                                gameApplicationService,
                                "gameApplicationService cannot be null."));
        this.authoritativeSession = session;
        this.lanHostService = hostService;
        this.lobbyPhase = LanLobbyPhase.STARTED;
        blockingSystemNotice = null;

        broadcastGameStart();

        return new LanHostGameLaunch(
                session,
                hostService,
                hostService.snapshotForViewer(localGameSession.getHostPlayerId()));
    }

    /**
     * Broadcasts viewer-specific game snapshots to all clients.
     */
    public void broadcastViewerSnapshotsToAllConnectedClients() {
        broadcastViewerSnapshotsToAllConnectedClients(null);
    }

    /**
     * Broadcasts a message to all clients except one player.
     *
     * @param excludedPlayerId player id to skip
     * @param message message to send
     */
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

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message message to send
     */
    public void broadcastToAll(LocalGameMessage message) {
        broadcastExcept(null, message);
    }

    /**
     * Stops the LAN server and clears broker state.
     */
    public void stopServer() {
        running.set(false);
        lobbyPhase = LanLobbyPhase.CLOSED;
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
        listeningAddress = null;
        blockingSystemNotice = null;
        pendingSystemNotices.clear();
        localGameSession = null;
        authoritativeSession = null;
        lanHostService = null;
        lobbySettings = null;
        brokerMode = null;
        logger.info("LAN host broker stopped.");
    }

    /**
     * Opens the server socket and starts the accept loop.
     *
     * @param session local session to host
     * @return session id
     * @throws IOException if the socket cannot be opened
     */
    private String initializeServer(LocalGameSession session) throws IOException {
        if (running.get()) {
            throw new IllegalStateException("Server is already running.");
        }
        this.localGameSession = Objects.requireNonNull(session, "session cannot be null.");
        this.serverSocket = openServerSocket();
        running.set(true);

        Thread acceptThread = new Thread(this::acceptLoop, "LAN-AcceptLoop");
        acceptThread.setDaemon(true);
        acceptThread.start();

        logger.info(
                "LAN broker started on "
                        + describeListeningEndpoint()
                        + " for session "
                        + localGameSession.getSessionId());
        return localGameSession.getSessionId();
    }

    /**
     * Opens the TCP server socket.
     *
     * @return server socket
     * @throws IOException if bind fails
     */
    private ServerSocket openServerSocket() throws IOException {
        ServerSocket fallbackSocket = new ServerSocket();
        fallbackSocket.setReuseAddress(true);
        fallbackSocket.bind(new InetSocketAddress(port));
        listeningAddress = fallbackSocket.getInetAddress();
        return fallbackSocket;
    }

    /**
     * Builds a readable listening endpoint.
     *
     * @return listening endpoint text
     */
    private String describeListeningEndpoint() {
        String host =
                listeningAddress == null
                        ? "0.0.0.0"
                        : listeningAddress.isAnyLocalAddress()
                                ? "0.0.0.0"
                                : listeningAddress.getHostAddress();
        return host
                + ":"
                        + getBoundPort()
                ;
    }

    /**
     * Accepts incoming TCP clients while the broker is running.
     */
    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("Accepted LAN TCP connection from " + describeRemoteEndpoint(clientSocket) + ".");
                Thread handshakeThread =
                        new Thread(
                                () -> handleNewClient(clientSocket),
                                "LAN-Handshake-" + clientSocket.getPort());
                handshakeThread.setDaemon(true);
                handshakeThread.start();
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

    /**
     * Handles the join handshake for one new client.
     *
     * @param clientSocket accepted client socket
     */
    private void handleNewClient(Socket clientSocket) {
        try {
            clientSocket.setSoTimeout(HANDSHAKE_TIMEOUT_MILLIS);
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            Object firstMessage = in.readObject();
            if (!(firstMessage instanceof JoinSessionMessage joinSessionMessage)) {
                logger.warning("Rejected client without JoinSessionMessage handshake.");
                safeClose(clientSocket);
                return;
            }

            if (brokerMode == BrokerMode.LOBBY && lobbyPhase != LanLobbyPhase.WAITING_FOR_PLAYERS) {
                logger.warning("Rejected client because lobby has already started.");
                safeClose(clientSocket);
                return;
            }

            AssignedSeat assignedSeat = resolveNextSeat(joinSessionMessage.getPlayerName());
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

            if (brokerMode == BrokerMode.LOBBY && authoritativeSession == null) {
                connection.sendMessage(buildLobbyStateMessage(assignedSeat.playerId()));
                broadcastLobbyState(assignedSeat.playerId());
            } else {
                connection.sendMessage(buildInitializationMessage(assignedSeat));
                broadcastExcept(
                        assignedSeat.playerId(),
                        new PlayerJoinedMessage(assignedSeat.playerId(), assignedSeat.playerName()));
            }

            clientSocket.setSoTimeout(0);
            connection.startListening(
                    message -> onClientMessage(assignedSeat.playerId(), message),
                    () -> onClientDisconnect(assignedSeat.playerId()));

            logger.info(
                    "Remote LAN player joined as "
                            + assignedSeat.playerId()
                            + " ("
                            + assignedSeat.playerName()
                            + ") from "
                            + describeRemoteEndpoint(clientSocket)
                            + ".");
        } catch (SocketTimeoutException exception) {
            logger.warning(
                    "LAN client handshake timed out from "
                            + describeRemoteEndpoint(clientSocket)
                            + ": "
                            + exception.getMessage());
            safeClose(clientSocket);
        } catch (IOException exception) {
            logger.warning(
                    "Failed to accept LAN client from "
                            + describeRemoteEndpoint(clientSocket)
                            + ": "
                            + exception.getMessage());
            safeClose(clientSocket);
        } catch (ClassNotFoundException exception) {
            logger.warning(
                    "Unknown class during LAN handshake from "
                            + describeRemoteEndpoint(clientSocket)
                            + ": "
                            + exception.getMessage());
            safeClose(clientSocket);
        }
    }

    /**
     * Builds the initial game message for a joined direct-session client.
     *
     * @param assignedSeat assigned player seat
     * @return initialization message
     */
    private GameInitializationMessage buildInitializationMessage(AssignedSeat assignedSeat) {
        return new GameInitializationMessage(
                localGameSession.getSessionId(),
                localGameSession.getHostPlayerId(),
                localGameSession.getPlayerBriefs(),
                authoritativeSession.getConfig(),
                assignedSeat.playerId(),
                requireLanHostService().snapshotForViewer(assignedSeat.playerId()));
    }

    /**
     * Builds a lobby-state message for one client.
     *
     * @param localPlayerId local id assigned to the client
     * @return lobby state message
     */
    private LobbyStateMessage buildLobbyStateMessage(String localPlayerId) {
        return new LobbyStateMessage(localPlayerId, buildLobbySnapshot());
    }

    /**
     * Builds the current lobby snapshot.
     *
     * @return lobby snapshot
     */
    private LanLobbySnapshot buildLobbySnapshot() {
        List<LanLobbyPlayerSnapshot> players = new ArrayList<>();
        for (String playerId : buildOrderedPlayerIds()) {
            String playerName = localGameSession.getPlayersReadonly().get(playerId);
            if (playerName == null) {
                continue;
            }
            players.add(
                    new LanLobbyPlayerSnapshot(
                            playerId,
                            playerName,
                            Objects.equals(playerId, localGameSession.getHostPlayerId())));
        }
        return new LanLobbySnapshot(
                localGameSession.getSessionId(),
                lobbyPhase,
                localGameSession.getHostPlayerId(),
                lobbySettings,
                players,
                lobbyPhase == LanLobbyPhase.WAITING_FOR_PLAYERS && players.size() >= 2);
    }

    /**
     * Builds a game config from the current lobby player list.
     *
     * @return game config
     */
    private GameConfig buildLobbyGameConfig() {
        List<PlayerConfig> playerConfigs = new ArrayList<>();
        List<String> orderedPlayerIds = buildOrderedPlayerIds();
        for (int index = 0; index < orderedPlayerIds.size(); index++) {
            String playerId = orderedPlayerIds.get(index);
            String playerName = localGameSession.getPlayersReadonly().get(playerId);
            if (playerName == null) {
                continue;
            }
            playerConfigs.add(
                    new PlayerConfig(
                            playerName,
                            index == 0 ? PlayerType.LOCAL : PlayerType.LAN));
        }
        return new GameConfig(
                com.kotva.mode.GameMode.LAN_MULTIPLAYER,
                playerConfigs,
                lobbySettings.getDictionaryType(),
                lobbySettings.getTimeControlConfig(),
                null);
    }

    /**
     * Broadcasts the latest lobby state to clients.
     *
     * @param excludedPlayerId player id to skip
     */
    private void broadcastLobbyState(String excludedPlayerId) {
        if (localGameSession == null || lobbySettings == null) {
            return;
        }
        LanLobbySnapshot snapshot = buildLobbySnapshot();
        localGameSession.getConnectionsReadonly().forEach((playerId, connection) -> {
            if (Objects.equals(playerId, excludedPlayerId)) {
                return;
            }
            connection.sendMessage(new LobbyStateMessage(playerId, snapshot));
        });
    }

    /**
     * Sends game-start messages to all connected clients.
     */
    private void broadcastGameStart() {
        if (localGameSession == null || authoritativeSession == null || lanHostService == null) {
            return;
        }
        GameConfig gameConfig = authoritativeSession.getConfig();
        for (Map.Entry<String, ClientConnection> entry : localGameSession.getConnectionsReadonly().entrySet()) {
            String playerId = entry.getKey();
            ClientConnection connection = entry.getValue();
            GameSessionSnapshot initialSnapshot = requireLanHostService().snapshotForViewer(playerId);
            connection.sendMessage(new GameStartMessage(playerId, gameConfig, initialSnapshot));
        }
    }

    /**
     * Finds the next available LAN player seat.
     *
     * @param requestedPlayerName requested display name
     * @return assigned seat, or {@code null}
     */
    private AssignedSeat resolveNextSeat(String requestedPlayerName) {
        if (localGameSession == null || localGameSession.isFull()) {
            return null;
        }

        for (int index = 1; index <= localGameSession.getMaxPlayers(); index++) {
            String candidatePlayerId = "player-" + index;
            if (localGameSession.containsPlayer(candidatePlayerId)) {
                continue;
            }
            return new AssignedSeat(
                    candidatePlayerId,
                    resolveAssignedPlayerName(candidatePlayerId, requestedPlayerName));
        }
        return null;
    }

    /**
     * Resolves the display name assigned to a player id.
     *
     * @param playerId assigned player id
     * @param requestedPlayerName requested display name
     * @return assigned player name
     */
    private String resolveAssignedPlayerName(String playerId, String requestedPlayerName) {
        if (brokerMode == BrokerMode.DIRECT_SESSION && authoritativeSession != null) {
            int playerIndex = extractSeatIndex(playerId) - 1;
            if (playerIndex >= 0 && playerIndex < authoritativeSession.getConfig().getPlayers().size()) {
                return authoritativeSession.getConfig().getPlayers().get(playerIndex).getPlayerName();
            }
        }
        return ensureUniqueLobbyName(normalizePlayerName(requestedPlayerName));
    }

    /**
     * Builds player ids sorted by seat index.
     *
     * @return ordered player ids
     */
    private List<String> buildOrderedPlayerIds() {
        return localGameSession.getPlayersReadonly().keySet().stream()
                .sorted(Comparator.comparingInt(GameSessionBroker::extractSeatIndex))
                .toList();
    }

    /**
     * Handles a message received from a client.
     *
     * @param playerId sender player id
     * @param message received message
     */
    private void onClientMessage(String playerId, LocalGameMessage message) {
        if (message == null) {
            return;
        }

        MessageType type = message.getType();
        switch (type) {
            case COMMAND_REQUEST -> {
                if (lanHostService != null) {
                    handleCommandRequest(playerId, (CommandRequestMessage) message);
                }
            }
            case PLAYER_ACTION -> {
                if (brokerMode == BrokerMode.DIRECT_SESSION && authoritativeSession != null) {
                    broadcastExcept(playerId, message);
                }
            }
            default -> logger.info("Unhandled LAN message type: " + type);
        }
    }

    /**
     * Handles a gameplay command request from a client.
     *
     * @param playerId sender player id
     * @param message command request message
     */
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

    /**
     * Builds a rejected command result without applying the command.
     *
     * @param playerId viewer player id
     * @param commandEnvelope rejected command
     * @param message rejection message
     * @return remote command result
     */
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

    /**
     * Broadcasts viewer-specific snapshots after a game command.
     *
     * @param excludedPlayerId player id that already received a command result
     */
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

    /**
     * Handles a client disconnect.
     *
     * @param playerId disconnected player id
     */
    private void onClientDisconnect(String playerId) {
        if (localGameSession == null) {
            return;
        }
        String playerName = localGameSession.getPlayersReadonly().get(playerId);
        ClientConnection connection = localGameSession.getConnectionsReadonly().get(playerId);
        if (connection != null && !connection.isClosed()) {
            connection.disconnect();
        }
        localGameSession.removePlayer(playerId);
        if (brokerMode == BrokerMode.LOBBY && lobbyPhase == LanLobbyPhase.WAITING_FOR_PLAYERS) {
            broadcastLobbyState(null);
            if (playerName != null && !playerName.isBlank()) {
                pendingSystemNotices.add(
                        new LanSystemNotice(
                                playerName + " disconnected.",
                                "Lobby player disconnected.",
                                false));
            }
        } else if (playerName != null && !playerName.isBlank()) {
            LanSystemNotice notice = new LanSystemNotice(
                    playerName + " disconnected.",
                    "Remote player disconnected. The LAN match cannot continue.",
                    true);
            blockingSystemNotice = notice;
            broadcastExcept(
                    playerId,
                    new PlayerDisconnectedMessage(
                            playerId,
                            playerName,
                            notice.details()));
        }
        logger.info("LAN player disconnected: " + playerId);
    }

    /**
     * Gets the host service or fails if it is not ready.
     *
     * @return LAN host service
     */
    private LanHostService requireLanHostService() {
        return Objects.requireNonNull(lanHostService, "lanHostService cannot be null.");
    }

    /**
     * Closes a socket and logs failures.
     *
     * @param socket socket to close
     */
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

    /**
     * Describes a remote socket endpoint.
     *
     * @param socket socket to inspect
     * @return endpoint text
     */
    private String describeRemoteEndpoint(Socket socket) {
        if (socket == null || socket.getRemoteSocketAddress() == null) {
            return "unknown-remote";
        }
        return socket.getRemoteSocketAddress().toString();
    }

    /**
     * Extracts the numeric seat index from a player id.
     *
     * @param playerId player id such as player-2
     * @return seat index, or {@link Integer#MAX_VALUE}
     */
    private static int extractSeatIndex(String playerId) {
        if (playerId == null) {
            return Integer.MAX_VALUE;
        }
        int separator = playerId.lastIndexOf('-');
        if (separator < 0 || separator == playerId.length() - 1) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(playerId.substring(separator + 1));
        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Normalizes a player name.
     *
     * @param playerName raw player name
     * @return normalized player name
     */
    private static String normalizePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "Guest";
        }
        return playerName.trim();
    }

    /**
     * Ensures a lobby player name is unique.
     *
     * @param requestedPlayerName normalized requested name
     * @return unique lobby name
     */
    private String ensureUniqueLobbyName(String requestedPlayerName) {
        if (localGameSession == null) {
            return requestedPlayerName;
        }

        String normalizedCandidate = requestedPlayerName;
        int suffix = 2;
        while (containsPlayerNameIgnoreCase(normalizedCandidate)) {
            normalizedCandidate = appendSuffix(requestedPlayerName, suffix++);
        }
        return normalizedCandidate;
    }

    /**
     * Checks whether a name already exists in the lobby.
     *
     * @param candidateName candidate name
     * @return {@code true} if already used
     */
    private boolean containsPlayerNameIgnoreCase(String candidateName) {
        return localGameSession.getPlayersReadonly().values().stream()
                .filter(Objects::nonNull)
                .anyMatch(existingName -> existingName.equalsIgnoreCase(candidateName));
    }

    /**
     * Appends a numeric suffix while keeping the name short.
     *
     * @param baseName base name
     * @param suffix numeric suffix
     * @return suffixed name
     */
    private String appendSuffix(String baseName, int suffix) {
        String suffixText = Integer.toString(suffix);
        int maxBaseCodePoints = Math.max(1, 8 - suffixText.length());
        String trimmedBaseName = trimToCodePoints(baseName, maxBaseCodePoints);
        return trimmedBaseName + suffixText;
    }

    /**
     * Trims text by Unicode code point count.
     *
     * @param text source text
     * @param maxCodePoints maximum code points
     * @return trimmed text
     */
    private String trimToCodePoints(String text, int maxCodePoints) {
        if (text == null || text.isEmpty() || maxCodePoints < 1) {
            return "";
        }
        int codePointCount = text.codePointCount(0, text.length());
        if (codePointCount <= maxCodePoints) {
            return text;
        }
        int endIndex = text.offsetByCodePoints(0, maxCodePoints);
        return text.substring(0, endIndex);
    }

    /**
     * Player seat assigned by the broker.
     *
     * @param playerId assigned player id
     * @param playerName assigned player name
     */
    private record AssignedSeat(String playerId, String playerName) {
    }

    /**
     * Broker operating mode.
     */
    private enum BrokerMode {
        /** A game session already exists before clients join. */
        DIRECT_SESSION,
        /** Clients wait in a lobby before the host starts the game. */
        LOBBY
    }
}
