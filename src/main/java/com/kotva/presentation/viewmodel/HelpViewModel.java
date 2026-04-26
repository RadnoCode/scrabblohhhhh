package com.kotva.presentation.viewmodel;

/**
 * Stores data for the help screen.
 */
public class HelpViewModel {
    private final String titleText;
    private final String helpText;

    public HelpViewModel(String titleText, String helpText) {
        this.titleText = titleText;
        this.helpText = helpText;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getHelpText() {
        return helpText;
    }
}
