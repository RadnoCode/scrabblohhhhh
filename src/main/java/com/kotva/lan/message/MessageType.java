package com.kotva.lan.message;

public enum MessageType {
    //connection messages
    JOIN_SESSION,  //client -> server, request to join a session
    GAME_INITIALIZATION, //server -> client, sent a snapshot of the game state when a client joins a session

    //waiting room messages
    LOBBY_STATE,
    PLAYER_JOINED,  //server -> client, notify other clients that a new player has joined the session
    GAME_START,

    //gameplay messages
    COMMAND_REQUEST,
    COMMAND_RESULT,
    SNAPSHOT_UPDATE,
    PLAYER_ACTION,  //client -> server, sent when a player performs an action

    //connection messages
    GAME_OVER,  //server -> client, sent when the game ends
    PLAYER_DISCONNECTED,  //server -> client, sent when a player disconnects from the session
}
