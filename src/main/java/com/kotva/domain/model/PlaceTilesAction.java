package com.kotva.domain.model;

import java.util.List;

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
