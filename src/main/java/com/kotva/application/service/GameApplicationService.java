package com.kotva.application.service;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.domain.model.Position;

public interface GameApplicationService {
    PreviewResult placeDraftTile(GameSession session, String tileId, Position position);

    PreviewResult moveDraftTile(GameSession session, String tileId, Position newPosition);

    PreviewResult removeDraftTile(GameSession session, String tileId);

    PreviewResult recallAllDraftTiles(GameSession session);

    SubmitDraftResult submitDraft(GameSession session);

    TurnTransitionResult passTurn(GameSession session);

    void confirmHotSeatHandoff(GameSession session);

    GameSessionSnapshot getSessionSnapshot(GameSession session);
}
