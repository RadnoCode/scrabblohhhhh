package com.kotva.infrastructure.settings;

public class AppSettings {
    public static final double DEFAULT_MUSIC_VOLUME = 1.0;
    public static final double DEFAULT_SFX_VOLUME = 1.0;
    public static final boolean DEFAULT_TUTORIAL_PROMPT_ACKNOWLEDGED = false;
    public static final boolean DEFAULT_TUTORIAL_COMPLETED = false;

    private final double musicVolume;
    private final double sfxVolume;
    private final boolean tutorialPromptAcknowledged;
    private final boolean tutorialCompleted;

    public AppSettings(double musicVolume, double sfxVolume) {
        this(
            musicVolume,
            sfxVolume,
            DEFAULT_TUTORIAL_PROMPT_ACKNOWLEDGED,
            DEFAULT_TUTORIAL_COMPLETED);
    }

    public AppSettings(
        double musicVolume,
        double sfxVolume,
        boolean tutorialPromptAcknowledged,
        boolean tutorialCompleted) {
        validateVolume("musicVolume", musicVolume);
        validateVolume("sfxVolume", sfxVolume);
        this.musicVolume = musicVolume;
        this.sfxVolume = sfxVolume;
        this.tutorialPromptAcknowledged = tutorialPromptAcknowledged;
        this.tutorialCompleted = tutorialCompleted;
    }

    public static AppSettings defaults() {
        return new AppSettings(
            DEFAULT_MUSIC_VOLUME,
            DEFAULT_SFX_VOLUME,
            DEFAULT_TUTORIAL_PROMPT_ACKNOWLEDGED,
            DEFAULT_TUTORIAL_COMPLETED);
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public boolean isTutorialPromptAcknowledged() {
        return tutorialPromptAcknowledged;
    }

    public boolean isTutorialCompleted() {
        return tutorialCompleted;
    }

    private static void validateVolume(String name, double volume) {
        if (Double.isNaN(volume) || Double.isInfinite(volume)) {
            throw new IllegalArgumentException(name + " must be a finite number.");
        }
        if (volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0.");
        }
    }
}
