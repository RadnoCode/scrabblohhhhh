package com.kotva.lan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.kotva.lan.message.GameInitializationMessage;
import com.kotva.lan.message.JoinSessionMessage;
import com.kotva.lan.message.LocalGameMessage;
import com.kotva.lan.message.MessageType;

/**owned by the server, this class manages the collection of active game sessions. It provides methods for creating new sessions, finding existing sessions by ID, and removing sessions when they are closed. 
 * The GameSessionBroker is responsible for maintaining the overall state of all game sessions on the server and ensuring that players are correctly routed to their respective sessions when they join or interact with the game. 
*/
public class GameSessionBroker {
    
    private static final Logger logger = Logger.getLogger(GameSessionBroker.class.getName());

    private final int port; // the port number on which the server listens for incoming connections

    private final AtomicBoolean running = new AtomicBoolean(false); // flag to indicate whether the server is currently running

    private volatile ServerSocket serverSocket; // the ServerSocket used to accept incoming client connections
    private volatile LocalGameSession localGameSession; // manages the collection of active game sessions

    public GameSessionBroker(int port) {
        this.port = port;
    }

    public LocalGameSession getLocalGameSession() {
        return localGameSession;
    }
    // Starts the server and begins listening for incoming client connections. This method initializes the ServerSocket and enters a loop to accept clients until the server is stopped.
    public String createSession (String hostPlayerId, String hostPlayerName, int maxPlayers) throws IOException {
        if (running.get()) {
            throw new IllegalStateException("Server is already running");
        }

        Objects.requireNonNull(hostPlayerId,"hostPlayerId can not be null");
        Objects.requireNonNull(hostPlayerName,"hostPlayerName can not be null");
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException("maxPlayers must be greater than 0.");
        }
        
        this.localGameSession = new LocalGameSession(hostPlayerId, hostPlayerName, maxPlayers);
        this.serverSocket = new ServerSocket(port);
        running.set(true);
        logger.info("Server started on port " + port);
    
        Thread acceptThread = new Thread(this::acceptLoop,"LAN-AcceptLoop");
        acceptThread.setDaemon(true);
        acceptThread.start();  // start a thread to accept incoming client connections

