package com.kotva.domain.model;

/**
 * Represents a single tile being placed on the board during a move.
 * * This class links a specific tile (tileId) to a target spot (position).
 * If it's a blank tile, it also stores the letter chosen by the player
 * (assignedLetter).
 */
public class TilePlacement {
    private String tileId;
    private Position position;
    private Character assignedLetter;

    public TilePlacement(String tileId, Position position,Character assignedLetter){
        this.tileId = tileId;
        this.position = position;
        this.assignedLetter = assignedLetter;
    }
    public String getTileId(){
        return tileId;
    }
    public Position getPosition(){
        return position;
    }
    public Character getAssignedLetter(){
        return  assignedLetter;
    }
}
