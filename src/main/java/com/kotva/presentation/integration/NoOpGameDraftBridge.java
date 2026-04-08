package com.kotva.presentation.integration;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.service.SubmitDraftResult;
import com.kotva.application.service.TurnTransitionResult;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Position;

/**
 * NoOpGameDraftBridge 是当前阶段的占位实现。
 * 它保证前端已经具备稳定调用点，但不会真正触发后端草稿逻辑。
 */
public class NoOpGameDraftBridge implements GameDraftBridge {
    @Override
    public PreviewResult placeDraftTile(GameSession session, String tileId, Position position) {
        return null;
    }

    @Override
    public PreviewResult moveDraftTile(GameSession session, String tileId, Position position) {
        return null;
    }

    @Override
    public PreviewResult removeDraftTile(GameSession session, String tileId) {
        return null;
    }

    @Override
    public PreviewResult recallAllDraftTiles(GameSession session) {
        return null;
    }

    @Override
    public SubmitDraftResult submitDraft(GameSession session) {
        return null;
    }

    @Override
    public TurnTransitionResult passTurn(GameSession session) {
        return null;
    }

    @Override
    public void rearrangeRack(GameSession session) {
    }

    @Override
    public void resign(GameSession session) {
    }
}