        logger.info("Game session created with ID: " + localGameSession.getSessionId());
        return localGameSession.getSessionId();
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept(); // wait for a client to connect
                logger.info("Client connected: " + clientSocket.getRemoteSocketAddress());
                handleNewClient(clientSocket); // handle the new client connection  
            
            } catch (SocketException e) {
                if (running.get()) {
                    logger.warning("Accept loop socket error: " + e.getMessage());
                }
            } catch (IOException e) {
                if (running.get()) {
                    logger.warning("Accept loop I/O error: " + e.getMessage());
                }
            }
        }
    }

    private void handleNewClient(Socket clientSocket) {
        // This method should handle the initial handshake with the client, including receiving the JoinSessionMessage, validating the player's information, and adding them to the localGameSession if everything is valid. 
        // It should also create a new ClientConnection for the client and add it to the localGameSession's connections map.
        try{
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            Object first = in.readObject();

            if (!(first instanceof JoinSessionMessage))
            {
                logger.warning("Received invalid first message from client: " + clientSocket.getRemoteSocketAddress());
                clientSocket.close();
                return;
            }

            JoinSessionMessage joinMsg = (JoinSessionMessage) first;

            String playerId = joinMsg.getPlayerId();
            String playerName = joinMsg.getPlayerName();

            // if the session is already full, reject the new connection to prevent exceeding the maximum player limit. This check ensures that the game session maintains its intended player capacity and prevents issues that could arise from having too many players in a session, such as performance degradation or an unmanageable game state.
            if(localGameSession.isFull()){
                logger.warning("session is full, player is rejected");
                clientSocket.close();
                return;
            }
            
            // if a player with the same ID already exists in the session, reject the new connection to prevent duplicate player entries. This check ensures that each player in the session has a unique identifier, which is crucial for managing game state and player interactions correctly.
            if(localGameSession.containsPlayer(playerId))
            {
                logger.warning("player with id " + playerId + " already exists in session, player is rejected");
                clientSocket.close();
                return;
            }

            // if the new player passes all validation checks, add them to the localGameSession and create a new ClientConnection for them. This step integrates the new player into the game session, allowing them to participate in the game and receive updates from the server.
            
            ClientConnection connection = new ClientConnection(playerId, clientSocket, in);
            localGameSession.addPlayer(playerId, playerName, connection);

            // send the new player an initialization message containing the current state of the game session, including the session ID, host player information, and a list of existing players. This message allows the new player to synchronize with the current game state and prepare for gameplay.
            connection.sendMessage(
                new GameInitializationMessage(
                    localGameSession.getSessionId(), 
                    localGameSession.getHostPlayerId(), 
                    localGameSession.getPlayerBriefs())
                    );
            
            // broadcast a message to all other players in the session (except the new player) to notify them that a new player has joined. This keeps all existing players informed about changes in the game session and allows them to update their local game state accordingly.
            broadcastExcept(playerId, new JoinSessionMessage(playerId, playerName)); // notify all other players in the session that a new player has joined, except for the new player themselves who already knows they joined.
            
            // start listening for messages from the new player. This allows the server to receive and process any game actions or updates sent by the new player, enabling them to actively participate in the game session.
            connection.startListening(
                msg -> {onClientMessage(playerId, msg);},
                () -> onClientDisconnect(playerId)
                );

            logger.info("Player joined. playerId=" + playerId + ", name=" + playerName);


        } catch (IOException e) {
           logger.warning("New client handling failed: " + e.getMessage());
            safeClose(clientSocket);
        }catch (ClassNotFoundException e) {
            logger.warning("Unknown class during handshake: " + e.getMessage());
            safeClose(clientSocket);
        }
    }

    public void broadcastExcept(String excludedPlayerId, LocalGameMessage message) {
        // This method should iterate over all ClientConnections in the localGameSession's connections map and send the specified message to each client, except for the client with the given exceptPlayerId. This allows the server to efficiently broadcast messages to all players in the session while excluding a specific player when necessary (e.g., when notifying other players about a new player joining).
        localGameSession.getConnectionsReadonly().forEach((playerId, connection) -> {
            if (!playerId.equals(excludedPlayerId)) {
                connection.sendMessage(message);
            }
        });
    }

    public void broadcastToAll(LocalGameMessage message) {
        // This method should iterate over all ClientConnections in the localGameSession's connections map and send the specified message to each client. This allows the server to efficiently broadcast messages to all players in the session (e.g., when sending game state updates or global notifications).
        localGameSession.getConnectionsReadonly().forEach((playerId, connection) -> {
            connection.sendMessage(message);
        });
    }

    private void onClientMessage(String playerId, LocalGameMessage message) {
        // This method should handle incoming messages from clients. It should determine the type of the message and perform the appropriate actions based on the message content. For example, if the message is a game action, it should update the game state accordingly and broadcast any necessary updates to other players in the session.
        if (message == null) return;

        MessageType type = message.getType();

        switch (type) {
            case PLAYER_ACTION ->
                // handle player action message
                broadcastExcept(playerId, message);
            default->
               logger.info("Unhandled message type in Day2: " + type);
        }

    }

    private void onClientDisconnect(String playerId) {
        ClientConnection conn = localGameSession.getConnectionsReadonly().get(playerId);
        if (conn != null && !conn.isClosed()) {
            conn.disconnect();
        }
        localGameSession.removePlayer(playerId);
        logger.info("Player disconnected: " + playerId);
    }

    private void safeClose(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            logger.warning("Failed to close socket: " + e.getMessage());
        }
    }

    public void stopServer() {
        running.set(false);
        if (localGameSession != null) {
            localGameSession.getConnectionsReadonly().values().forEach(ClientConnection::disconnect);
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing server socket: " + e.getMessage());
            }
        }
        logger.info("Server stopped.");
    }

}
