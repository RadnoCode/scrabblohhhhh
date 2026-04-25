package com.kotva.lan;

import com.kotva.runtime.LanLaunchConfig;
import com.kotva.runtime.LanRole;
import com.kotva.lan.message.GameInitializationMessage;
import com.kotva.lan.message.JoinSessionMessage;
import com.kotva.lan.message.LobbyStateMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.UUID;

public final class LanClientConnector {
    public static final int CONNECT_TIMEOUT_MILLIS = 4_000;
    public static final int HANDSHAKE_TIMEOUT_MILLIS = 4_000;

    private LanClientConnector() {
    }

    public static LanLobbyClientSession joinLobby(String endpoint, String playerName)
            throws IOException, ClassNotFoundException {
        Endpoint resolvedEndpoint = Endpoint.parse(endpoint);
        Socket socket = openSocket(resolvedEndpoint);

        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(
                    new JoinSessionMessage(
                            UUID.randomUUID().toString(),
                            normalizePlayerName(playerName)));
            out.flush();
            out.reset();

            Object firstMessage = in.readObject();
            if (!(firstMessage instanceof LobbyStateMessage lobbyStateMessage)) {
                throw new IOException("Expected LobbyStateMessage from host.");
            }
            if (lobbyStateMessage.getLocalPlayerId() == null
                    || lobbyStateMessage.getSnapshot() == null) {
                throw new IOException("LobbyStateMessage is missing required join data.");
            }

            socket.setSoTimeout(0);
            ClientConnection connection =
                    new ClientConnection(
                            lobbyStateMessage.getLocalPlayerId(),
                            socket,
                            in,
                            out);
            SocketLanClientTransport transport = new SocketLanClientTransport(connection);
            LanLobbyClientSession lobbyClientSession =
                    new LanLobbyClientSession(
                            lobbyStateMessage.getLocalPlayerId(),
                            lobbyStateMessage.getSnapshot(),
                            connection,
                            transport);
            connection.startListening(
                    lobbyClientSession::onNetworkMessage,
                    lobbyClientSession::onDisconnect);
            return lobbyClientSession;
        } catch (IOException | ClassNotFoundException exception) {
            socket.close();
            throw exception;
        }
    }

    public static LanLaunchConfig connect(String endpoint) throws IOException, ClassNotFoundException {
        Endpoint resolvedEndpoint = Endpoint.parse(endpoint);
        Socket socket = openSocket(resolvedEndpoint);

        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new JoinSessionMessage(UUID.randomUUID().toString(), "Guest"));
            out.flush();
            out.reset();

            Object firstMessage = in.readObject();
            if (!(firstMessage instanceof GameInitializationMessage initializationMessage)) {
                throw new IOException("Expected GameInitializationMessage from host.");
            }

            socket.setSoTimeout(0);
            ClientConnection connection =
                    new ClientConnection(
                            initializationMessage.getLocalPlayerId(),
                            socket,
                            in,
                            out);
            SocketLanClientTransport transport = new SocketLanClientTransport(connection);
            connection.startListening(transport::onNetworkMessage, transport::onDisconnect);

            return new LanLaunchConfig(
                    LanRole.CLIENT,
                    initializationMessage.getGameConfig(),
                    initializationMessage.getLocalPlayerId(),
                    initializationMessage.getInitialSnapshot(),
                    transport);
        } catch (IOException | ClassNotFoundException exception) {
            socket.close();
            throw exception;
        }
    }

    public static String sanitizeEndpointInput(String endpoint) {
        if (endpoint == null) {
            return "";
        }

        String normalized = endpoint.trim()
                .replace('\uFF1A', ':')
                .replaceAll("\\s+", "");

        int schemeIndex = normalized.indexOf("://");
        if (schemeIndex >= 0) {
            normalized = normalized.substring(schemeIndex + 3);
        }

        int slashIndex = normalized.indexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(0, slashIndex);
        }

        return normalized;
    }

    public static String normalizeEndpointForDisplay(String endpoint) {
        return Endpoint.parse(endpoint).displayValue();
    }

    private static String normalizePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "Guest";
        }
        return playerName.trim();
    }

    private static Socket openSocket(Endpoint resolvedEndpoint) throws IOException {
        Socket socket = new Socket(Proxy.NO_PROXY);
        try {
            socket.connect(
                    new InetSocketAddress(resolvedEndpoint.host(), resolvedEndpoint.port()),
                    CONNECT_TIMEOUT_MILLIS);
            socket.setSoTimeout(HANDSHAKE_TIMEOUT_MILLIS);
            return socket;
        } catch (IOException exception) {
            socket.close();
            throw exception;
        }
    }

    private record Endpoint(String host, int port) {
        private static Endpoint parse(String endpoint) {
            if (endpoint == null || endpoint.isBlank()) {
                return new Endpoint("127.0.0.1", GameSessionBroker.DEFAULT_PORT);
            }

            String trimmed = sanitizeEndpointInput(endpoint);
            int separatorIndex = trimmed.lastIndexOf(':');
            if (separatorIndex < 0) {
                return new Endpoint(trimmed, GameSessionBroker.DEFAULT_PORT);
            }

            String host = trimmed.substring(0, separatorIndex).trim();
            String portText = trimmed.substring(separatorIndex + 1).trim();
            int port;
            try {
                port = portText.isEmpty() ? GameSessionBroker.DEFAULT_PORT : Integer.parseInt(portText);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "Invalid LAN address. Use host[:port], for example 10.190.129.253:5050.",
                        exception);
            }
            if (port < 1 || port > 65_535) {
                throw new IllegalArgumentException("LAN port must be between 1 and 65535.");
            }
            return new Endpoint(host.isEmpty() ? "127.0.0.1" : host, port);
        }

        private String displayValue() {
            return host + ":" + port;
        }
    }
}
