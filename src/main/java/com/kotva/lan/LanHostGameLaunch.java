package com.kotva.lan;

import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import java.util.Objects;

public record LanHostGameLaunch(
        GameSession session,
        LanHostService lanHostService,
        GameSessionSnapshot hostInitialSnapshot) {
    public LanHostGameLaunch {
        session = Objects.requireNonNull(session, "session cannot be null.");
        lanHostService = Objects.requireNonNull(
                lanHostService,
                "lanHostService cannot be null.");
        hostInitialSnapshot = Objects.requireNonNull(
                hostInitialSnapshot,
                "hostInitialSnapshot cannot be null.");
    }
}
