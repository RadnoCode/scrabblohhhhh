package com.kotva.application.service;

import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.GameState;
import com.kotva.policy.DictionaryType;

public interface MovePreviewService {
    PreviewResult preview(GameSession session);

    PreviewResult preview(GameSession session, TurnDraft turnDraft);

    PreviewResult preview(GameState gameState, DictionaryType dictionaryType, TurnDraft turnDraft);
}
