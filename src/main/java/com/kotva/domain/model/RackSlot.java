package com.kotva.domain.model;

import java.io.Serializable;

public class RackSlot implements Serializable {
    private static final long serialVersionUID = 1L;

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
