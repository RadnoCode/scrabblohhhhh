package com.kotva.runtime;

import com.kotva.application.service.GameApplicationService;
import com.kotva.tutorial.TutorialScriptId;
import java.util.Objects;

public final class TutorialRuntimeFactory {
    private final GameApplicationService gameApplicationService;

    public TutorialRuntimeFactory(GameApplicationService gameApplicationService) {
        this.gameApplicationService = Objects.requireNonNull(
            gameApplicationService,
            "gameApplicationService cannot be null.");
    }

    public GameRuntime create(TutorialScriptId tutorialScriptId) {
        return new TutorialGameRuntime(
            Objects.requireNonNull(tutorialScriptId, "tutorialScriptId cannot be null."),
            gameApplicationService);
    }
}
