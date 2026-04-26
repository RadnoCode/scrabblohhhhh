package com.kotva.launcher;

import com.kotva.runtime.GameRuntimeFactory;
import com.kotva.runtime.TutorialRuntimeFactory;
import com.kotva.application.service.ClockService;
import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.GameSetupServiceImpl;
import com.kotva.application.service.SettlementService;
import com.kotva.application.service.SettlementServiceImpl;
import com.kotva.infrastructure.AudioManager;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.infrastructure.dictionary.TutorialDictionaryRepository;
import com.kotva.infrastructure.save.SaveGameRepository;
import com.kotva.infrastructure.settings.AppSettings;
import com.kotva.infrastructure.settings.SettingsRepository;
import java.util.Objects;
import java.util.Random;

/**
 * Holds shared services used by the application.
 */
public class AppContext {
    private final ClockService clockService;
    private final SettlementService settlementService;
    private final DictionaryRepository dictionaryRepository;
    private final SettingsRepository settingsRepository;
    private final SaveGameRepository saveGameRepository;
    private final GameApplicationService gameApplicationService;
    private final GameSetupService gameSetupService;
    private final GameRuntimeFactory gameRuntimeFactory;
    private final TutorialRuntimeFactory tutorialRuntimeFactory;
    private AudioManager audioManager;

    /**
     * Creates the default application context.
     */
    public AppContext() {
        this(
            new ClockServiceImpl(),
            new SettlementServiceImpl(),
            new DictionaryRepository(),
            new SettingsRepository(),
            new SaveGameRepository(),
            new Random());
    }

    /**
     * Creates an application context with core services.
     *
     * @param clockService clock service
     * @param settlementService settlement service
     * @param dictionaryRepository dictionary repository
     * @param settingsRepository settings repository
     * @param random random source for game setup
     */
    public AppContext(
        ClockService clockService,
        SettlementService settlementService,
        DictionaryRepository dictionaryRepository,
        SettingsRepository settingsRepository,
        Random random) {
        this(
            clockService,
            settlementService,
            dictionaryRepository,
            settingsRepository,
            new SaveGameRepository(),
            random);
    }

    /**
     * Creates an application context with all main services.
     *
     * @param clockService clock service
     * @param settlementService settlement service
     * @param dictionaryRepository dictionary repository
     * @param settingsRepository settings repository
     * @param saveGameRepository save game repository
     * @param random random source for game setup
     */
    public AppContext(
        ClockService clockService,
        SettlementService settlementService,
        DictionaryRepository dictionaryRepository,
        SettingsRepository settingsRepository,
        SaveGameRepository saveGameRepository,
        Random random) {
        this.clockService = Objects.requireNonNull(clockService, "clockService cannot be null.");
        this.settlementService =
            Objects.requireNonNull(settlementService, "settlementService cannot be null.");
        this.dictionaryRepository =
            Objects.requireNonNull(dictionaryRepository, "dictionaryRepository cannot be null.");
        this.settingsRepository =
            Objects.requireNonNull(settingsRepository, "settingsRepository cannot be null.");
        this.saveGameRepository =
            Objects.requireNonNull(saveGameRepository, "saveGameRepository cannot be null.");
        Random nonNullRandom = Objects.requireNonNull(random, "random cannot be null.");
        this.gameApplicationService =
            new GameApplicationServiceImpl(this.clockService, this.dictionaryRepository);
        this.gameSetupService =
            new GameSetupServiceImpl(this.dictionaryRepository, this.clockService, nonNullRandom);
        this.gameRuntimeFactory = new GameRuntimeFactory(
            this.gameSetupService,
            this.gameApplicationService,
            this.saveGameRepository);
        this.tutorialRuntimeFactory = new TutorialRuntimeFactory(
            new GameApplicationServiceImpl(
                this.clockService,
                new TutorialDictionaryRepository()));
    }

    /**
     * Returns the clock service.
     *
     * @return clock service
     */
    public ClockService getClockService() {
        return clockService;
    }

    /**
     * Returns the dictionary repository.
     *
     * @return dictionary repository
     */
    public DictionaryRepository getDictionaryRepository() {
        return dictionaryRepository;
    }

    /**
     * Returns the settings repository.
     *
     * @return settings repository
     */
    public SettingsRepository getSettingsRepository() {
        return settingsRepository;
    }

    /**
     * Returns the save game repository.
     *
     * @return save game repository
     */
    public SaveGameRepository getSaveGameRepository() {
        return saveGameRepository;
    }

    /**
     * Returns the settlement service.
     *
     * @return settlement service
     */
    public SettlementService getSettlementService() {
        return settlementService;
    }

    /**
     * Returns the game runtime factory.
     *
     * @return game runtime factory
     */
    public GameRuntimeFactory getGameRuntimeFactory() {
        return gameRuntimeFactory;
    }

    /**
     * Returns the game application service.
     *
     * @return game application service
     */
    public GameApplicationService getGameApplicationService() {
        return gameApplicationService;
    }

    /**
     * Returns the game setup service.
     *
     * @return game setup service
     */
    public GameSetupService getGameSetupService() {
        return gameSetupService;
    }

    /**
     * Returns the tutorial runtime factory.
     *
     * @return tutorial runtime factory
     */
    public TutorialRuntimeFactory getTutorialRuntimeFactory() {
        return tutorialRuntimeFactory;
    }

    /**
     * Returns the shared audio manager.
     *
     * @return audio manager
     */
    public AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = new AudioManager();
            AppSettings settings = settingsRepository.load();
            audioManager.setBGMVolume(settings.getMusicVolume());
            audioManager.setSFXVolume(settings.getSfxVolume());
        }
        return audioManager;
    }
}
