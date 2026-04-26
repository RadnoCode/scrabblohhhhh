package com.kotva.application.service;

import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.GameState;
import com.kotva.policy.DictionaryType;

/**
 * Builds preview results for a draft move.
 */
public interface MovePreviewService {

    /**
     * Previews the current draft in a session.
     *
     * @param session game session
     * @return preview result
     */
    PreviewResult preview(GameSession session);

    /**
     * Previews a draft with explicit state and player information.
     *
     * @param gameState domain game state
     * @param dictionaryType dictionary to use
     * @param playerId player id for the preview
     * @param turnDraft draft to preview
     * @return preview result
     */
    PreviewResult preview(
            GameState gameState,
            DictionaryType dictionaryType,
            String playerId,
            TurnDraft turnDraft);
}
