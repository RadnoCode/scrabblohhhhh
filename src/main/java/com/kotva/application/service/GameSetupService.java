package com.kotva.application.service;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.setup.NewGameRequest;

/**
 * Builds game configuration and starts new sessions.
 */
public interface GameSetupService {

    /**
     * Builds a game config from setup input.
     *
     * @param request setup request
     * @return game config
     */
    GameConfig buildConfig(NewGameRequest request);

    /**
     * Starts a new game from setup input.
     *
     * @param request setup request
     * @return game session
     */
    GameSession startNewGame(NewGameRequest request);

    /**
     * Starts a new game from an existing config.
     *
     * @param config game config
     * @return game session
     */
    GameSession startNewGame(GameConfig config);
}
