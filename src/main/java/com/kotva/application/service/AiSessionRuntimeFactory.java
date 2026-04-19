package com.kotva.application.service;

import com.kotva.ai.QuackleNativeBridge;
import com.kotva.application.session.GameConfig;
import com.kotva.mode.GameMode;
import java.util.Objects;

public final class AiSessionRuntimeFactory implements AiRuntimeBootstrapper {
    private final QuackleNativeBridge quackleNativeBridge;

    public AiSessionRuntimeFactory(QuackleNativeBridge quackleNativeBridge) {
        this.quackleNativeBridge =
                Objects.requireNonNull(quackleNativeBridge, "quackleNativeBridge cannot be null.");
    }

    public AiSessionRuntime create(GameConfig gameConfig) {
        Objects.requireNonNull(gameConfig, "gameConfig cannot be null.");
        if (gameConfig.getGameMode() != GameMode.HUMAN_VS_AI) {
            return null;
        }
        if (gameConfig.getAiDifficulty() == null) {
            throw new IllegalStateException("AI games require an aiDifficulty.");
        }
        quackleNativeBridge.load();

        return new AiSessionRuntime(new AiTurnCoordinator(
                quackleNativeBridge,
                gameConfig.getDictionaryType(),
                gameConfig.getAiDifficulty()));
    }

    @Override
    public String getLibraryPath() {
        return quackleNativeBridge.getLibraryPath().toString();
    }

    @Override
    public String getDataDirectory() {
        return quackleNativeBridge.getDataDirectory().toString();
    }
}
