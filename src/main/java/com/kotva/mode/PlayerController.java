package com.kotva.mode;

import com.kotva.ai.AiMove;
import com.kotva.ai.AiMoveOptionSet;
import com.kotva.domain.action.PlayerAction;
import com.kotva.application.service.AiTurnAttemptResult;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.service.AiTurnCoordinator;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.SubmitDraftResult;
import com.kotva.application.service.TurnTransitionResult;
import com.kotva.application.session.GameSession;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.domain.model.Position;
import com.kotva.policy.PlayerType;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// Action source adapter for a player. It forwards player-intent events into the application service.
public class PlayerController {
    private final PlayerType type;
    private final String playerId;

    public PlayerController(String playerId, PlayerType type) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
        this.type = Objects.requireNonNull(type, "type cannot be null.");
    }

    public static PlayerController create(String playerId, PlayerType type) {
        Objects.requireNonNull(playerId, "playerId cannot be null.");
        Objects.requireNonNull(type, "type cannot be null.");

        return switch (type) {
            case LOCAL -> new LocalPlayerController(playerId);
            case LAN -> new LANPlayerController(playerId);
            case AI -> new AIPlayerController(playerId);
        };
    }

    public String getPlayerId() {
        return playerId;
    }

    public PlayerType getType() {
        return type;
    }
    public void assignLettertoBlank(GameApplicationService service, GameSession session, String tileId, char assignedLetter) {
        requireService(service).assignLettertoBlank(session, tileId, assignedLetter);
    }
    public PreviewResult placeDraftTile(
            GameApplicationService service, GameSession session, String tileId, Position position) {
        return requireService(service).placeDraftTile(session, tileId, position);
    }

    public PreviewResult moveDraftTile(
            GameApplicationService service, GameSession session, String tileId, Position newPosition) {
        return requireService(service).moveDraftTile(session, tileId, newPosition);
    }

    public PreviewResult removeDraftTile(
            GameApplicationService service, GameSession session, String tileId) {
        return requireService(service).removeDraftTile(session, tileId);
    }

    public PreviewResult recallAllDraftTiles(GameApplicationService service, GameSession session) {
        return requireService(service).recallAllDraftTiles(session);
    }

    public SubmitDraftResult submitDraft(GameApplicationService service, GameSession session) {
        return requireService(service).submitDraft(session);
    }

    public TurnTransitionResult passTurn(GameApplicationService service, GameSession session) {
        return requireService(service).passTurn(session);
    }

    public CommandEnvelope buildLanCommand(
            String sessionId, int expectedTurnNumber, PlayerAction action) {
        throw new UnsupportedOperationException("This player controller does not support LAN commands.");
    }

    public boolean supportsAutomatedTurn() {
        return false;
    }

    public CompletableFuture<AiMoveOptionSet> requestAutomatedTurn(
            AiTurnCoordinator aiTurnCoordinator, GameSession session) {
        throw new UnsupportedOperationException("This player controller does not support automated turns.");
    }

    public AiTurnAttemptResult applyAutomatedTurn(
            AiTurnCoordinator aiTurnCoordinator,
            GameApplicationService gameApplicationService,
            GameSession session,
            AiMove move) {
        throw new UnsupportedOperationException("This player controller does not support automated turns.");
    }

    private GameApplicationService requireService(GameApplicationService service) {
        return Objects.requireNonNull(service, "service cannot be null.");
    }
}
