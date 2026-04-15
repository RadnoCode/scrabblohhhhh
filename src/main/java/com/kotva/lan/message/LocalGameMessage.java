package com.kotva.lan.message;

import java.io.Serializable;

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