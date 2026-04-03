package com.kotva.domain.model;

/**
 * A player's personal tile holder.
 * * This class represents the rack where a player keeps their unplayed
 * letter tiles. It is fixed to exactly 7 slots, which are set up automatically
 * when the rack is created.
 */
public class RackSlot {
    private int index;
    private Tile tile;
    public RackSlot(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public Tile getTile(){
        return tile;
    }

    public void setTile(Tile tile){
        this.tile = tile;
    }

    public void clearSlot(){
        tile = null;
    }

    public boolean isEmpty() {
        return tile == null;
    }

}
