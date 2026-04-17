package com.kotva.presentation.viewmodel;

public class HomeViewModel {
    private final String titleText;
    private final String playText;
    private final String tutorialText;
    private final String settingsText;
    private final String helpText;

    public HomeViewModel(
        String titleText,
        String playText,
        String tutorialText,
        String settingsText,
        String helpText) {
        this.titleText = titleText;
        this.playText = playText;
        this.tutorialText = tutorialText;
        this.settingsText = settingsText;
        this.helpText = helpText;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getPlayText() {
        return playText;
    }

    public String getTutorialText() {
        return tutorialText;
    }

    public String getSettingsText() {
        return settingsText;
    }

    public String getHelpText() {
        return helpText;
    }
}
