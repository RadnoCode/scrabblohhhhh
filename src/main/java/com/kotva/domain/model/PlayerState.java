package com.kotva.domain.model;

public class PlayerState {
    private String playerId;
    private String playerName;
    private int score;
    private Rack rack;

    public PlayerState(String playerId, String playerName){
        this.playerId = playerId;
        this.playerName = playerName;
        score = 0;
        this.rack = new Rack();
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
}
