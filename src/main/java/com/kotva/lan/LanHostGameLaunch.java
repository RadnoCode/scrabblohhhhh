package com.kotva.lan;

import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import java.util.Objects;

/**
 * Data created when a LAN host starts the game.
 *
 * @param session authoritative game session
 * @param lanHostService service that handles client commands
 * @param hostInitialSnapshot initial snapshot for the host
 */
public record LanHostGameLaunch(
        GameSession session,
        LanHostService lanHostService,
        GameSessionSnapshot hostInitialSnapshot) {
    /**
     * Validates launch data.
     */
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
