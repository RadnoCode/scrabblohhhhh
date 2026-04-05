package com.kotva.launcher;

import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.SettlementServiceImpl;
import com.kotva.application.service.ClockService;
import com.kotva.infrastructure.settings.SettingsRepository;

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
        this.SettlementService = new SettlementServiceImpl();
        //this.audioManager = new AudioManager();
    }
    private SceneNavigator sceneNavigator;
    private GameSetupService gameSetupService;
    private GameApplicationService gameApplicationService;
    private ClockService clockService;
    private SettlementService settlementService;
    private DictionaryRepository dictionaryRepository;
    private SettingsRepository settingsRepository;
    //TODO: Add audio service. private AudioManager audioManager; 
    //public AudioManager getAudioManager() {
    //    return audioManager;
    //}
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
