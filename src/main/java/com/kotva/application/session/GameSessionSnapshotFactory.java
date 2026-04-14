package com.kotva.application.session;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.BoardHighlight;
import com.kotva.application.preview.HighlightType;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.preview.PreviewWord;
import com.kotva.application.result.BoardCellSnapshot;
import com.kotva.application.result.BoardSnapshot;
import com.kotva.application.result.BoardSnapshotFactory;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import com.kotva.policy.BonusType;
import com.kotva.policy.WordType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GameSessionSnapshotFactory {
    private GameSessionSnapshotFactory() {
    }

    public static GameSessionSnapshot fromSession(GameSession session) {
        return fromSession(session, null, null);
    }

    public static GameSessionSnapshot fromSession(
            GameSession session, AiRuntimeSnapshot aiRuntimeSnapshot) {
        return fromSession(session, aiRuntimeSnapshot, null);
    }

    public static GameSessionSnapshot fromSession(
            GameSession session,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        Objects.requireNonNull(session, "session cannot be null.");
        Player currentPlayer = resolveSnapshotPlayer(session);
        return buildSnapshot(
                session,
                currentPlayer.getPlayerId(),
                true,
                aiRuntimeSnapshot,
                clientRuntimeSnapshot);
    }

    public static GameSessionSnapshot fromSessionForViewer(
            GameSession session,
            String viewerPlayerId) {
        return fromSessionForViewer(session, viewerPlayerId, null, null);
    }

    public static GameSessionSnapshot fromSessionForViewer(
            GameSession session,
            String viewerPlayerId,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        Objects.requireNonNull(session, "session cannot be null.");
        Player currentPlayer = resolveSnapshotPlayer(session);
        String effectiveViewerPlayerId =
                viewerPlayerId == null ? currentPlayer.getPlayerId() : viewerPlayerId;
        boolean viewerCanSeeDraft =
                Objects.equals(effectiveViewerPlayerId, currentPlayer.getPlayerId());
        return buildSnapshot(
                session,
                effectiveViewerPlayerId,
                viewerCanSeeDraft,
                aiRuntimeSnapshot,
                clientRuntimeSnapshot);
    }

    public static GameSessionSnapshot withLocalDraft(
            GameSessionSnapshot baseSnapshot,
            TurnDraft turnDraft) {
        Objects.requireNonNull(baseSnapshot, "baseSnapshot cannot be null.");
        Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");

        PreviewSnapshot previewSnapshot = buildPreviewSnapshot(turnDraft.getPreviewResult());
        List<DraftPlacementSnapshot> draftPlacements = buildDraftPlacements(turnDraft);
        List<BoardCellRenderSnapshot> boardCells =
                buildBoardCells(
                        baseSnapshot.getBoardSnapshot(),
                        baseSnapshot.getVisibleRackTiles(),
                        turnDraft,
                        previewSnapshot);
        return copyOf(
                baseSnapshot,
                boardCells,
                baseSnapshot.getVisibleRackTiles(),
                draftPlacements,
                previewSnapshot,
                baseSnapshot.getAiRuntimeSnapshot(),
                baseSnapshot.getClientRuntimeSnapshot());
    }

    public static GameSessionSnapshot withClientRuntimeSnapshot(
            GameSessionSnapshot baseSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        Objects.requireNonNull(baseSnapshot, "baseSnapshot cannot be null.");
        return copyOf(
                baseSnapshot,
                baseSnapshot.getBoardCells(),
                baseSnapshot.getVisibleRackTiles(),
                baseSnapshot.getDraftPlacements(),
                baseSnapshot.getPreview(),
                baseSnapshot.getAiRuntimeSnapshot(),
                clientRuntimeSnapshot);
    }

    private static GameSessionSnapshot buildSnapshot(
            GameSession session,
            String viewerPlayerId,
            boolean viewerCanSeeDraft,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        Player currentPlayer = resolveSnapshotPlayer(session);
        Player viewerPlayer = resolveViewerPlayer(session, viewerPlayerId, currentPlayer);
        PlayerClock currentClock = currentPlayer.getClock();
        BoardSnapshot boardSnapshot = BoardSnapshotFactory.fromBoard(session.getGameState().getBoard());
        PreviewSnapshot previewSnapshot =
                viewerCanSeeDraft ? buildPreviewSnapshot(session.getTurnDraft().getPreviewResult()) : null;
        List<RackTileSnapshot> visibleRackTiles = buildVisibleRackTiles(viewerPlayer);
        List<DraftPlacementSnapshot> draftPlacements =
                viewerCanSeeDraft ? buildDraftPlacements(session.getTurnDraft()) : List.of();

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
                            player.getPlayerId().equals(currentPlayer.getPlayerId()),
                            countRackTiles(player),
                            clockSnapshot));
        }

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
                boardSnapshot,
                buildBoardCells(
                        boardSnapshot,
                        visibleRackTiles,
                        viewerCanSeeDraft ? session.getTurnDraft() : null,
                        previewSnapshot),
                visibleRackTiles,
                draftPlacements,
                previewSnapshot,
                session.getTurnCoordinator().getSettlementResult(),
                aiRuntimeSnapshot,
                clientRuntimeSnapshot);
    }

    private static GameSessionSnapshot copyOf(
            GameSessionSnapshot baseSnapshot,
            List<BoardCellRenderSnapshot> boardCells,
            List<RackTileSnapshot> visibleRackTiles,
            List<DraftPlacementSnapshot> draftPlacements,
            PreviewSnapshot previewSnapshot,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        return new GameSessionSnapshot(
                baseSnapshot.getSessionId(),
                baseSnapshot.getGameMode(),
                baseSnapshot.getSessionStatus(),
                baseSnapshot.isGameEnded(),
                baseSnapshot.getGameEndReason(),
                baseSnapshot.getTurnNumber(),
                baseSnapshot.getCurrentPlayerId(),
                baseSnapshot.getCurrentPlayerName(),
                baseSnapshot.getCurrentPlayerMainTimeRemainingMillis(),
                baseSnapshot.getCurrentPlayerByoYomiRemainingMillis(),
                baseSnapshot.getCurrentPlayerClockPhase(),
                baseSnapshot.getPlayerClockSnapshots(),
                baseSnapshot.getPlayers(),
                baseSnapshot.getBoardSnapshot(),
                boardCells,
                visibleRackTiles,
                draftPlacements,
                previewSnapshot,
                baseSnapshot.getSettlementResult(),
                aiRuntimeSnapshot,
                clientRuntimeSnapshot);
    }

    private static List<RackTileSnapshot> buildVisibleRackTiles(Player player) {
        List<RackTileSnapshot> visibleRackTiles = new ArrayList<>();
        for (RackSlot slot : player.getRack().getSlots()) {
            Tile tile = slot.getTile();
            visibleRackTiles.add(
                    new RackTileSnapshot(
                            slot.getIndex(),
                            tile != null ? tile.getTileID() : null,
                            tile != null ? tile.getLetter() : null,
                            tile != null ? resolveDisplayLetter(tile) : null,
                            tile != null ? tile.getScore() : 0,
                            tile != null && tile.isBlank(),
                            tile != null ? tile.getAssignedLetter() : null));
        }
        return visibleRackTiles;
    }

    private static List<BoardCellRenderSnapshot> buildBoardCells(
            BoardSnapshot boardSnapshot,
            List<RackTileSnapshot> visibleRackTiles,
            TurnDraft turnDraft,
            PreviewSnapshot previewSnapshot) {
        Map<BoardPositionKey, DraftPlacement> draftPlacementsByPosition = new HashMap<>();
        if (turnDraft != null) {
            for (DraftPlacement placement : turnDraft.getPlacements()) {
                if (placement == null || placement.getPosition() == null) {
                    continue;
                }
                draftPlacementsByPosition.put(
                        new BoardPositionKey(
                                placement.getPosition().getRow(),
                                placement.getPosition().getCol()),
                        placement);
            }
        }

        Map<String, RackTileSnapshot> visibleRackTilesById = new HashMap<>();
        for (RackTileSnapshot visibleRackTile : visibleRackTiles) {
            if (visibleRackTile.getTileId() != null) {
                visibleRackTilesById.put(visibleRackTile.getTileId(), visibleRackTile);
            }
        }

        Set<BoardPositionKey> previewValidPositions = new HashSet<>();
        Set<BoardPositionKey> previewInvalidPositions = new HashSet<>();
        collectPreviewHighlightPositions(
                previewSnapshot,
                previewValidPositions,
                previewInvalidPositions);

        Set<BoardPositionKey> mainWordPositions = new HashSet<>();
        Set<BoardPositionKey> crossWordPositions = new HashSet<>();
        collectPreviewWordPositions(previewSnapshot, mainWordPositions, crossWordPositions);

        List<BoardCellRenderSnapshot> boardCells = new ArrayList<>();
        for (BoardCellSnapshot boardCellSnapshot : boardSnapshot.getCells()) {
            int row = boardCellSnapshot.getRow();
            int col = boardCellSnapshot.getCol();
            BoardPositionKey key = new BoardPositionKey(row, col);
            DraftPlacement draftPlacement = draftPlacementsByPosition.get(key);

            if (boardCellSnapshot.getLetter() != null) {
                boardCells.add(
                        new BoardCellRenderSnapshot(
                                row,
                                col,
                                boardCellSnapshot.getBonusType(),
                                "__snapshot_board_" + row + "_" + col,
                                boardCellSnapshot.getLetter(),
                                boardCellSnapshot.getScore(),
                                boardCellSnapshot.isBlank(),
                                false,
                                previewValidPositions.contains(key),
                                previewInvalidPositions.contains(key),
                                mainWordPositions.contains(key),
                                crossWordPositions.contains(key)));
                continue;
            }

            if (draftPlacement == null) {
                continue;
            }

            RackTileSnapshot visibleRackTile = visibleRackTilesById.get(draftPlacement.getTileId());
            if (visibleRackTile == null) {
                continue;
            }

            boardCells.add(
                    new BoardCellRenderSnapshot(
                            row,
                            col,
                            boardCellSnapshot.getBonusType(),
                            visibleRackTile.getTileId(),
                            visibleRackTile.getDisplayLetter(),
                            visibleRackTile.getScore(),
                            visibleRackTile.isBlank(),
                            true,
                            previewValidPositions.contains(key),
                            previewInvalidPositions.contains(key),
                            mainWordPositions.contains(key),
                            crossWordPositions.contains(key)));
        }
        return boardCells;
    }

    private static List<DraftPlacementSnapshot> buildDraftPlacements(TurnDraft turnDraft) {
        List<DraftPlacementSnapshot> draftPlacements = new ArrayList<>();
        for (DraftPlacement placement : turnDraft.getPlacements()) {
            draftPlacements.add(
                    new DraftPlacementSnapshot(
                            placement.getTileId(),
                            placement.getPosition().getRow(),
                            placement.getPosition().getCol()));
        }
        return draftPlacements;
    }

    private static PreviewSnapshot buildPreviewSnapshot(PreviewResult previewResult) {
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

    private static Player resolveViewerPlayer(
            GameSession session,
            String viewerPlayerId,
            Player fallbackPlayer) {
        if (viewerPlayerId != null) {
            Player player = session.getGameState().getPlayerById(viewerPlayerId);
            if (player != null) {
                return player;
            }
        }
        return fallbackPlayer;
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

    private static void collectPreviewHighlightPositions(
            PreviewSnapshot previewSnapshot,
            Set<BoardPositionKey> previewValidPositions,
            Set<BoardPositionKey> previewInvalidPositions) {
        if (previewSnapshot == null) {
            return;
        }

        for (PreviewHighlightSnapshot highlight : previewSnapshot.getHighlights()) {
            BoardPositionKey key = new BoardPositionKey(highlight.getRow(), highlight.getCol());
            if (highlight.getHighlightType() == HighlightType.VALID_TILE) {
                previewValidPositions.add(key);
            } else if (highlight.getHighlightType() == HighlightType.INVALID_TILE) {
                previewInvalidPositions.add(key);
            }
        }
    }

    private static void collectPreviewWordPositions(
            PreviewSnapshot previewSnapshot,
            Set<BoardPositionKey> mainWordPositions,
            Set<BoardPositionKey> crossWordPositions) {
        if (previewSnapshot == null) {
            return;
        }

        for (PreviewWordSnapshot word : previewSnapshot.getWords()) {
            if (word.getWordType() == null) {
                continue;
            }

            Set<BoardPositionKey> target =
                    word.getWordType() == WordType.MAIN_WORD ? mainWordPositions : crossWordPositions;
            for (PreviewPositionSnapshot position : word.getCoveredPositions()) {
                target.add(new BoardPositionKey(position.getRow(), position.getCol()));
            }
        }
    }

    private static Character resolveDisplayLetter(Tile tile) {
        Objects.requireNonNull(tile, "tile cannot be null.");
        if (tile.isBlank() && tile.getAssignedLetter() != null) {
            return tile.getAssignedLetter();
        }
        return tile.getLetter();
    }

    private record BoardPositionKey(int row, int col) {
    }
}
