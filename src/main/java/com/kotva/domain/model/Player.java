package com.kotva.domain.model;

import com.kotva.mode.PlayerController;
import com.kotva.policy.PlayerType;
import java.util.Objects;

/**
 * Represents a participant in the Scrabble game.
 * * This class holds all the personal data for a player, including their
 * identity (ID and name), their current score, and their personal tile rack
 * (the letters they currently hold). It also tracks if they are still active
 * and what kind of controller (e.g., Human or AI) is making moves for them.
 */

public class Player {
    private final String playerId;
    private final String playerName;
    private int score;
    private final Rack rack;
    private boolean active;
    private PlayerController controller;
    private final PlayerType playerType;
    private PlayerClock clock;

    public Player(String playerId, String playerName, PlayerType playerType) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.playerType = playerType;
        this.score = 0;
        this.rack = new Rack();
        this.active = true;
        this.clock = PlayerClock.disabled();
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPlayerId(){
        return playerId;
    }

    public String getPlayerName(){
        return playerName;
    }

    public PlayerType getPlayerType(){
        return playerType;
    }

    public PlayerController getController() {
        return controller;
    }

    public int getScore(){
        return score;
    }

    public Rack getRack(){
        return rack;
    }

    public void addScore(int points){
        this.score += points;
    }

    public void setController(PlayerController controller) {
        this.controller = controller;
    }

    public PlayerClock getClock() {
        return clock;
    }

    public void setClock(PlayerClock clock) {
        this.clock = Objects.requireNonNull(clock, "clock cannot be null.");
    }
}
