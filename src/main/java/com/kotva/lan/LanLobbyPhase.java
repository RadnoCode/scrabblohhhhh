package com.kotva.lan;

import java.io.Serializable;

/**
 * Current phase of a LAN lobby.
 */
public enum LanLobbyPhase implements Serializable {
    /** Players can still join and wait in the lobby. */
    WAITING_FOR_PLAYERS,
    /** The host has started the game. */
    STARTED,
    /** The lobby is closed and no longer accepts players. */
    CLOSED
}
