package com.kotva.presentation.viewmodel;

public class HomeViewModel {
    private final String titleText;
    private final String playText;
    private final String settingsText;
    private final String helpText;

    public HomeViewModel(String titleText, String playText, String settingsText, String helpText) {
        this.titleText = titleText;
        this.playText = playText;
        this.settingsText = settingsText;
        this.helpText = helpText;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getPlayText() {
        return playText;
    }

    public String getSettingsText() {
        return settingsText;
    }

    public String getHelpText() {
        return helpText;
    }
}