package com.kotva.presentation.viewmodel;

public class SettingsViewModel {
    private String playerName;
    private double soundEffectVolume;
    private final String userId;

    public SettingsViewModel(String playerName, double soundEffectVolume, String userId) {
        this.playerName = playerName;
        this.soundEffectVolume = soundEffectVolume;
        this.userId = userId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public double getSoundEffectVolume() {
        return soundEffectVolume;
    }

    public void setSoundEffectVolume(double soundEffectVolume) {
        this.soundEffectVolume = soundEffectVolume;
    }

    public String getUserId() {
        return userId;
    }
}
