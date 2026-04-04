package com.kotva.domain.model;

import com.kotva.mode.PlayerController;
import com.kotva.policy.PlayerType;
import com.kotva.mode.PlayerController;

/**
 * Represents a participant in the Scrabble game.
 * * This class holds all the personal data for a player, including their
 * identity (ID and name), their current score, and their personal tile rack
 * (the letters they currently hold). It also tracks if they are still active
 * and what kind of controller (e.g., Human or AI) is making moves for them.
 */

public class Player {
    private String playerId;
    private String playerName;
    private int score;
    private Rack rack;
    private boolean active;
    private PlayerController controller;
    private PlayerType playerType;

    public Player(String playerId, String playerName, PlayerType playerType) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.playerType = playerType;
        score = 0;
        this.rack = new Rack();
        this.active = true;
        this.controller = new PlayerController(playerId, playerType);
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
}
