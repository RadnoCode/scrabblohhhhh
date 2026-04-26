package com.kotva.lan.message;

import com.kotva.infrastructure.network.CommandEnvelope;
import java.util.Objects;

/**
 * Message sent from LAN client to host to request an action.
 */
public class CommandRequestMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final CommandEnvelope commandEnvelope;

    /**
     * Creates a command request message.
     *
     * @param commandEnvelope command data to send
     */
    public CommandRequestMessage(CommandEnvelope commandEnvelope) {
        super(MessageType.COMMAND_REQUEST);
        this.commandEnvelope = Objects.requireNonNull(commandEnvelope, "commandEnvelope cannot be null.");
    }

    /**
     * Gets the command envelope.
     *
     * @return command envelope
     */
    public CommandEnvelope getCommandEnvelope() {
        return commandEnvelope;
    }
}
