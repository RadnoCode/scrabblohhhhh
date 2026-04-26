package com.kotva.application.service;

import com.kotva.application.session.GameConfig;

/**
 * Creates AI turn runtimes and exposes native AI paths.
 */
public interface AiRuntimeBootstrapper {

    /**
     * Creates an AI turn runtime for a game.
     *
     * @param gameConfig game config
     * @return AI turn runtime
     */
    AiTurnRuntime create(GameConfig gameConfig);

    /**
     * Gets the native AI library path.
     *
     * @return library path
     */
    String getLibraryPath();

    /**
     * Gets the AI data directory.
     *
     * @return data directory
     */
    String getDataDirectory();

    /**
     * Gets the current platform name.
     *
     * @return platform name
     */
    default String getPlatform() {
        return System.getProperty("os.name", "unknown");
    }
}
