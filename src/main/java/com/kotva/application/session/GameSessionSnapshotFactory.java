package com.kotva.application.session;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.preview.BoardHighlight;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.preview.PreviewWord;
import com.kotva.application.result.BoardSnapshotFactory;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GameSessionSnapshotFactory {
    private GameSessionSnapshotFactory() {
    }

    public static GameSessionSnapshot fromSession(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");

        List<PlayerClockSnapshot> playerClockSnapshots = new ArrayList<>();
        List<GamePlayerSnapshot> players = new ArrayList<>();
        for (Player player : session.getGameState().getPlayers()) {
            PlayerClock clock = player.getClock();
            PlayerClockSnapshot clockSnapshot =
                    new PlayerClockSnapshot(
                            player.getPlayerId(),
                            player.getPlayerName(),
                            clock.getMainTimeRemainingMillis(),
                            clock.getByoYomiRemainingMillis(),
                            clock.getPhase(),
                            player.getActive());
            playerClockSnapshots.add(clockSnapshot);
            players.add(
                    new GamePlayerSnapshot(
                            player.getPlayerId(),
                            player.getPlayerName(),
                            player.getPlayerType(),
                            player.getScore(),
                            player.getActive(),
                            player == session.getGameState().getCurrentPlayer(),
                            countRackTiles(player),
                            clockSnapshot));
        }

        Player currentPlayer = resolveSnapshotPlayer(session);
        PlayerClock currentClock = currentPlayer.getClock();

        return new GameSessionSnapshot(
                session.getSessionId(),
                session.getConfig().getGameMode(),
                session.getSessionStatus(),
                session.getGameState().isGameOver(),
                session.getGameState().getGameEndReason(),
                session.getTurnCoordinator().getTurnNumber(),
                currentPlayer.getPlayerId(),
                currentPlayer.getPlayerName(),
                currentClock.getMainTimeRemainingMillis(),
                currentClock.getByoYomiRemainingMillis(),
                currentClock.getPhase(),
                playerClockSnapshots,
                players,
                BoardSnapshotFactory.fromBoard(session.getGameState().getBoard()),
                buildCurrentRackTiles(currentPlayer),
                buildDraftPlacements(session),
                buildPreviewSnapshot(session),
                session.getTurnCoordinator().getSettlementResult());
    }

    private static List<RackTileSnapshot> buildCurrentRackTiles(Player player) {
        List<RackTileSnapshot> currentRackTiles = new ArrayList<>();
        for (RackSlot slot : player.getRack().getSlots()) {
            Tile tile = slot.getTile();
            currentRackTiles.add(
                    new RackTileSnapshot(
                            slot.getIndex(),
                            tile != null ? tile.getTileID() : null,
                            tile != null ? tile.getLetter() : null,
                            tile != null ? tile.getScore() : 0,
                            tile != null && tile.isBlank(),
                            tile != null ? tile.getAssignedLetter() : null));
        }
        return currentRackTiles;
    }

    private static List<DraftPlacementSnapshot> buildDraftPlacements(GameSession session) {
        List<DraftPlacementSnapshot> draftPlacements = new ArrayList<>();
        for (DraftPlacement placement : session.getTurnDraft().getPlacements()) {
            draftPlacements.add(
                    new DraftPlacementSnapshot(
                            placement.getTileId(),
                            placement.getPosition().getRow(),
                            placement.getPosition().getCol()));
        }
        return draftPlacements;
    }

    private static PreviewSnapshot buildPreviewSnapshot(GameSession session) {
        PreviewResult previewResult = session.getTurnDraft().getPreviewResult();
        if (previewResult == null) {
            return null;
        }
        return new PreviewSnapshot(
                previewResult.isValid(),
                previewResult.getEstimatedScore(),
                buildPreviewWords(previewResult),
                buildPreviewHighlights(previewResult),
                previewResult.getMessages());
    }

    private static List<PreviewWordSnapshot> buildPreviewWords(PreviewResult previewResult) {
        List<PreviewWordSnapshot> words = new ArrayList<>();
        if (previewResult.getWordList() == null) {
            return words;
        }

        for (PreviewWord word : previewResult.getWordList()) {
            if (word == null) {
                continue;
            }
            words.add(
                    new PreviewWordSnapshot(
                            word.getWord(),
                            word.isValid(),
                            word.getScoreContribution(),
                            buildPreviewPositions(word.getCoveredPositions()),
                            word.getWordType()));
        }
        return words;
    }

    private static List<PreviewHighlightSnapshot> buildPreviewHighlights(PreviewResult previewResult) {
        List<PreviewHighlightSnapshot> highlights = new ArrayList<>();
        if (previewResult.getHighlights() == null) {
            return highlights;
        }

        for (BoardHighlight highlight : previewResult.getHighlights()) {
            if (highlight == null || highlight.getPosition() == null) {
                continue;
            }
            highlights.add(
                    new PreviewHighlightSnapshot(
                            highlight.getPosition().getRow(),
                            highlight.getPosition().getCol(),
                            highlight.getHighlightType()));
        }
        return highlights;
    }

    private static List<PreviewPositionSnapshot> buildPreviewPositions(List<Position> positions) {
        List<PreviewPositionSnapshot> coveredPositions = new ArrayList<>();
        if (positions == null) {
            return coveredPositions;
        }

        for (Position position : positions) {
            if (position == null) {
                continue;
            }
            coveredPositions.add(
                    new PreviewPositionSnapshot(position.getRow(), position.getCol()));
        }
        return coveredPositions;
    }

    private static Player resolveSnapshotPlayer(GameSession session) {
        if (session.getGameState().hasActivePlayers()) {
            return session.getGameState().requireCurrentActivePlayer();
        }
        return session.getGameState().getCurrentPlayer();
    }

    private static int countRackTiles(Player player) {
        int count = 0;
        for (RackSlot slot : player.getRack().getSlots()) {
            if (!slot.isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
