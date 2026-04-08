package com.kotva.presentation.interaction;

import com.kotva.domain.model.Position;
import com.kotva.domain.model.TilePlacement;
import java.util.List;

/**
 * GameActionPort is the UI-to-controller bridge for draft and workbench
 * actions. The current implementation can stay front-end only while the
 * backend draft APIs are still being prepared.
 */
public interface GameActionPort {
    void onDraftTilePlaced(String tileId, Position position);

    void onDraftTileMoved(String tileId, Position position);

    void onDraftTileRemoved(String tileId);

    void onRecallAllDraftTilesRequested(List<TilePlacement> placements);

    void onSubmitDraftRequested(List<TilePlacement> placements);

    void onSkipTurnRequested();

    void onRearrangeRequested();

    void onResignRequested();
}
