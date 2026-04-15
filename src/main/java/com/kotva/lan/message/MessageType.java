package com.kotva.lan.message;

public enum MessageType {
    JOIN_SESSION,  //client -> server, request to join a session
    GAME_INITIALIZATION, //server -> client, sent a snapshot of the game state when a client joins a session

    PLAYER_JOINED,  //server -> client, notify other clients that a new player has joined the session

    PLAYER_ACTION,  //client -> server, sent when a player performs an action

    GAME_OVER,  //server -> client, sent when the game ends
    PLAYER_DISCONNECTED,  //server -> client, sent when a player disconnects from the session
}