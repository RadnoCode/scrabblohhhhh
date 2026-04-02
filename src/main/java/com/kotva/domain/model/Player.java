package com.kotva.domain.model;

import com.kotva.mode.PlayerController;

public class Player {
    private String playerId;
    private String playerName;
    private int score;
    private Rack rack;
    private boolean active;
    private PlayerController controller;

    public Player(String playerId, String playerName){
        this.playerId = playerId;
        this.playerName = playerName;
        score = 0;
        this.rack = new Rack();
        this.active = true;
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

    public int getScore(){
        return score;
    }

    public Rack getRack(){
        return rack;
    }

    public void addScore(int points){
        this.score += points;
    }

    public PlayerController getController() {
        return controller;
    }

    public void setController(PlayerController controller) {
        this.controller = controller;
    }
}
