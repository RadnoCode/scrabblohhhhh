package com.kotva.lan.message;

import com.kotva.infrastructure.network.CommandEnvelope;
import java.util.Objects;

public class CommandRequestMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final CommandEnvelope commandEnvelope;

    public CommandRequestMessage(CommandEnvelope commandEnvelope) {
        super(MessageType.COMMAND_REQUEST);
        this.commandEnvelope = Objects.requireNonNull(commandEnvelope, "commandEnvelope cannot be null.");
    }

    public CommandEnvelope getCommandEnvelope() {
        return commandEnvelope;
    }
}
