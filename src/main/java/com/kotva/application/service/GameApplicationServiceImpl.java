package com.kotva.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.kotva.application.draft.DraftManager;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.PlayerClockSnapshot;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.domain.model.Position;
import com.kotva.policy.ClockPhase;

public class GameApplicationServiceImpl implements GameApplicationService {
    private final ClockService clockService;
    private final DraftManager draftManager;
    private final MovePreviewService movePreviewService;

    public GameApplicationServiceImpl(ClockService clockService) {
        this(clockService, new DraftManager(), null);
    }

    public GameApplicationServiceImpl(
            ClockService clockService,
            DraftManager draftManager,
            MovePreviewService movePreviewService) {
        this.clockService = Objects.requireNonNull(clockService, "clockService cannot be null.");
        this.draftManager = Objects.requireNonNull(draftManager, "draftManager cannot be null.");
        this.movePreviewService = movePreviewService;
    }

    @Override
    public PreviewResult placeDraftTile(GameSession session, String tileId, Position position) {
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(position, "position cannot be null.");

        draftManager.placeTile(session.getTurnDraft(), tileId, position);
        return previewAndStore(session);
    }

    @Override
    public PreviewResult moveDraftTile(GameSession session, String tileId, Position newPosition) {
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Objects.requireNonNull(newPosition, "newPosition cannot be null.");

        draftManager.moveTile(session.getTurnDraft(), tileId, newPosition);
        return previewAndStore(session);
    }

    @Override
    public PreviewResult removeDraftTile(GameSession session, String tileId) {
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(tileId, "tileId cannot be null.");

        draftManager.removeTile(session.getTurnDraft(), tileId);
        return previewAndStore(session);
    }

    @Override
    public PreviewResult recallAllDraftTiles(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");

        draftManager.recallAllTiles(session.getTurnDraft());
        return previewAndStore(session);
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

    private PreviewResult previewAndStore(GameSession session) {
        PreviewResult previewResult;
        if (movePreviewService == null) {
            previewResult = new PreviewResult(true, 0, List.of(), List.of(), List.of());
        } else {
            previewResult = movePreviewService.preview(session);
            if (previewResult == null) {
                throw new IllegalStateException("movePreviewService returned null preview result.");
            }
        }

        session.getTurnDraft().setPreviewResult(previewResult);
        return previewResult;
    }
}
