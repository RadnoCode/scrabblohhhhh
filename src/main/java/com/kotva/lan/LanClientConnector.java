package com.kotva.lan;

import com.kotva.application.runtime.LanLaunchConfig;
import com.kotva.application.runtime.LanRole;
import com.kotva.lan.message.GameInitializationMessage;
import com.kotva.lan.message.LobbyStateMessage;
import com.kotva.lan.message.JoinSessionMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public final class LanClientConnector {
    private LanClientConnector() {
    }

    public static LanLobbyClientSession joinLobby(String endpoint, String playerName)
            throws IOException, ClassNotFoundException {
        Endpoint resolvedEndpoint = Endpoint.parse(endpoint);
        Socket socket = new Socket(resolvedEndpoint.host(), resolvedEndpoint.port());

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
        Socket socket = new Socket(resolvedEndpoint.host(), resolvedEndpoint.port());

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

    private static String normalizePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "Guest";
        }
        return playerName.trim();
    }

    private record Endpoint(String host, int port) {
        private static Endpoint parse(String endpoint) {
            if (endpoint == null || endpoint.isBlank()) {
                return new Endpoint("127.0.0.1", GameSessionBroker.DEFAULT_PORT);
            }

            String trimmed = endpoint.trim();
            int separatorIndex = trimmed.lastIndexOf(':');
            if (separatorIndex < 0) {
                return new Endpoint(trimmed, GameSessionBroker.DEFAULT_PORT);
            }

            String host = trimmed.substring(0, separatorIndex).trim();
            String portText = trimmed.substring(separatorIndex + 1).trim();
            int port = portText.isEmpty() ? GameSessionBroker.DEFAULT_PORT : Integer.parseInt(portText);
            return new Endpoint(host.isEmpty() ? "127.0.0.1" : host, port);
        }
    }
}
