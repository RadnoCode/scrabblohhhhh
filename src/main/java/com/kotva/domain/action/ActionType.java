package com.kotva.domain.action;

/**
 * Lists the action types a player can send.
 */
public enum ActionType {
    /**
     * Places one or more tiles.
     */
    PLACE_TILE,
    /**
     * Ends the current turn without placing tiles.
     */
    PASS_TURN,
    /**
     * Resigns from the game.
     */
    LOSE,
}
