package com.kotva.application;
import com.kotva.policy.ActionType;
import com.kotva.application.draft.TurnDraft;

public record PlayerAction(
        String playerId,
        ActionType type,
        TurnDraft draft 
) {
    
    public static PlayerAction place(String id, TurnDraft draft) {
        return new PlayerAction(id, ActionType.PLACE_TILE, draft);
    }

    public static PlayerAction pass(String id) {
        return new PlayerAction(id, ActionType.PASS_TURN, null);
    }
    public static PlayerAction lose(String id) {
        return new PlayerAction(id, ActionType.LOSE, null);
    }
}