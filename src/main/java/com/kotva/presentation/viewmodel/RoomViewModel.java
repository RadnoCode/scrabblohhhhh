package com.kotva.presentation.viewmodel;

import com.kotva.lan.udp.DiscoveredRoom;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomViewModel stores the display data used by LAN room pages.
 */
public class RoomViewModel {
    private final String titleText;
    private final String searchPromptText;
    private final String waitingHintText;

    private List<DiscoveredRoom> rooms;
    private String statusText;
    private String selectedRoomText;
    private boolean scanning;

    public RoomViewModel(String titleText, String searchPromptText, String waitingHintText) {
        this.titleText = titleText;
        this.searchPromptText = searchPromptText;
        this.waitingHintText = waitingHintText;
        this.rooms = new ArrayList<>();
        this.statusText = "";
        this.selectedRoomText = "";
        this.scanning = false;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getSearchPromptText() {
        return searchPromptText;
    }

    public String getWaitingHintText() {
        return waitingHintText;
    }

    public List<DiscoveredRoom> getRooms() {
        return List.copyOf(rooms);
    }

    public void setRooms(List<DiscoveredRoom> rooms) {
        this.rooms = new ArrayList<>(rooms);
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText == null ? "" : statusText;
    }

    public String getSelectedRoomText() {
        return selectedRoomText;
    }

    public void setSelectedRoomText(String selectedRoomText) {
        this.selectedRoomText = selectedRoomText == null ? "" : selectedRoomText;
    }

    public boolean isScanning() {
        return scanning;
    }

    public void setScanning(boolean scanning) {
        this.scanning = scanning;
    }
}
