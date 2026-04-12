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

/**
 *   Server:                          Client:
 *   clients[0] = CC(PlayerB)            hostConnection = CC(Host)
 *   clients[1] = CC(PlayerC)
 *         ↓ sendMessage()                     ↓ sendMessage()
 *      send to B / C                           send to host
 */
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

//--------------main methods: startListening, sendMessage, disconnect----------------
    /**
     * Start a background thread to listen for incoming messages from the client.
     * @param onMessage    every time a message is received, this callback is executed with the message as parameter.    
     * @param onDisconnect  when the connection is closed (either by us or the remote), this callback is executed.
     */
    public void startListening(Consumer<LocalGameMessage> onMessage, Runnable onDisconnect) {

        Thread listenerThread = new Thread(() -> {

            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); // create ObjectInputStream to read messages from the client

                while (!closed && !socket.isClosed()) {

                    LocalGameMessage message = (LocalGameMessage) in.readObject();
                    onMessage.accept(message);
                }

            } catch (EOFException e) {
                // EOFException :End Of File. means the remote side has closed the connection and there is no more data to read.
                if (!closed) {
                    logger.info("[" + playerId + "] Remote closed connection normally.");
                }

            } catch (SocketException e) {
                // SocketException : socket was forcibly closed (e.g., disconnect() called, or network interruption)
                if (!closed) {
                    logger.warning("[" + playerId + "] Socket closed unexpectedly: " + e.getMessage());
                }

            } catch (ClassNotFoundException e) {
                // ClassNotFoundException : the received object is of a class that we don't have in our classpath. This could be a sign of incompatible versions between client and server, or a malformed message.
                logger.severe("[" + playerId + "] Unknown message class received: " + e.getMessage());

            } catch (IOException e) {
                // other IOException : could be network error, or error while reading from the stream. If it's not caused by us closing the connection, log it.
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

    /**
     * Send a message to the other side.
     * Thread-safe: can be called from any thread.
     *
     * @param message The message to send (a subclass of LocalGameMessage)
     */
    public void sendMessage(LocalGameMessage message) {
        // If already closed, return immediately without throwing an exception (graceful degradation)
        if (closed) {
            logger.warning("[" + playerId + "] Tried to send message on closed connection, ignoring.");
            return;
        }

        synchronized (out) {
            // We synchronize on the ObjectOutputStream to ensure that messages are sent atomically and not interleaved when multiple threads call sendMessage concurrently.
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


//-----------------getter-----------------

    public String getPlayerId() {
        return playerId;
    }

    /**
     * Check if the connection is closed.
     * Used by the host to skip disconnected connections before broadcasting.
     */
    public boolean isClosed() {
        return closed || socket.isClosed();
    }
}