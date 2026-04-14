package com.kotva.application.service;

import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.PreviewResult;
import com.kotva.domain.model.GameState;
import com.kotva.policy.DictionaryType;

public interface MovePreviewService {
    PreviewResult preview(
            GameState gameState,
            DictionaryType dictionaryType,
            String playerId,
            TurnDraft turnDraft);
}
