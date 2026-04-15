package com.kotva.lan.message;

import java.io.Serializable;

/**
 * the base class for all messages sent between client and server in the LAN version of the game.
*/
public abstract class LocalGameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final MessageType type;

    protected LocalGameMessage(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }
}
