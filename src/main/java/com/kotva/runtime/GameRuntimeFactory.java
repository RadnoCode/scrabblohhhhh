package com.kotva.runtime;

import com.kotva.ai.QuackleNativeBridge;
import com.kotva.application.service.AiRuntimeBootstrapper;
import com.kotva.application.service.AiSessionRuntimeFactory;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.session.GameSession;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.infrastructure.save.SaveGameRepository;
import java.util.Objects;
import java.util.function.Supplier;

public final class GameRuntimeFactory {
    private final GameSetupService gameSetupService;
    private final GameApplicationService gameApplicationService;
    private final Supplier<AiRuntimeBootstrapper> aiRuntimeBootstrapperSupplier;
    private final SaveGameRepository saveGameRepository;

    public GameRuntimeFactory(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService) {
        this(
            gameSetupService,
            gameApplicationService,
            () -> new AiSessionRuntimeFactory(new QuackleNativeBridge()),
            new SaveGameRepository());
    }

    GameRuntimeFactory(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService,
        Supplier<AiRuntimeBootstrapper> aiRuntimeBootstrapperSupplier) {
        this(gameSetupService, gameApplicationService, aiRuntimeBootstrapperSupplier, new SaveGameRepository());
    }

    public GameRuntimeFactory(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService,
        SaveGameRepository saveGameRepository) {
        this(
            gameSetupService,
            gameApplicationService,
            () -> new AiSessionRuntimeFactory(new QuackleNativeBridge()),
            saveGameRepository);
    }

    GameRuntimeFactory(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService,
        Supplier<AiRuntimeBootstrapper> aiRuntimeBootstrapperSupplier,
        SaveGameRepository saveGameRepository) {
        this.gameSetupService =
        Objects.requireNonNull(gameSetupService, "gameSetupService cannot be null.");
        this.gameApplicationService = Objects.requireNonNull(
            gameApplicationService, "gameApplicationService cannot be null.");
        this.aiRuntimeBootstrapperSupplier = Objects.requireNonNull(
            aiRuntimeBootstrapperSupplier,
            "aiRuntimeBootstrapperSupplier cannot be null.");
        this.saveGameRepository = Objects.requireNonNull(
            saveGameRepository,
            "saveGameRepository cannot be null.");
    }

    public GameRuntime create(NewGameRequest request) {
        Objects.requireNonNull(request, "request cannot be null.");
        if (request.getGameMode() == com.kotva.mode.GameMode.LAN_MULTIPLAYER) {
            return create(RuntimeLaunchSpec.forLanHost(request));
        }
        return create(RuntimeLaunchSpec.forLocal(request));
    }

    public GameRuntime create(RuntimeLaunchSpec launchSpec) {
        Objects.requireNonNull(launchSpec, "launchSpec cannot be null.");
        return switch (launchSpec.getGameMode()) {
            case HOT_SEAT -> new HotSeatGameRuntime(
                    gameSetupService,
                    gameApplicationService,
                    saveGameRepository);
            case HUMAN_VS_AI -> new LocalAiGameRuntime(
                    gameSetupService,
                    gameApplicationService,
                    aiRuntimeBootstrapperSupplier);
            case LAN_MULTIPLAYER -> launchSpec.getLanRole() == LanRole.CLIENT
                    ? new ClientGameRuntime(launchSpec)
                    : new HostGameRuntime(
                            gameSetupService,
                            gameApplicationService);
        };
    }

    public GameRuntime createHotSeatFromSave(GameSession session) {
        return new HotSeatGameRuntime(
            gameSetupService,
            gameApplicationService,
            saveGameRepository,
            Objects.requireNonNull(session, "session cannot be null."));
    }
}
