package com.kotva.domain.model;

import java.util.Arrays;
import java.util.List;

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

    public List<RackSlot> getSlots() {
        return Arrays.asList(slots.clone());
    }

    public boolean isEmpty() {
        for (RackSlot slot : slots) {
            if (!slot.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
