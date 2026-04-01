package com.kotva.domain.model;

public class Rack {

    private final RackSlot[] slots;

    public Rack(){
        slots = new RackSlot[7];

        for(int i=0;i<slots.length;i++){
            slots[i] = new RackSlot(i);
        }
    }
    public void setTileAt(int index, Tile tile) {
        slots[index].setTile(tile);
    }

}
