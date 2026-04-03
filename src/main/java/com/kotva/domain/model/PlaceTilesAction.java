package com.kotva.domain.model;

import java.util.List;

/**
 * The action of a player placing tiles on the board.
 * * This class captures exactly what a player wants to do on their turn:
 * it records WHO is making the move (playerId) and exactly WHAT tiles
 * they are putting down and WHERE (the list of placements).
 */
public class PlaceTilesAction implements PlayerAction {
    private String playerId;
    private List<TilePlacement> placements;

    public PlaceTilesAction(String playerId,List<TilePlacement> placements){
        this.playerId = playerId;
        this.placements = placements;
    }
    public String getPlayerId(){
        return playerId;
    }
    public List<TilePlacement> getPlacements(){
        return placements;
    }

}
