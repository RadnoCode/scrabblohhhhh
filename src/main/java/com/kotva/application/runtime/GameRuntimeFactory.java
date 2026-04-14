package com.kotva.application.runtime;

import com.kotva.ai.QuackleNativeBridge;
import com.kotva.application.session.GameConfig;
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
        return create(
                request.getGameMode() == com.kotva.mode.GameMode.LAN_MULTIPLAYER
                        ? RuntimeLaunchSpec.forLanHost(request)
                        : RuntimeLaunchSpec.forLocal(request));
    }

    public GameRuntime create(RuntimeLaunchSpec launchSpec) {
        Objects.requireNonNull(launchSpec, "launchSpec cannot be null.");
        RuntimeLaunchSpec normalizedLaunchSpec = normalizeLaunchSpec(launchSpec);
        return switch (normalizedLaunchSpec.getGameMode()) {
            case HOT_SEAT -> new HotSeatGameRuntime(
                    normalizedLaunchSpec,
                    gameSetupService,
                    gameApplicationService);
            case HUMAN_VS_AI -> new LocalAiGameRuntime(
                    normalizedLaunchSpec,
                    gameSetupService,
                    gameApplicationService,
                    aiRuntimeBootstrapperSupplier);
            case LAN_MULTIPLAYER -> normalizedLaunchSpec.getLanRole() == LanRole.CLIENT
                    ? new ClientGameRuntime(normalizedLaunchSpec)
                    : new HostGameRuntime(
                            normalizedLaunchSpec,
                            gameSetupService,
                            gameApplicationService);
        };
    }

    private RuntimeLaunchSpec normalizeLaunchSpec(RuntimeLaunchSpec launchSpec) {
        if (launchSpec.getGameMode() != com.kotva.mode.GameMode.LAN_MULTIPLAYER) {
            return launchSpec;
        }
        if (launchSpec.hasLanLaunchConfig()) {
            return launchSpec;
        }
        if (launchSpec.getLanRole() != LanRole.HOST || !launchSpec.hasRequest()) {
            throw new IllegalArgumentException("LAN client launch requires explicit LanLaunchConfig.");
        }

        NewGameRequest request = launchSpec.requireRequest();
        GameConfig config = gameSetupService.buildConfig(request);
        LanLaunchConfig lanLaunchConfig =
                new LanLaunchConfig(
                        LanRole.HOST,
                        config,
                        "player-1",
                        null,
                        null);
        return launchSpec.withLanLaunchConfig(lanLaunchConfig);
    }
}
