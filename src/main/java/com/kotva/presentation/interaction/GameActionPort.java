package com.kotva.presentation.interaction;

import com.kotva.domain.model.Position;

public interface GameActionPort {

    boolean isInteractionLocked();

    void onDraftTilePlaced(String tileId, Position position);

    void onDraftTileMoved(String tileId, Position position);

    void onDraftTileRemoved(String tileId);

    void onRecallAllDraftTilesRequested();

    void onSubmitDraftRequested();

    void onSkipTurnRequested();

    void onRearrangeRequested();

    void onResignRequested();
}