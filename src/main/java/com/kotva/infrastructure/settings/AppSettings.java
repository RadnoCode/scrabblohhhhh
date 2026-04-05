package com.kotva.infrastructure.settings;

public class AppSettings {
    public static final double DEFAULT_MUSIC_VOLUME = 1.0;
    public static final double DEFAULT_SFX_VOLUME = 1.0;

    private final double musicVolume;
    private final double sfxVolume;

    public AppSettings(double musicVolume, double sfxVolume) {
        validateVolume("musicVolume", musicVolume);
        validateVolume("sfxVolume", sfxVolume);
        this.musicVolume = musicVolume;
        this.sfxVolume = sfxVolume;
    }

    public static AppSettings defaults() {
        return new AppSettings(DEFAULT_MUSIC_VOLUME, DEFAULT_SFX_VOLUME);
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public double getSfxVolume() {
        return sfxVolume;
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
