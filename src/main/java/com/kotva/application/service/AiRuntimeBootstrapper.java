package com.kotva.application.service;

import com.kotva.application.session.GameConfig;

/**
 * Creates AI runtime support for a game when needed.
 */
public interface AiRuntimeBootstrapper {

    /**
     * Creates an AI turn runtime for the given game config.
     *
     * @param gameConfig game configuration
     * @return AI runtime, or null when the game does not use AI
     */
    AiTurnRuntime create(GameConfig gameConfig);

    /**
     * Returns the native library path.
     *
     * @return native library path
     */
    String getLibraryPath();

    /**
     * Returns the AI data directory.
     *
     * @return AI data directory
     */
    String getDataDirectory();

    /**
     * Returns the current platform name.
     *
     * @return platform name
     */
    default String getPlatform() {
        return System.getProperty("os.name", "unknown");
    }
}
