package com.kotva.domain.model;

/**
 * A player's personal tile holder.
 * * This class represents the wooden rack where a player keeps their unplayed
 * letter tiles. It is fixed to exactly 7 slots, which are set up automatically
 * when the rack is created.
 */
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
