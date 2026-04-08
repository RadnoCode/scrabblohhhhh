package com.kotva.infrastructure.network;

import com.kotva.application.service.GameApplicationService;
import com.kotva.domain.action.PlayerAction;
import com.kotva.infrastructure.network.*;
import com.kotva.application.session.*
import com.kotva.application.draft.*;
public class RemoteClientService{

    @Override
    public RemoteCommandResult placeDraftTile(GameSession session, String tileId, Position position) {
        ensureEditingAllowed(session);
        draftManager.placeTile(session.getTurnDraft(), tileId, position);
        return refreshPreview(session);
    }

    @Override
    public RemoteCommandResult moveDraftTile(GameSession session, String tileId, Position newPosition) {
        ensureEditingAllowed(session);
        draftManager.moveTile(session.getTurnDraft(), tileId, newPosition);
        return refreshPreview(session);
    }

    @Override
    public RemoteCommandResult removeDraftTile(GameSession session, String tileId) {
        ensureEditingAllowed(session);
        draftManager.removeTile(session.getTurnDraft(), tileId);
        return refreshPreview(session);
    }

    @Override
    public PreviewResult recallAllDraftTiles(GameSession session) {
        ensureEditingAllowed(session);
        draftManager.recallAllTiles(session.getTurnDraft());
        return refreshPreview(session);
    }

    public PlayerAction createAction(String playerId, ActionType type, List<ActionPlacement> placements){
        switch(type){
            case ActionType.PLACE_TILE -> return PlayerAction.place(playerId, placements);
            case ActionType.PASS_TURN -> return PlayerAction.pass(playerId);
            case ActionType.LOSE -> return PlayerAction.lose(playerId);
            default -> throw new IllegalArgumentException("Unsupported action type: " + type);
        }
    }

    public SubmitDraftResult submitDraft(GameSession session) {
        Player currentPlayer = requireCurrentPlayer(session);
        PlayerAction action =
            TurnDraftActionMapper.toPlaceAction(currentPlayer.getPlayerId(), session.getTurnDraft());
    }
    public TurnTransitionResult passTurn(GameSession session) {
        Player currentPlayer = requireCurrentPlayer(session);
        PlayerAction action = PlayerAction.pass(currentPlayer.getPlayerId());
    }

}