package com.kotva.presentation.viewmodel;

/**
 * Stores data for setup screens.
 */
public class SetupViewModel {
    private final String titleText;
    private final String withFriendsText;
    private final String withRobotText;
    private final String byLanText;

    public SetupViewModel(String titleText, String withFriendsText, String withRobotText, String byLanText) {
        this.titleText = titleText;
        this.withFriendsText = withFriendsText;
        this.withRobotText = withRobotText;
        this.byLanText = byLanText;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getWithFriendsText() {
        return withFriendsText;
    }

    public String getWithRobotText() {
        return withRobotText;
    }

    public String getByLanText() {
        return byLanText;
    }
}
