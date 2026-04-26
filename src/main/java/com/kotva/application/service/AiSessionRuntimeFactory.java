package com.kotva.application.service;

import com.kotva.ai.QuackleNativeBridge;
import com.kotva.application.session.GameConfig;
import com.kotva.mode.GameMode;
import java.util.Objects;

/**
 * Creates AI runtimes backed by the Quackle native bridge.
 */
public final class AiSessionRuntimeFactory implements AiRuntimeBootstrapper {
    private final QuackleNativeBridge quackleNativeBridge;

    /**
     * Creates a factory.
     *
     * @param quackleNativeBridge native AI bridge
     */
    public AiSessionRuntimeFactory(QuackleNativeBridge quackleNativeBridge) {
        this.quackleNativeBridge =
        Objects.requireNonNull(quackleNativeBridge, "quackleNativeBridge cannot be null.");
    }

    /**
     * Creates an AI runtime when the game mode needs one.
     *
     * @param gameConfig game config
     * @return AI runtime, or {@code null} for non-AI games
     */
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

    /**
     * Gets the native library path.
     *
     * @return library path
     */
    @Override
    public String getLibraryPath() {
        return quackleNativeBridge.getLibraryPath().toString();
    }

    /**
     * Gets the AI data directory.
     *
     * @return data directory
     */
    @Override
    public String getDataDirectory() {
        return quackleNativeBridge.getDataDirectory().toString();
    }
}
