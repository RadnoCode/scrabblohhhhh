package com.kotva.runtime;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.session.GameSession;
import com.kotva.infrastructure.save.SaveGameRepository;
import java.util.Objects;

final class HotSeatGameRuntime extends AbstractLocalGameRuntime {
    private final SaveGameRepository saveGameRepository;

    HotSeatGameRuntime(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService) {
        this(gameSetupService, gameApplicationService, new SaveGameRepository());
    }

    HotSeatGameRuntime(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService,
        SaveGameRepository saveGameRepository) {
        super(gameSetupService, gameApplicationService);
        this.saveGameRepository = Objects.requireNonNull(
            saveGameRepository,
            "saveGameRepository cannot be null.");
    }

    HotSeatGameRuntime(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService,
        SaveGameRepository saveGameRepository,
        GameSession restoredSession) {
        this(gameSetupService, gameApplicationService, saveGameRepository);
        setSession(restoredSession);
    }

    @Override
    public boolean supportsSaveGame() {
        return true;
    }

    @Override
    public void saveGame() {
        saveGameRepository.saveHotSeat(requireSession(), getSessionSnapshot());
    }

    @Override
    public void loadGame() {
        setSession(saveGameRepository.loadHotSeat().getSession());
    }
}
