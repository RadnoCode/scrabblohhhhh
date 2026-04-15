package com.kotva.application.runtime;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;

final class HotSeatGameRuntime extends AbstractLocalGameRuntime {

    HotSeatGameRuntime(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService) {
        super(gameSetupService, gameApplicationService);
    }
}