package com.kotva.lan;

import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.LanClientTransport;
import com.kotva.infrastructure.network.LanCommandResultMessage;
import com.kotva.infrastructure.network.LanInboundMessage;
import com.kotva.infrastructure.network.LanSnapshotMessage;
import com.kotva.lan.message.CommandRequestMessage;
import com.kotva.lan.message.CommandResultMessage;
import com.kotva.lan.message.LocalGameMessage;
import com.kotva.lan.message.SnapshotUpdateMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketLanClientTransport implements LanClientTransport {
    private final ClientConnection connection;
    private final Queue<LanInboundMessage> inboundMessages;

    public SocketLanClientTransport(ClientConnection connection) {
        this.connection = connection;
        this.inboundMessages = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendCommand(CommandEnvelope commandEnvelope) {
        connection.sendMessage(new CommandRequestMessage(commandEnvelope));
    }

    @Override
    public List<LanInboundMessage> drainInboundMessages() {
        List<LanInboundMessage> drainedMessages = new ArrayList<>();
        LanInboundMessage inboundMessage;
        while ((inboundMessage = inboundMessages.poll()) != null) {
            drainedMessages.add(inboundMessage);
        }
        return drainedMessages;
    }

    public void onNetworkMessage(LocalGameMessage message) {
        if (message instanceof CommandResultMessage commandResultMessage) {
            inboundMessages.add(new LanCommandResultMessage(commandResultMessage.getResult()));
        } else if (message instanceof SnapshotUpdateMessage snapshotUpdateMessage) {
            inboundMessages.add(new LanSnapshotMessage(snapshotUpdateMessage.getSnapshot()));
        }
    }

    public void onDisconnect() {
        connection.disconnect();
    }

    @Override
    public void close() {
        connection.disconnect();
    }
}
