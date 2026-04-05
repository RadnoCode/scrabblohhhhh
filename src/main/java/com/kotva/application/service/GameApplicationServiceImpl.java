package com.kotva.application.service;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.PlayerClockSnapshot;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.domain.model.Position;
import com.kotva.policy.ClockPhase;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameApplicationServiceImpl implements GameApplicationService {
    private final ClockService clockService;

    public GameApplicationServiceImpl(ClockService clockService) {
        this.clockService = Objects.requireNonNull(clockService, "clockService cannot be null.");
    }

    @Override
    public PreviewResult placeDraftTile(GameSession session, String tileId, Position position) {
        throw new UnsupportedOperationException("placeDraftTile is not implemented yet.");
    }

    @Override
    public PreviewResult moveDraftTile(GameSession session, String tileId, Position newPosition) {
        throw new UnsupportedOperationException("moveDraftTile is not implemented yet.");
    }

    @Override
    public PreviewResult removeDraftTile(GameSession session, String tileId) {
        throw new UnsupportedOperationException("removeDraftTile is not implemented yet.");
    }

    @Override
    public PreviewResult recallAllDraftTiles(GameSession session) {
        throw new UnsupportedOperationException("recallAllDraftTiles is not implemented yet.");
    }

    @Override
    public SubmitDraftResult submitDraft(GameSession session) {
        throw new UnsupportedOperationException("submitDraft is not implemented yet.");
    }

    @Override
    public TurnTransitionResult passTurn(GameSession session) {
        throw new UnsupportedOperationException("passTurn is not implemented yet.");
    }

    @Override
    public void confirmHotSeatHandoff(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
    }

    @Override
    public GameSessionSnapshot tickClock(GameSession session, long elapsedMillis) {
        clockService.tick(session, elapsedMillis);
        return getSessionSnapshot(session);
    }

    @Override
    public GameSessionSnapshot getSessionSnapshot(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");

        List<PlayerClockSnapshot> playerClockSnapshots = new ArrayList<>();
        for (Player player : session.getGameState().getPlayers()) {
            PlayerClock clock = player.getClock();
            playerClockSnapshots.add(
                    new PlayerClockSnapshot(
                            player.getPlayerId(),
                            player.getPlayerName(),
                            clock.getMainTimeRemainingMillis(),
                            clock.getByoYomiRemainingMillis(),
                            clock.getPhase(),
                            player.getActive()));
        }

        Player currentPlayer = session.getGameState().getCurrentPlayer();
        PlayerClock currentClock = currentPlayer.getClock();
        ClockPhase currentPhase = currentClock.getPhase();

        return new GameSessionSnapshot(
                session.getSessionStatus(),
                currentPlayer.getPlayerId(),
                currentPlayer.getPlayerName(),
                currentClock.getMainTimeRemainingMillis(),
                currentClock.getByoYomiRemainingMillis(),
                currentPhase,
                playerClockSnapshots);
    }
}
