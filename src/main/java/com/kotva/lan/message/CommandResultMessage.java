package com.kotva.lan.message;

import com.kotva.infrastructure.network.RemoteCommandResult;
import java.util.Objects;

/**
 * Message sent from host to client with the result of a command.
 */
public class CommandResultMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final RemoteCommandResult result;

    /**
     * Creates a command result message.
     *
     * @param result command result from host
     */
    public CommandResultMessage(RemoteCommandResult result) {
        super(MessageType.COMMAND_RESULT);
        this.result = Objects.requireNonNull(result, "result cannot be null.");
    }

    /**
     * Gets the command result.
     *
     * @return command result
     */
    public RemoteCommandResult getResult() {
        return result;
    }
}
