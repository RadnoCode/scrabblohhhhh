package com.kotva.application.service.client;

import com.kotva.application.preview.PreviewResult;
import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Position;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RemoteClientService {
    private final ClientGameContext context;
    private final ClientDraftService draftService;

    public RemoteClientService(ClientGameContext context) {
        this(context, new ClientDraftService(context));
    }

    public RemoteClientService(ClientGameContext context, ClientDraftService draftService) {
        this.context = Objects.requireNonNull(context, "context cannot be null.");
        this.draftService = Objects.requireNonNull(draftService, "draftService cannot be null.");
    }

    public PreviewResult placeDraftTile(String tileId, Position position) {
        ensureEditingAllowed();
        return draftService.placeDraftTile(tileId, position);
    }

    public PreviewResult moveDraftTile(String tileId, Position newPosition) {
        ensureEditingAllowed();
        return draftService.moveDraftTile(tileId, newPosition);
    }

    public PreviewResult removeDraftTile(String tileId) {
        ensureEditingAllowed();
        return draftService.removeDraftTile(tileId);
    }

    public PreviewResult recallAllDraftTiles() {
        ensureEditingAllowed();
        return draftService.recallAllDraftTiles();
    }

    public CommandEnvelope submitDraft() {
        ensureEditingAllowed();
        return buildCommand(draftService.buildSubmitAction());
    }

    public CommandEnvelope passTurn() {
        ensureEditingAllowed();
        return buildCommand(PlayerAction.pass(context.getLocalPlayerId()));
    }

    public PlayerAction createAction(ActionType type, List<ActionPlacement> placements) {
        Objects.requireNonNull(type, "type cannot be null.");
        return switch (type) {
            case PLACE_TILE -> PlayerAction.place(context.getLocalPlayerId(), placements);
            case PASS_TURN -> PlayerAction.pass(context.getLocalPlayerId());
            case LOSE -> PlayerAction.lose(context.getLocalPlayerId());
            default -> throw new IllegalArgumentException("Unsupported action type: " + type);
        };
    }

    public ClientDraftService getDraftService() {
        return draftService;
    }

    public void resetDraft() {
        draftService.resetDraft();
    }

    private CommandEnvelope buildCommand(PlayerAction action) {
        return new CommandEnvelope(
                UUID.randomUUID().toString(),
                context.getSessionId(),
                context.getLocalPlayerId(),
                context.getTurnNumber(),
                action);
    }

    private void ensureEditingAllowed() {
        if (context.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game session is not in progress.");
        }
        if (!context.isLocalPlayerTurn()) {
            throw new IllegalStateException("It is not this client player's turn.");
        }
    }
}
