package com.kotva.application.draft;

import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.PlayerAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TurnDraftActionMapper {
    private TurnDraftActionMapper() {
    }

    public static PlayerAction toPlaceAction(String playerId, TurnDraft turnDraft) {
        Objects.requireNonNull(playerId, "playerId cannot be null.");
        Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");

        List<ActionPlacement> placements = new ArrayList<>(turnDraft.getPlacements().size());
        for (DraftPlacement placement : turnDraft.getPlacements()) {
            Objects.requireNonNull(placement, "draft placement cannot be null.");
            placements.add(new ActionPlacement(placement.getTileId(), placement.getPosition()));
        }
        return PlayerAction.place(playerId, placements);
    }
}
