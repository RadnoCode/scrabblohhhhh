package com.kotva.application.runtime;

import com.kotva.application.session.GameConfig;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.mode.GameMode;
import java.util.Objects;

public final class RuntimeLaunchSpec {
    private final NewGameRequest request;
    private final LanRole lanRole;
    private final LanLaunchConfig lanLaunchConfig;

    private RuntimeLaunchSpec(
            NewGameRequest request,
            LanRole lanRole,
            LanLaunchConfig lanLaunchConfig) {
        this.request = request;
        this.lanRole = lanRole;
        this.lanLaunchConfig = lanLaunchConfig;
    }

    public static RuntimeLaunchSpec forLocal(NewGameRequest request) {
        Objects.requireNonNull(request, "request cannot be null.");
        return new RuntimeLaunchSpec(request, null, null);
    }

    public static RuntimeLaunchSpec forLanHost(NewGameRequest request) {
        Objects.requireNonNull(request, "request cannot be null.");
        return new RuntimeLaunchSpec(request, LanRole.HOST, null);
    }

    public static RuntimeLaunchSpec forLanHost(LanLaunchConfig lanLaunchConfig) {
        Objects.requireNonNull(lanLaunchConfig, "lanLaunchConfig cannot be null.");
        if (lanLaunchConfig.getRole() != LanRole.HOST) {
            throw new IllegalArgumentException("LAN host launch requires HOST role.");
        }
        return new RuntimeLaunchSpec(null, LanRole.HOST, lanLaunchConfig);
    }

    public static RuntimeLaunchSpec forLanClient(LanLaunchConfig lanLaunchConfig) {
        Objects.requireNonNull(lanLaunchConfig, "lanLaunchConfig cannot be null.");
        if (lanLaunchConfig.getRole() != LanRole.CLIENT) {
            throw new IllegalArgumentException("LAN client launch requires CLIENT role.");
        }
        return new RuntimeLaunchSpec(null, LanRole.CLIENT, lanLaunchConfig);
    }

    public RuntimeLaunchSpec withLanLaunchConfig(LanLaunchConfig lanLaunchConfig) {
        return new RuntimeLaunchSpec(request, lanRole, Objects.requireNonNull(
                lanLaunchConfig, "lanLaunchConfig cannot be null."));
    }

    public boolean hasRequest() {
        return request != null;
    }

    public NewGameRequest requireRequest() {
        return Objects.requireNonNull(request, "request cannot be null.");
    }

    public LanRole getLanRole() {
        return lanRole;
    }

    public boolean hasLanLaunchConfig() {
        return lanLaunchConfig != null;
    }

    public LanLaunchConfig requireLanLaunchConfig() {
        return Objects.requireNonNull(lanLaunchConfig, "lanLaunchConfig cannot be null.");
    }

    public GameMode getGameMode() {
        if (request != null) {
            return request.getGameMode();
        }
        return requireLanLaunchConfig().getGameConfig().getGameMode();
    }

    public GameConfig getGameConfigOrNull() {
        return lanLaunchConfig == null ? null : lanLaunchConfig.getGameConfig();
    }
}
