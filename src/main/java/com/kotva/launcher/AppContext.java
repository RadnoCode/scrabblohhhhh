package com.kotva.launcher;

import com.kotva.application.runtime.GameRuntimeFactory;
import com.kotva.application.service.ClockService;
import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.GameSetupServiceImpl;
import com.kotva.application.service.SettlementService;
import com.kotva.application.service.SettlementServiceImpl;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.infrastructure.settings.SettingsRepository;
import java.util.Objects;
import java.util.Random;

public class AppContext {
    private final ClockService clockService;
    private final SettlementService settlementService;
    private final DictionaryRepository dictionaryRepository;
    private final SettingsRepository settingsRepository;
    private final GameApplicationService gameApplicationService;
    private final GameSetupService gameSetupService;
    private final GameRuntimeFactory gameRuntimeFactory;

    public AppContext() {
        this(
                new ClockServiceImpl(),
                new SettlementServiceImpl(),
                new DictionaryRepository(),
                new SettingsRepository(),
                new Random());
    }

    public AppContext(
            ClockService clockService,
            SettlementService settlementService,
            DictionaryRepository dictionaryRepository,
            SettingsRepository settingsRepository,
            Random random) {
        this.clockService = Objects.requireNonNull(clockService, "clockService cannot be null.");
        this.settlementService =
                Objects.requireNonNull(settlementService, "settlementService cannot be null.");
        this.dictionaryRepository =
                Objects.requireNonNull(dictionaryRepository, "dictionaryRepository cannot be null.");
        this.settingsRepository =
                Objects.requireNonNull(settingsRepository, "settingsRepository cannot be null.");
        Random nonNullRandom = Objects.requireNonNull(random, "random cannot be null.");
        this.gameApplicationService =
                new GameApplicationServiceImpl(this.clockService, this.dictionaryRepository);
        this.gameSetupService =
                new GameSetupServiceImpl(this.dictionaryRepository, this.clockService, nonNullRandom);
        this.gameRuntimeFactory = new GameRuntimeFactory(
                this.gameSetupService,
                this.gameApplicationService);
    }

    public ClockService getClockService() {
        return clockService;
    }

    public DictionaryRepository getDictionaryRepository() {
        return dictionaryRepository;
    }

    public SettingsRepository getSettingsRepository() {
        return settingsRepository;
    }

    public SettlementService getSettlementService() {
        return settlementService;
    }

    public GameRuntimeFactory getGameRuntimeFactory() {
        return gameRuntimeFactory;
    }

    public GameApplicationService getGameApplicationService() {
        return gameApplicationService;
    }

    public GameSetupService getGameSetupService() {
        return gameSetupService;
    }
}
