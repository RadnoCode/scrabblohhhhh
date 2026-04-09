package com.kotva.mode;

import java.util.Objects;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.SubmitDraftResult;
import com.kotva.application.service.TurnTransitionResult;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Position;
import com.kotva.policy.PlayerType;

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
            case LAN -> throw new IllegalArgumentException("LAN player type is not supported on this branch.");
            case AI -> new AIPlayerController(playerId);
        };
    }

    public String getPlayerId() {
        return playerId;
    }

    public PlayerType getType() {
        return type;
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

    private GameApplicationService requireService(GameApplicationService service) {
        return Objects.requireNonNull(service, "service cannot be null.");
    }
}
