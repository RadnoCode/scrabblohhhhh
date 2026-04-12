package com.kotva.application.service;

import com.kotva.application.session.GameConfig;

public interface AiRuntimeBootstrapper {
    AiTurnRuntime create(GameConfig gameConfig);

    String getLibraryPath();

    String getDataDirectory();

    default String getPlatform() {
        return System.getProperty("os.name", "unknown");
    }
}
