package com.kotva.domain.model;

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
