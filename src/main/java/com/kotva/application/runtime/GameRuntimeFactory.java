package com.kotva.application.runtime;

import com.kotva.ai.QuackleNativeBridge;
import com.kotva.application.service.AiRuntimeBootstrapper;
import com.kotva.application.service.AiSessionRuntimeFactory;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.setup.NewGameRequest;
import java.util.Objects;
import java.util.function.Supplier;

public final class GameRuntimeFactory {
    private final GameSetupService gameSetupService;
    private final GameApplicationService gameApplicationService;
    private final Supplier<AiRuntimeBootstrapper> aiRuntimeBootstrapperSupplier;

    public GameRuntimeFactory(
            GameSetupService gameSetupService,
            GameApplicationService gameApplicationService) {
        this(
                gameSetupService,
                gameApplicationService,
                () -> new AiSessionRuntimeFactory(new QuackleNativeBridge()));
    }

    GameRuntimeFactory(
            GameSetupService gameSetupService,
            GameApplicationService gameApplicationService,
            Supplier<AiRuntimeBootstrapper> aiRuntimeBootstrapperSupplier) {
        this.gameSetupService =
                Objects.requireNonNull(gameSetupService, "gameSetupService cannot be null.");
        this.gameApplicationService = Objects.requireNonNull(
                gameApplicationService, "gameApplicationService cannot be null.");
        this.aiRuntimeBootstrapperSupplier = Objects.requireNonNull(
                aiRuntimeBootstrapperSupplier,
                "aiRuntimeBootstrapperSupplier cannot be null.");
    }

    public GameRuntime create(NewGameRequest request) {
        Objects.requireNonNull(request, "request cannot be null.");
        return switch (request.getGameMode()) {
            case HOT_SEAT -> new HotSeatGameRuntime(
                    gameSetupService,
                    gameApplicationService);
            case HUMAN_VS_AI -> new LocalAiGameRuntime(
                    gameSetupService,
                    gameApplicationService,
                    aiRuntimeBootstrapperSupplier);
            case LAN_MULTIPLAYER ->
                    throw new IllegalArgumentException(
                            "LAN_MULTIPLAYER is not supported on this branch.");
        };
    }
}
