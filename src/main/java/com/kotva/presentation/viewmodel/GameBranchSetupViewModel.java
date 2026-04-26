package com.kotva.presentation.viewmodel;

/**
 * Stores text for a two-button setup screen.
 */
public class GameBranchSetupViewModel {
    private final String titleText;
    private final String viceTitleText;
    private final String firstOptionText;
    private final String secondOptionText;
    private final String thirdOptionText;

    public GameBranchSetupViewModel(
        String titleText,
        String viceTitleText,
        String firstOptionText,
        String secondOptionText,
        String thirdOptionText) {
        this.titleText = titleText;
        this.viceTitleText = viceTitleText;
        this.firstOptionText = firstOptionText;
        this.secondOptionText = secondOptionText;
        this.thirdOptionText = thirdOptionText;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getViceTitleText() {
        return viceTitleText;
    }

    public String getFirstOptionText() {
        return firstOptionText;
    }

    public String getSecondOptionText() {
        return secondOptionText;
    }

    public String getThirdOptionText() {
        return thirdOptionText;
    }
}
