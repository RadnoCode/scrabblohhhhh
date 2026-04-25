package com.kotva.application.draft;

import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.PlayerAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Converts application-layer draft state into domain-layer player actions.
 *
 * <p>The draft package keeps temporary UI/editing state, while the domain layer
 * accepts immutable {@link PlayerAction} commands. This mapper performs that
 * boundary conversion when a preview or final submit operation needs domain
 * validation.</p>
 */
public final class TurnDraftActionMapper {

    /**
     * Prevents construction because this class only contains static mapping helpers.
     */
    private TurnDraftActionMapper() {
    }

    /**
     * Builds a domain place action from the placements currently stored in a draft.
     *
     * @param playerId id of the player submitting the move
     * @param turnDraft draft containing staged tile placements
     * @return place action containing the draft placements in their current order
     * @throws NullPointerException if {@code playerId}, {@code turnDraft}, or any
     *         draft placement is {@code null}
     */
    public static PlayerAction toPlaceAction(String playerId, TurnDraft turnDraft) {
        Objects.requireNonNull(playerId, "playerId cannot be null.");
        Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");

        List<ActionPlacement> placements = new ArrayList<>(turnDraft.getPlacements().size());
        for (DraftPlacement placement : turnDraft.getPlacements()) {
            Objects.requireNonNull(placement, "draft placement cannot be null.");
            placements.add(
                new ActionPlacement(
                    placement.getTileId(),
                    placement.getPosition(),
                    placement.getAssignedLetter()));
        }
        return PlayerAction.place(playerId, placements);
    }
}
