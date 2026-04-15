package com.kotva.presentation.viewmodel;

public class SettingsViewModel {
    private String playerName;
    private double musicVolume;
    private final String userId;

    public SettingsViewModel(String playerName, double musicVolume, String userId) {
        this.playerName = playerName;
        this.musicVolume = musicVolume;
        this.userId = userId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
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