package com.kotva.infrastructure.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SettingsRepository {
    private static final String MUSIC_VOLUME_KEY = "musicVolume";
    private static final String SFX_VOLUME_KEY = "sfxVolume";

    private final Path storagePath;

    public SettingsRepository() {
        this(defaultStoragePath());
    }

    public SettingsRepository(Path storagePath) {
        if (storagePath == null) {
            throw new IllegalArgumentException("storagePath cannot be null.");
        }
        this.storagePath = storagePath;
    }

    public AppSettings load() {
        if (Files.notExists(storagePath)) {
            return AppSettings.defaults();
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return new AppSettings(
                readVolume(properties, MUSIC_VOLUME_KEY, AppSettings.DEFAULT_MUSIC_VOLUME),
                readVolume(properties, SFX_VOLUME_KEY, AppSettings.DEFAULT_SFX_VOLUME));
        } catch (IOException e) {
            return AppSettings.defaults();
        } catch (IllegalArgumentException e) {
            return AppSettings.defaults();
        }
    }

    public void save(AppSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings cannot be null.");
        }

        try {
            Path parent = storagePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Properties properties = new Properties();
            properties.setProperty(MUSIC_VOLUME_KEY, Double.toString(settings.getMusicVolume()));
            properties.setProperty(SFX_VOLUME_KEY, Double.toString(settings.getSfxVolume()));

            try (Writer writer = Files.newBufferedWriter(storagePath, StandardCharsets.UTF_8)) {
                properties.store(writer, "settings");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save settings to " + storagePath, e);
        }
    }

    static Path defaultStoragePath() {
        return Path.of(
            System.getProperty("user.home"),
            ".scrabblohhhhh",
            "settings.properties");
    }

    private double readVolume(Properties properties, String key, double defaultValue) {
        String rawValue = properties.getProperty(key);
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        return Double.parseDouble(rawValue.trim());
    }
}