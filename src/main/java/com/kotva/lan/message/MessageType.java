package com.kotva.lan.message;

/**
 * Types of messages exchanged between LAN client and host.
 */
public enum MessageType {
    /** Client requests to join a session. */
    JOIN_SESSION,
    /** Host sends initial session information to a client. */
    GAME_INITIALIZATION,
    /** Host sends the latest lobby state. */
    LOBBY_STATE,
    /** Host notifies clients that a player joined. */
    PLAYER_JOINED,
    /** Host notifies clients that a player disconnected. */
    PLAYER_DISCONNECTED,
    /** Host starts the game for clients. */
    GAME_START,
    /** Client sends a gameplay command to the host. */
    COMMAND_REQUEST,
    /** Host sends command execution result to a client. */
    COMMAND_RESULT,
    /** Host sends an updated game snapshot. */
    SNAPSHOT_UPDATE,
    /** Client sends a player action. */
    PLAYER_ACTION,
    /** Host notifies clients that the game ended. */
    GAME_OVER,
}
