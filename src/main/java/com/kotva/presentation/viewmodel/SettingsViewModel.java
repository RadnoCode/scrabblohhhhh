package com.kotva.presentation.viewmodel;

/**
 * SettingsViewModel stores editable and display values for the settings page.
 */
public class SettingsViewModel {
    private String playerName;
    private final String[] languages;
    private int currentLanguageIndex;
    private double musicVolume;
    private final String userId;

    public SettingsViewModel(String playerName, String[] languages, int currentLanguageIndex, double musicVolume, String userId) {
        this.playerName = playerName;
        this.languages = languages;
        this.currentLanguageIndex = currentLanguageIndex;
        this.musicVolume = musicVolume;
        this.userId = userId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getCurrentLanguage() {
        return languages[currentLanguageIndex];
    }

    public String rotateLanguage() {
        currentLanguageIndex = (currentLanguageIndex + 1) % languages.length;
        return getCurrentLanguage();
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(double musicVolume) {
        this.musicVolume = musicVolume;
    }

    public String getUserId() {
        return userId;
    }
}
