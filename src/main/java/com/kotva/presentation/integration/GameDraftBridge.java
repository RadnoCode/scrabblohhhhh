package com.kotva.presentation.integration;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.service.SubmitDraftResult;
import com.kotva.application.service.TurnTransitionResult;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Position;

/**
 * GameDraftBridge 是前端控制器与后端草稿接口之间的适配层。
 * 当前可以先接一个空实现，等后端方法可用后再切成真实桥接实现。
 */
public interface GameDraftBridge {
    PreviewResult placeDraftTile(GameSession session, String tileId, Position position);

    PreviewResult moveDraftTile(GameSession session, String tileId, Position position);

    PreviewResult removeDraftTile(GameSession session, String tileId);

    PreviewResult recallAllDraftTiles(GameSession session);

    SubmitDraftResult submitDraft(GameSession session);

    TurnTransitionResult passTurn(GameSession session);

    void rearrangeRack(GameSession session);

    void resign(GameSession session);
}
