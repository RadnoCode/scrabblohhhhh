package com.kotva.application.service;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.setup.NewGameRequest;

public interface GameSetupService {
    GameConfig buildConfig(NewGameRequest request);
    GameSession createSession(GameConfig config);
}
