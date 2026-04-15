package com.kotva.lan;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.kotva.lan.message.LocalGameMessage;

public class ClientConnection {

    private static final Logger logger = Logger.getLogger(ClientConnection.class.getName());
    private final String playerId;  // the ID of the player associated with this connection (could be UUID or in-game name)

    private final Socket socket; //create a TCP socket for communication with the client

    private final ObjectOutputStream out;

    private volatile boolean closed = false;

    public ClientConnection(String playerId, Socket socket) throws IOException {
        this.playerId = playerId;
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush(); // ensure the header is sent immediately, so the other side's ObjectInputStream can be constructed without blocking
    }

    public void startListening(Consumer<LocalGameMessage> onMessage, Runnable onDisconnect) {

        Thread listenerThread = new Thread(() -> {

                try {
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); // create ObjectInputStream to read messages from the client

                    while (!closed && !socket.isClosed()) {

                        LocalGameMessage message = (LocalGameMessage) in.readObject();
                        onMessage.accept(message);
                    }

                } catch (EOFException e) {
                    if (!closed) {
                        logger.info("[" + playerId + "] Remote closed connection normally.");
                    }

                } catch (SocketException e) {
                    if (!closed) {
                        logger.warning("[" + playerId + "] Socket closed unexpectedly: " + e.getMessage());
                    }

                } catch (ClassNotFoundException e) {
                    logger.severe("[" + playerId + "] Unknown message class received: " + e.getMessage());

                } catch (IOException e) {
                    if (!closed) {
                        logger.warning("[" + playerId + "] I/O error: " + e.getMessage());
                    }

                } finally {
                    onDisconnect.run();  // whether we exit the loop normally (remote closed) or due to an exception, we consider the connection closed and trigger the onDisconnect callback.
                }

            }, "ClientConnection-" + playerId);

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void sendMessage(LocalGameMessage message) {
        if (closed) {
            logger.warning("[" + playerId + "] Tried to send message on closed connection, ignoring.");
            return;
        }

        synchronized (out) {
            try {
                out.writeObject(message);
                out.flush();  //sent the message immediately, don't wait for the buffer to fill up
                out.reset();
            } catch (IOException e) {
                logger.warning("[" + playerId + "] Failed to send message: " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        closed = true;
        try {
            socket.close();
        } catch (IOException e) {
            logger.warning("[" + playerId + "] Error while closing socket: " + e.getMessage());
        }
    }

    public String getPlayerId() {
        return playerId;
    }

    public boolean isClosed() {
        return closed || socket.isClosed();
    }
}