package com.kotva.launcher;

import com.kotva.application.GameSetupService;
import com.kotva.infrastructure.dictionary.DictionaryRepository;

public class AppContext {
    AppContext() {
        this.sceneNavigator = new SceneNavigator();
        this.gameSetupService = new GameSetupService();
        this.gameApplicationService = new GameApplicationService();
        this.clockService = new ClockService();
        this.settlementService = new SettlementService();
        this.dictionaryRepository = new DictionaryRepository();
        this.settingsRepository = new SettingsRepository();
        this.audioManager = new AudioManager();
    }
    private SceneNavigator sceneNavigator;
    private GameSetupService gameSetupService;
    private GameApplicationService gameApplicationService;
    private ClockService clockService;
    private SettlementService settlementService;
    private DictionaryRepository dictionaryRepository;
    private SettingsRepository settingsRepository;
    private AudioManager audioManager;
    public AudioManager getAudioManager() {
        return audioManager;
    }
    public ClockService getClockService() {
        return clockService;
    }
    public DictionaryRepository getDictionaryRepository() {
        return dictionaryRepository;
    }
    public GameApplicationService getGameApplicationService() {
        return gameApplicationService;
    }
    public GameSetupService getGameSetupService() {
        return gameSetupService;
    }
    public SceneNavigator getSceneNavigator() {
        return sceneNavigator;
    }
    public SettingsRepository getSettingsRepository() {
        return settingsRepository;
    }
    public SettlementService getSettlementService() {
        return settlementService;
    }
}
