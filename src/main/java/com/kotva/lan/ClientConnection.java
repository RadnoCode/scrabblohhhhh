package com.kotva.lan;

import com.kotva.lan.message.LocalGameMessage;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Wraps a TCP socket and object streams for LAN messages.
 */
public class ClientConnection {
    private static final Logger logger = Logger.getLogger(ClientConnection.class.getName());

    private final String playerId;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private volatile boolean closed = false;

    /**
     * Creates a connection and opens object streams.
     *
     * @param playerId player id for this connection
     * @param socket TCP socket
     * @throws IOException if streams cannot be opened
     */
    public ClientConnection(String playerId, Socket socket) throws IOException {
        this(playerId, socket, createStreams(socket));
    }

    /**
     * Creates a connection with an existing input stream.
     *
     * @param playerId player id for this connection
     * @param socket TCP socket
     * @param in object input stream
     * @throws IOException if output stream cannot be opened
     */
    public ClientConnection(String playerId, Socket socket, ObjectInputStream in) throws IOException {
        this(playerId, socket, in, createOutputStream(socket));
    }

    /**
     * Creates a connection from a stream pair.
     *
     * @param playerId player id for this connection
     * @param socket TCP socket
     * @param streamPair input and output streams
     */
    private ClientConnection(String playerId, Socket socket, StreamPair streamPair) {
        this(playerId, socket, streamPair.in(), streamPair.out());
    }

    /**
     * Creates a connection with explicit object streams.
     *
     * @param playerId player id for this connection
     * @param socket TCP socket
     * @param in object input stream
     * @param out object output stream
     */
    public ClientConnection(
            String playerId,
            Socket socket,
            ObjectInputStream in,
            ObjectOutputStream out) {
        this.playerId = playerId;
        this.socket = socket;
        this.in = Objects.requireNonNull(in, "in cannot be null.");
        this.out = Objects.requireNonNull(out, "out cannot be null.");
    }

    /**
     * Blocks until one LAN message is read.
     *
     * @return received LAN message
     * @throws IOException if the socket fails
     * @throws ClassNotFoundException if the received class is unknown
     */
    public LocalGameMessage readMessageBlocking() throws IOException, ClassNotFoundException {
        return (LocalGameMessage) in.readObject();
    }

    /**
     * Starts a background thread that reads incoming messages.
     *
     * @param onMessage callback for each received message
     * @param onDisconnect callback when the connection ends
     */
    public void startListening(Consumer<LocalGameMessage> onMessage, Runnable onDisconnect) {
        Thread listenerThread = new Thread(() -> {
            try {
                while (!closed && !socket.isClosed()) {
                    LocalGameMessage message = readMessageBlocking();
                    onMessage.accept(message);
                }
            } catch (EOFException exception) {
                if (!closed) {
                    logger.info("[" + playerId + "] Remote closed connection normally.");
                }
            } catch (SocketException exception) {
                if (!closed) {
                    logger.warning("[" + playerId + "] Socket closed unexpectedly: " + exception.getMessage());
                }
            } catch (ClassNotFoundException exception) {
                logger.severe("[" + playerId + "] Unknown message class received: " + exception.getMessage());
            } catch (IOException exception) {
                if (!closed) {
                    logger.warning("[" + playerId + "] I/O error: " + exception.getMessage());
                }
            } finally {
                onDisconnect.run();
            }
        }, "ClientConnection-" + playerId);

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Sends a message to the remote side.
     *
     * @param message message to send
     */
    public void sendMessage(LocalGameMessage message) {
        if (closed) {
            logger.warning("[" + playerId + "] Tried to send message on closed connection, ignoring.");
            return;
        }

        synchronized (out) {
            try {
                out.writeObject(message);
                out.flush();
                out.reset();
            } catch (IOException exception) {
                logger.warning("[" + playerId + "] Failed to send message: " + exception.getMessage());
            }
        }
    }

    /**
     * Closes the socket connection.
     */
    public void disconnect() {
        closed = true;
        try {
            socket.close();
        } catch (IOException exception) {
            logger.warning("[" + playerId + "] Error while closing socket: " + exception.getMessage());
        }
    }

    /**
     * Gets the player id for this connection.
     *
     * @return player id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Checks whether the connection is closed.
     *
     * @return {@code true} if closed
     */
    public boolean isClosed() {
        return closed || socket.isClosed();
    }

    /**
     * Creates and flushes an object output stream.
     *
     * @param socket TCP socket
     * @return object output stream
     * @throws IOException if the stream cannot be created
     */
    private static ObjectOutputStream createOutputStream(Socket socket) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        return outputStream;
    }

    /**
     * Creates object streams for a socket.
     *
     * @param socket TCP socket
     * @return stream pair
     * @throws IOException if streams cannot be created
     */
    private static StreamPair createStreams(Socket socket) throws IOException {
        ObjectOutputStream out = createOutputStream(socket);
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        return new StreamPair(in, out);
    }

    /**
     * Pair of object input and output streams.
     *
     * @param in object input stream
     * @param out object output stream
     */
    private record StreamPair(ObjectInputStream in, ObjectOutputStream out) {
    }
}
