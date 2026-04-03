package com.kotva.application.service;

import java.util.ArrayList;
import java.util.UUID;

import com.kotva.application.draft.TurnDraft;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.RoundPassTracker;
import com.kotva.application.session.SessionStatus;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.GameState;

/**
 * GameSetupService is responsible for setting up a new game session based on the provided configuration. It takes a NewGameRequest, which contains the necessary information to configure the game, and builds a GameConfig object. It then creates a new GameSession using this configuration. This service abstracts away the details of how a game session is initialized and allows for different configurations to be easily applied when starting a new game.
 */
public class GameSetupServiceImpl implements GameSetupService {
    
    @Override
    public GameConfig buildConfig(NewGameRequest request) {
        // Build the GameConfig based on the NewGameRequest
        // This is a placeholder implementation and should be expanded based on actual requirements
        return new GameConfig(
            request.getGameMode(),
            request.getPlayerNames(),
            request.getBoardSize(),
            request.isTimeLimited()
        );
    }

    @Override
    public GameSession createSession(GameConfig config) {
        // Create a new GameSession based on the provided GameConfig
        String sessionId = UUID.randomUUID().toString(); // Implement a method to generate unique session 
        GameState initialGameState = new GameState(new ArrayList<>()); // Initialize the game state based on the config
        TurnDraft initialTurnDraft = new TurnDraft(); // Initialize an empty turn draft
    
        return new GameSession(
            sessionId,
            config,
            initialGameState,
            initialTurnDraft,
            SessionStatus.IN_PROGRESS,
            new ArrayList<>(),
            new RoundPassTracker()
        );
    }

}
