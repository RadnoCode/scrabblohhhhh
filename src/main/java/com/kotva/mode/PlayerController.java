package com.kotva.mode;
import com.kotva.application.draft.TurnDraft;
public interface PlayerController {
    String getPlayerId();
    TurnDraft getOnSubmitDraft();
}

