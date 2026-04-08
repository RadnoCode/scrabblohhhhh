package com.kotva.presentation.viewmodel;

/**
 * RoomViewModel stores the lightweight display data used by online room pages.
 * For now it mainly keeps text used by the search page and waiting page.
 */
public class RoomViewModel {
    private final String titleText;
    private final String searchPromptText;
    private final String waitingHintText;

    public RoomViewModel(String titleText, String searchPromptText, String waitingHintText) {
        this.titleText = titleText;
        this.searchPromptText = searchPromptText;
        this.waitingHintText = waitingHintText;
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
}
