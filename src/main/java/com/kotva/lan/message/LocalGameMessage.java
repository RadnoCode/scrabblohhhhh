package com.kotva.lan.message;

import java.io.Serializable;

/**
 * Base class for messages sent between LAN client and host.
 */
public abstract class LocalGameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final MessageType type;

    /**
     * Creates a LAN game message.
     *
     * @param type message type
     */
    protected LocalGameMessage(MessageType type) {
        this.type = type;
    }

    /**
     * Gets the message type.
     *
     * @return message type
     */
    public MessageType getType() {
        return type;
    }
}
