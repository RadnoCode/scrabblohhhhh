package com.kotva.lan.message;

import com.kotva.infrastructure.network.RemoteCommandResult;
import java.util.Objects;

public class CommandResultMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final RemoteCommandResult result;

    public CommandResultMessage(RemoteCommandResult result) {
        super(MessageType.COMMAND_RESULT);
        this.result = Objects.requireNonNull(result, "result cannot be null.");
    }

    public RemoteCommandResult getResult() {
        return result;
    }
}
