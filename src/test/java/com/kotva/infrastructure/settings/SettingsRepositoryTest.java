package com.kotva.infrastructure.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class SettingsRepositoryTest {

    @Test
    public void loadReturnsDefaultsWhenSettingsFileDoesNotExist() throws IOException {
        Path tempDirectory = Files.createTempDirectory("settings-repo-defaults");
        SettingsRepository repository = new SettingsRepository(tempDirectory.resolve("settings.properties"));

        AppSettings settings = repository.load();

        assertEquals(AppSettings.DEFAULT_MUSIC_VOLUME, settings.getMusicVolume(), 0.0);
        assertEquals(AppSettings.DEFAULT_SFX_VOLUME, settings.getSfxVolume(), 0.0);
        assertFalse(settings.isTutorialPromptAcknowledged());
        assertFalse(settings.isTutorialCompleted());
    }

    @Test
    public void saveAndLoadRoundTripsTutorialFlags() throws IOException {
        Path tempDirectory = Files.createTempDirectory("settings-repo-roundtrip");
        SettingsRepository repository = new SettingsRepository(tempDirectory.resolve("settings.properties"));
        AppSettings savedSettings = new AppSettings(0.3, 0.8, true, true);

        repository.save(savedSettings);
        AppSettings loadedSettings = repository.load();

        assertEquals(0.3, loadedSettings.getMusicVolume(), 0.0);
        assertEquals(0.8, loadedSettings.getSfxVolume(), 0.0);
        assertTrue(loadedSettings.isTutorialPromptAcknowledged());
        assertTrue(loadedSettings.isTutorialCompleted());
    }
}
