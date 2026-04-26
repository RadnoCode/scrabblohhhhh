package com.kotva.lan;

import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.LanClientTransport;
import com.kotva.infrastructure.network.LanCommandResultMessage;
import com.kotva.infrastructure.network.LanDisconnectNoticeMessage;
import com.kotva.infrastructure.network.LanInboundMessage;
import com.kotva.infrastructure.network.LanSnapshotMessage;
import com.kotva.lan.message.CommandRequestMessage;
import com.kotva.lan.message.CommandResultMessage;
import com.kotva.lan.message.LocalGameMessage;
import com.kotva.lan.message.PlayerDisconnectedMessage;
import com.kotva.lan.message.SnapshotUpdateMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LAN client transport backed by a socket client connection.
 */
public class SocketLanClientTransport implements LanClientTransport {
    private final ClientConnection connection;
    private final Queue<LanInboundMessage> inboundMessages;
    private final AtomicBoolean closing;
    private final AtomicBoolean disconnectQueued;

    /**
     * Creates a socket transport.
     *
     * @param connection socket connection to the host
     */
    public SocketLanClientTransport(ClientConnection connection) {
        this.connection = connection;
        this.inboundMessages = new ConcurrentLinkedQueue<>();
        this.closing = new AtomicBoolean(false);
        this.disconnectQueued = new AtomicBoolean(false);
    }

    /**
     * Sends a command to the host.
     *
     * @param commandEnvelope command envelope
     */
    @Override
    public void sendCommand(CommandEnvelope commandEnvelope) {
        connection.sendMessage(new CommandRequestMessage(commandEnvelope));
    }

    /**
     * Drains inbound messages received from the socket listener.
     *
     * @return inbound message list
     */
    @Override
    public List<LanInboundMessage> drainInboundMessages() {
        List<LanInboundMessage> drainedMessages = new ArrayList<>();
        LanInboundMessage inboundMessage;
        while ((inboundMessage = inboundMessages.poll()) != null) {
            drainedMessages.add(inboundMessage);
        }
        return drainedMessages;
    }

    /**
     * Converts a network message into an infrastructure inbound message.
     *
     * @param message network message
     */
    public void onNetworkMessage(LocalGameMessage message) {
        if (message instanceof CommandResultMessage commandResultMessage) {
            inboundMessages.add(new LanCommandResultMessage(commandResultMessage.getResult()));
        } else if (message instanceof SnapshotUpdateMessage snapshotUpdateMessage) {
            inboundMessages.add(new LanSnapshotMessage(snapshotUpdateMessage.getSnapshot()));
        } else if (message instanceof PlayerDisconnectedMessage disconnectedMessage) {
            queueDisconnectNotice(
                    disconnectedMessage.getPlayerName().isBlank()
                            ? "A player disconnected."
                            : disconnectedMessage.getPlayerName() + " disconnected.",
                    disconnectedMessage.getReason().isBlank()
                            ? "Remote player disconnected. The LAN match cannot continue."
                            : disconnectedMessage.getReason());
        }
    }

    /**
     * Handles socket disconnection.
     */
    public void onDisconnect() {
        if (!closing.get()) {
            queueDisconnectNotice(
                    "Connection lost to host.",
                    "Lost TCP connection to host.");
        }
        connection.disconnect();
    }

    /**
     * Closes the transport.
     */
    @Override
    public void close() {
        closing.set(true);
        connection.disconnect();
    }

    /**
     * Queues one disconnect notice.
     *
     * @param summary short message
     * @param details detailed message
     */
    private void queueDisconnectNotice(String summary, String details) {
        if (disconnectQueued.compareAndSet(false, true)) {
            inboundMessages.add(new LanDisconnectNoticeMessage(summary, details));
        }
    }
}
