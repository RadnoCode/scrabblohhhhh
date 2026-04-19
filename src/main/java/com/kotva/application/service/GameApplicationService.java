package com.kotva.application.service;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Position;

public interface GameApplicationService {
    void assignLettertoBlank(GameSession session, String tileId, char assignedLetter);

    PreviewResult placeDraftTile(GameSession session, String tileId, Position position);

    PreviewResult moveDraftTile(GameSession session, String tileId, Position newPosition);

    PreviewResult removeDraftTile(GameSession session, String tileId);

    PreviewResult recallAllDraftTiles(GameSession session);

    GameActionResult submitDraft(GameSession session);

    GameActionResult submitDraft(GameSession session, String clientActionId);

    GameActionResult passTurn(GameSession session);

    GameActionResult passTurn(GameSession session, String clientActionId);

    GameActionResult resign(GameSession session);

    GameActionResult resign(GameSession session, String clientActionId);

    GameActionResult executeRemoteCommand(GameSession session, PlayerAction action);

    GameActionResult executeRemoteCommand(
            GameSession session,
            PlayerAction action,
            String clientActionId);

    void confirmHotSeatHandoff(GameSession session);

    GameSessionSnapshot tickClock(GameSession session, long elapsedMillis);

    GameSessionSnapshot getSessionSnapshot(GameSession session);

}
