package com.kotva.application.session;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.BoardHighlight;
import com.kotva.application.preview.HighlightType;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.preview.PreviewWord;
import com.kotva.application.result.BoardCellSnapshot;
import com.kotva.application.result.BoardSnapshotFactory;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.SessionStatus;
import com.kotva.policy.WordType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Builds game session snapshots for UI rendering and LAN transfer.
 */
public final class GameSessionSnapshotFactory {

    /**
     * Prevents creating this utility class.
     */
    private GameSessionSnapshotFactory() {
    }

    /**
     * Builds a snapshot visible to the current player.
     *
     * @param session source game session
     * @return session snapshot
     */
    public static GameSessionSnapshot fromSession(GameSession session) {
        return fromSession(session, null, null);
    }

    /**
     * Builds a snapshot with AI runtime status.
     *
     * @param session source game session
     * @param aiRuntimeSnapshot AI runtime status
     * @return session snapshot
     */
    public static GameSessionSnapshot fromSession(
            GameSession session,
            AiRuntimeSnapshot aiRuntimeSnapshot) {
        return fromSession(session, aiRuntimeSnapshot, null);
    }

    /**
     * Builds a snapshot with AI and LAN client runtime status.
     *
     * @param session source game session
     * @param aiRuntimeSnapshot AI runtime status
     * @param clientRuntimeSnapshot LAN client runtime status
     * @return session snapshot
     */
    public static GameSessionSnapshot fromSession(
            GameSession session,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        Objects.requireNonNull(session, "session cannot be null.");
        Player currentPlayer = resolveSnapshotPlayer(session);
        return buildSnapshot(
                session,
                currentPlayer,
                true,
                aiRuntimeSnapshot,
                clientRuntimeSnapshot);
    }

    /**
     * Builds a snapshot for a specific viewer.
     *
     * @param session source game session
     * @param viewerPlayerId id of the player who will view the snapshot
     * @return session snapshot filtered for that viewer
     */
    public static GameSessionSnapshot fromSessionForViewer(
            GameSession session,
            String viewerPlayerId) {
        return fromSessionForViewer(session, viewerPlayerId, null, null);
    }

    /**
     * Builds a snapshot for a specific viewer with runtime status.
     *
     * @param session source game session
     * @param viewerPlayerId id of the player who will view the snapshot
     * @param aiRuntimeSnapshot AI runtime status
     * @param clientRuntimeSnapshot LAN client runtime status
     * @return session snapshot filtered for that viewer
     */
    public static GameSessionSnapshot fromSessionForViewer(
            GameSession session,
            String viewerPlayerId,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        Objects.requireNonNull(session, "session cannot be null.");
        Player currentPlayer = resolveSnapshotPlayer(session);
        Player viewerPlayer = resolveViewerPlayer(session, viewerPlayerId, currentPlayer);
        boolean viewerCanSeeDraft = viewerPlayer != null
                && Objects.equals(viewerPlayer.getPlayerId(), currentPlayer.getPlayerId());
        return buildSnapshot(
                session,
                viewerPlayer,
                viewerCanSeeDraft,
                aiRuntimeSnapshot,
                clientRuntimeSnapshot);
    }

    /**
     * Adds a local draft to an existing snapshot.
     *
     * @param baseSnapshot snapshot from the host or latest local state
     * @param turnDraft local editable draft
     * @return copied snapshot with local draft data
     */
    public static GameSessionSnapshot withLocalDraft(
            GameSessionSnapshot baseSnapshot,
            TurnDraft turnDraft) {
        Objects.requireNonNull(baseSnapshot, "baseSnapshot cannot be null.");
        Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");

        PreviewSnapshot previewSnapshot = buildPreviewSnapshot(turnDraft.getPreviewResult());
        List<DraftPlacementSnapshot> draftPlacements = buildDraftPlacements(turnDraft);
        List<RackTileSnapshot> visibleRackTiles = buildVisibleRackTiles(baseSnapshot, turnDraft);
        List<BoardCellRenderSnapshot> boardCells =
                buildBoardCells(baseSnapshot, turnDraft, previewSnapshot, visibleRackTiles);
        return copyOf(
                baseSnapshot,
                boardCells,
                visibleRackTiles,
                draftPlacements,
                previewSnapshot,
                baseSnapshot.getAiRuntimeSnapshot(),
                baseSnapshot.getClientRuntimeSnapshot());
    }

    /**
     * Adds LAN client runtime status to a snapshot.
     *
     * @param baseSnapshot source snapshot
     * @param clientRuntimeSnapshot client runtime status
     * @return copied snapshot with client status
     */
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

    /**
     * Copies a snapshot and records when it was received.
     *
     * @param baseSnapshot source snapshot
     * @param receivedAtEpochMillis receive time in epoch milliseconds
     * @return copied snapshot with receive timestamp
     */
    public static GameSessionSnapshot withReceivedTimestamp(
            GameSessionSnapshot baseSnapshot,
            long receivedAtEpochMillis) {
        Objects.requireNonNull(baseSnapshot, "baseSnapshot cannot be null.");
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
                baseSnapshot.getBoardCells(),
                baseSnapshot.getVisibleRackTiles(),
                baseSnapshot.getDraftPlacements(),
                baseSnapshot.getPreview(),
                baseSnapshot.getTutorial(),
                baseSnapshot.getLatestActionResult(),
                baseSnapshot.getSettlementResult(),
                baseSnapshot.getAiRuntimeSnapshot(),
                baseSnapshot.getClientRuntimeSnapshot(),
                baseSnapshot.getSnapshotSentAtEpochMillis(),
                receivedAtEpochMillis);
    }

    /**
     * Predicts the local clock display using elapsed client time.
     *
     * @param baseSnapshot source snapshot
     * @param localElapsedMillis elapsed local time since receiving the snapshot
     * @return copied snapshot with predicted clock values
     */
    public static GameSessionSnapshot withLocalClockPrediction(
            GameSessionSnapshot baseSnapshot,
            long localElapsedMillis) {
        Objects.requireNonNull(baseSnapshot, "baseSnapshot cannot be null.");
        if (localElapsedMillis < 0L) {
            throw new IllegalArgumentException("localElapsedMillis cannot be negative.");
        }
        if (baseSnapshot.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            return baseSnapshot;
        }
        if (baseSnapshot.getCurrentPlayerClockPhase() == ClockPhase.DISABLED
                || baseSnapshot.getCurrentPlayerClockPhase() == ClockPhase.TIMEOUT) {
            return baseSnapshot;
        }

        long totalElapsedMillis = safeAdd(resolveTransportDelayMillis(baseSnapshot), localElapsedMillis);
        if (totalElapsedMillis == 0L) {
            return baseSnapshot;
        }

        PredictedClock predictedClock = applyElapsedToClock(
                baseSnapshot.getCurrentPlayerMainTimeRemainingMillis(),
                baseSnapshot.getCurrentPlayerByoYomiRemainingMillis(),
                baseSnapshot.getCurrentPlayerClockPhase(),
                totalElapsedMillis);

        List<PlayerClockSnapshot> predictedPlayerClockSnapshots = new ArrayList<>();
        for (PlayerClockSnapshot playerClockSnapshot : baseSnapshot.getPlayerClockSnapshots()) {
            if (Objects.equals(playerClockSnapshot.getPlayerId(), baseSnapshot.getCurrentPlayerId())) {
                predictedPlayerClockSnapshots.add(new PlayerClockSnapshot(
                        playerClockSnapshot.getPlayerId(),
                        playerClockSnapshot.getPlayerName(),
                        predictedClock.mainTimeRemainingMillis(),
                        predictedClock.byoYomiRemainingMillis(),
                        predictedClock.phase(),
                        playerClockSnapshot.isActive()));
                continue;
            }
            predictedPlayerClockSnapshots.add(playerClockSnapshot);
        }

        Map<String, PlayerClockSnapshot> playerClockSnapshotsById = new HashMap<>();
        for (PlayerClockSnapshot playerClockSnapshot : predictedPlayerClockSnapshots) {
            playerClockSnapshotsById.put(playerClockSnapshot.getPlayerId(), playerClockSnapshot);
        }

        List<GamePlayerSnapshot> predictedPlayers = new ArrayList<>();
        for (GamePlayerSnapshot playerSnapshot : baseSnapshot.getPlayers()) {
            PlayerClockSnapshot clockSnapshot = playerClockSnapshotsById.getOrDefault(
                    playerSnapshot.getPlayerId(),
                    playerSnapshot.getClockSnapshot());
            predictedPlayers.add(new GamePlayerSnapshot(
                    playerSnapshot.getPlayerId(),
                    playerSnapshot.getPlayerName(),
                    playerSnapshot.getPlayerType(),
                    playerSnapshot.getScore(),
                    playerSnapshot.isActive(),
                    playerSnapshot.isCurrentTurn(),
                    playerSnapshot.getRackTileCount(),
                    clockSnapshot));
        }

        return new GameSessionSnapshot(
                baseSnapshot.getSessionId(),
                baseSnapshot.getGameMode(),
                baseSnapshot.getSessionStatus(),
                baseSnapshot.isGameEnded(),
                baseSnapshot.getGameEndReason(),
                baseSnapshot.getTurnNumber(),
                baseSnapshot.getCurrentPlayerId(),
                baseSnapshot.getCurrentPlayerName(),
                predictedClock.mainTimeRemainingMillis(),
                predictedClock.byoYomiRemainingMillis(),
                predictedClock.phase(),
                predictedPlayerClockSnapshots,
                predictedPlayers,
                baseSnapshot.getBoardSnapshot(),
                baseSnapshot.getBoardCells(),
                baseSnapshot.getVisibleRackTiles(),
                baseSnapshot.getDraftPlacements(),
                baseSnapshot.getPreview(),
                baseSnapshot.getTutorial(),
                baseSnapshot.getLatestActionResult(),
                baseSnapshot.getSettlementResult(),
                baseSnapshot.getAiRuntimeSnapshot(),
                baseSnapshot.getClientRuntimeSnapshot(),
                baseSnapshot.getSnapshotSentAtEpochMillis(),
                baseSnapshot.getSnapshotReceivedAtEpochMillis());
    }

    /**
     * Builds a full snapshot from a session and viewer context.
     *
     * @param session source session
     * @param viewerPlayer player who will see the snapshot
     * @param viewerCanSeeDraft whether draft and preview should be included
     * @param aiRuntimeSnapshot AI runtime status
     * @param clientRuntimeSnapshot LAN client runtime status
     * @return full session snapshot
     */
    private static GameSessionSnapshot buildSnapshot(
            GameSession session,
            Player viewerPlayer,
            boolean viewerCanSeeDraft,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot) {
        Player currentPlayer = resolveSnapshotPlayer(session);
        Player effectiveViewerPlayer = viewerPlayer == null ? currentPlayer : viewerPlayer;
        PlayerClock currentClock = currentPlayer.getClock();
        PreviewSnapshot previewSnapshot = viewerCanSeeDraft
                ? buildPreviewSnapshot(session.getTurnDraft().getPreviewResult())
                : null;
        List<RackTileSnapshot> visibleRackTiles = buildVisibleRackTiles(effectiveViewerPlayer);
        List<DraftPlacementSnapshot> draftPlacements = viewerCanSeeDraft
                ? buildDraftPlacements(session.getTurnDraft())
                : List.of();

        List<PlayerClockSnapshot> playerClockSnapshots = new ArrayList<>();
        List<GamePlayerSnapshot> players = new ArrayList<>();
        for (Player player : session.getGameState().getPlayers()) {
            PlayerClock clock = player.getClock();
            PlayerClockSnapshot clockSnapshot = new PlayerClockSnapshot(
                    player.getPlayerId(),
                    player.getPlayerName(),
                    clock.getMainTimeRemainingMillis(),
                    clock.getByoYomiRemainingMillis(),
                    clock.getPhase(),
                    player.getActive());
            playerClockSnapshots.add(clockSnapshot);
            players.add(new GamePlayerSnapshot(
                    player.getPlayerId(),
                    player.getPlayerName(),
                    player.getPlayerType(),
                    player.getScore(),
                    player.getActive(),
                    player.getPlayerId().equals(currentPlayer.getPlayerId()),
                    countRackTiles(player),
                    clockSnapshot));
        }

        long snapshotTimestamp = System.currentTimeMillis();
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
                buildBoardCells(
                        session,
                        effectiveViewerPlayer,
                        viewerCanSeeDraft ? session.getTurnDraft() : null,
                        previewSnapshot),
                visibleRackTiles,
                draftPlacements,
                previewSnapshot,
                null,
                session.getLatestActionResult(),
                session.getTurnCoordinator().getSettlementResult(),
                aiRuntimeSnapshot,
                clientRuntimeSnapshot,
                snapshotTimestamp,
                snapshotTimestamp);
    }

    /**
     * Copies a snapshot while replacing render-related parts.
     *
     * @param baseSnapshot source snapshot
     * @param boardCells rendered board cells
     * @param visibleRackTiles visible rack tiles
     * @param draftPlacements draft placements
     * @param previewSnapshot preview data
     * @param aiRuntimeSnapshot AI runtime status
     * @param clientRuntimeSnapshot LAN client runtime status
     * @return copied snapshot
     */
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
                baseSnapshot.getTutorial(),
                baseSnapshot.getLatestActionResult(),
                baseSnapshot.getSettlementResult(),
                aiRuntimeSnapshot,
                clientRuntimeSnapshot,
                baseSnapshot.getSnapshotSentAtEpochMillis(),
                baseSnapshot.getSnapshotReceivedAtEpochMillis());
    }

    /**
     * Builds rack tile snapshots visible to one player.
     *
     * @param player player whose rack is shown
     * @return rack tile snapshots
     */
    private static List<RackTileSnapshot> buildVisibleRackTiles(Player player) {
        List<RackTileSnapshot> visibleRackTiles = new ArrayList<>();
        for (RackSlot slot : player.getRack().getSlots()) {
            Tile tile = slot.getTile();
            visibleRackTiles.add(new RackTileSnapshot(
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

    /**
     * Estimates network delay stored inside a snapshot.
     *
     * @param snapshot snapshot with sent and received timestamps
     * @return non-negative delay in milliseconds
     */
    private static long resolveTransportDelayMillis(GameSessionSnapshot snapshot) {
        long delayMillis = snapshot.getSnapshotReceivedAtEpochMillis()
                - snapshot.getSnapshotSentAtEpochMillis();
        return Math.max(0L, delayMillis);
    }

    /**
     * Adds two long values while avoiding overflow.
     *
     * @param left first value
     * @param right second value
     * @return sum or {@link Long#MAX_VALUE} on overflow
     */
    private static long safeAdd(long left, long right) {
        if (left >= Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    /**
     * Applies elapsed time to a clock and returns predicted values.
     *
     * @param mainTimeRemainingMillis remaining main time
     * @param byoYomiRemainingMillis remaining byo-yomi time
     * @param phase current clock phase
     * @param elapsedMillis elapsed time to apply
     * @return predicted clock state
     */
    private static PredictedClock applyElapsedToClock(
            long mainTimeRemainingMillis,
            long byoYomiRemainingMillis,
            ClockPhase phase,
            long elapsedMillis) {
        long predictedMainTimeRemainingMillis = Math.max(0L, mainTimeRemainingMillis);
        long predictedByoYomiRemainingMillis = Math.max(0L, byoYomiRemainingMillis);
        ClockPhase predictedPhase = Objects.requireNonNull(phase, "phase cannot be null.");
        long remainingElapsedMillis = elapsedMillis;

        if (predictedPhase == ClockPhase.MAIN_TIME) {
            long consumedMainTimeMillis = Math.min(predictedMainTimeRemainingMillis, remainingElapsedMillis);
            predictedMainTimeRemainingMillis -= consumedMainTimeMillis;
            remainingElapsedMillis -= consumedMainTimeMillis;
            if (predictedMainTimeRemainingMillis == 0L) {
                predictedPhase = ClockPhase.BYO_YOMI;
            } else {
                return new PredictedClock(
                        predictedMainTimeRemainingMillis,
                        predictedByoYomiRemainingMillis,
                        predictedPhase);
            }
        }

        if (predictedPhase == ClockPhase.BYO_YOMI) {
            long predictedRemainingByoYomiMillis =
                    predictedByoYomiRemainingMillis - remainingElapsedMillis;
            if (predictedRemainingByoYomiMillis <= 0L) {
                return new PredictedClock(0L, 0L, ClockPhase.TIMEOUT);
            }
            return new PredictedClock(
                    predictedMainTimeRemainingMillis,
                    predictedRemainingByoYomiMillis,
                    ClockPhase.BYO_YOMI);
        }

        return new PredictedClock(
                predictedMainTimeRemainingMillis,
                predictedByoYomiRemainingMillis,
                predictedPhase);
    }

    /**
     * Builds board render cells from a live game session.
     *
     * @param session source session
     * @param viewerPlayer viewer whose rack may contain draft tiles
     * @param turnDraft visible turn draft
     * @param previewSnapshot visible preview data
     * @return render cell snapshots
     */
    private static List<BoardCellRenderSnapshot> buildBoardCells(
            GameSession session,
            Player viewerPlayer,
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

        Map<String, Tile> viewerRackTilesById = new HashMap<>();
        if (viewerPlayer != null) {
            for (RackSlot slot : viewerPlayer.getRack().getSlots()) {
                Tile tile = slot.getTile();
                if (tile != null) {
                    viewerRackTilesById.put(tile.getTileID(), tile);
                }
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
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                Position position = new Position(row, col);
                Cell cell = session.getGameState().getBoard().getCell(position);
                DraftPlacement draftPlacement = draftPlacementsByPosition.get(new BoardPositionKey(row, col));
                BoardPositionKey key = new BoardPositionKey(row, col);

                if (!cell.isEmpty()) {
                    Tile tile = cell.getPlacedTile();
                    boardCells.add(new BoardCellRenderSnapshot(
                            row,
                            col,
                            cell.getBonusType(),
                            tile.getTileID(),
                            resolveDisplayLetter(tile),
                            tile.getScore(),
                            tile.isBlank(),
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

                Tile tile = viewerRackTilesById.get(draftPlacement.getTileId());
                if (tile == null) {
                    continue;
                }

                boardCells.add(new BoardCellRenderSnapshot(
                        row,
                        col,
                        cell.getBonusType(),
                        tile.getTileID(),
                        resolveDisplayLetter(tile),
                        tile.getScore(),
                        tile.isBlank(),
                        true,
                        previewValidPositions.contains(key),
                        previewInvalidPositions.contains(key),
                        mainWordPositions.contains(key),
                        crossWordPositions.contains(key)));
            }
        }
        return boardCells;
    }

    /**
     * Rebuilds visible rack tiles after applying local blank-letter choices.
     *
     * @param baseSnapshot source snapshot
     * @param turnDraft local draft
     * @return updated rack tile snapshots
     */
    private static List<RackTileSnapshot> buildVisibleRackTiles(
            GameSessionSnapshot baseSnapshot,
            TurnDraft turnDraft) {
        List<RackTileSnapshot> visibleRackTiles = new ArrayList<>();
        for (RackTileSnapshot visibleRackTile : baseSnapshot.getVisibleRackTiles()) {
            
            Character assignedLetter = turnDraft.getAssignedLettersByTileId().get(visibleRackTile.getTileId());
            if (!visibleRackTile.isBlank() || assignedLetter == null) {
                visibleRackTiles.add(visibleRackTile);
                continue;
            }

            visibleRackTiles.add(new RackTileSnapshot(
                    visibleRackTile.getSlotIndex(),
                    visibleRackTile.getTileId(),
                    visibleRackTile.getLetter(),
                    assignedLetter,
                    visibleRackTile.getScore(),
                    true,
                    assignedLetter));
        }
        return visibleRackTiles;
    }

    /**
     * Builds board render cells by applying a local draft to a base snapshot.
     *
     * @param baseSnapshot source snapshot
     * @param turnDraft local draft
     * @param previewSnapshot local preview data
     * @param visibleRackTiles rack tiles visible to the local player
     * @return render cell snapshots
     */
    private static List<BoardCellRenderSnapshot> buildBoardCells(
            GameSessionSnapshot baseSnapshot,
            TurnDraft turnDraft,
            PreviewSnapshot previewSnapshot,
            List<RackTileSnapshot> visibleRackTiles) {
        Map<BoardPositionKey, DraftPlacement> draftPlacementsByPosition = new HashMap<>();
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

        Map<String, RackTileSnapshot> visibleRackTilesById = new HashMap<>();
        for (RackTileSnapshot visibleRackTile : visibleRackTiles) {
            if (visibleRackTile.getTileId() != null) {
                visibleRackTilesById.put(visibleRackTile.getTileId(), visibleRackTile);
            }
        }

        Map<BoardPositionKey, BoardCellRenderSnapshot> committedBoardCellsByPosition = new HashMap<>();
        for (BoardCellRenderSnapshot boardCell : baseSnapshot.getBoardCells()) {
            if (!boardCell.isDraft()) {
                committedBoardCellsByPosition.put(
                        new BoardPositionKey(boardCell.getRow(), boardCell.getCol()),
                        boardCell);
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
        for (BoardCellSnapshot boardCellSnapshot : baseSnapshot.getBoardSnapshot().getCells()) {
            int row = boardCellSnapshot.getRow();
            int col = boardCellSnapshot.getCol();
            BoardPositionKey key = new BoardPositionKey(row, col);
            BoardCellRenderSnapshot committedCell = committedBoardCellsByPosition.get(key);
            if (committedCell != null) {
                boardCells.add(new BoardCellRenderSnapshot(
                        row,
                        col,
                        committedCell.getBonusType(),
                        committedCell.getTileId(),
                        committedCell.getDisplayLetter(),
                        committedCell.getScore(),
                        committedCell.isBlank(),
                        false,
                        previewValidPositions.contains(key),
                        previewInvalidPositions.contains(key),
                        mainWordPositions.contains(key),
                        crossWordPositions.contains(key)));
                continue;
            }

            DraftPlacement draftPlacement = draftPlacementsByPosition.get(key);
            if (draftPlacement == null) {
                continue;
            }

            RackTileSnapshot visibleRackTile = visibleRackTilesById.get(draftPlacement.getTileId());
            if (visibleRackTile == null) {
                continue;
            }

            boardCells.add(new BoardCellRenderSnapshot(
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

    /**
     * Converts draft placements into snapshot objects.
     *
     * @param turnDraft source draft
     * @return draft placement snapshots
     */
    private static List<DraftPlacementSnapshot> buildDraftPlacements(TurnDraft turnDraft) {
        List<DraftPlacementSnapshot> draftPlacements = new ArrayList<>();
        for (DraftPlacement placement : turnDraft.getPlacements()) {
            draftPlacements.add(new DraftPlacementSnapshot(
                    placement.getTileId(),
                    placement.getPosition().getRow(),
                    placement.getPosition().getCol()));
        }
        return draftPlacements;
    }

    /**
     * Converts preview result into a serializable snapshot.
     *
     * @param previewResult source preview result
     * @return preview snapshot, or {@code null}
     */
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

    /**
     * Converts preview words into snapshot objects.
     *
     * @param previewResult source preview result
     * @return preview word snapshots
     */
    private static List<PreviewWordSnapshot> buildPreviewWords(PreviewResult previewResult) {
        List<PreviewWordSnapshot> words = new ArrayList<>();
        if (previewResult.getWordList() == null) {
            return words;
        }

        for (PreviewWord word : previewResult.getWordList()) {
            if (word == null) {
                continue;
            }
            words.add(new PreviewWordSnapshot(
                    word.getWord(),
                    word.isValid(),
                    word.getScoreContribution(),
                    buildPreviewPositions(word.getCoveredPositions()),
                    word.getWordType()));
        }
        return words;
    }

    /**
     * Converts preview highlights into snapshot objects.
     *
     * @param previewResult source preview result
     * @return preview highlight snapshots
     */
    private static List<PreviewHighlightSnapshot> buildPreviewHighlights(PreviewResult previewResult) {
        List<PreviewHighlightSnapshot> highlights = new ArrayList<>();
        if (previewResult.getHighlights() == null) {
            return highlights;
        }

        for (BoardHighlight highlight : previewResult.getHighlights()) {
            if (highlight == null || highlight.getPosition() == null) {
                continue;
            }
            highlights.add(new PreviewHighlightSnapshot(
                    highlight.getPosition().getRow(),
                    highlight.getPosition().getCol(),
                    highlight.getHighlightType()));
        }
        return highlights;
    }

    /**
     * Converts domain positions into preview position snapshots.
     *
     * @param positions source positions
     * @return preview position snapshots
     */
    private static List<PreviewPositionSnapshot> buildPreviewPositions(List<Position> positions) {
        List<PreviewPositionSnapshot> coveredPositions = new ArrayList<>();
        if (positions == null) {
            return coveredPositions;
        }

        for (Position position : positions) {
            if (position == null) {
                continue;
            }
            coveredPositions.add(new PreviewPositionSnapshot(position.getRow(), position.getCol()));
        }
        return coveredPositions;
    }

    /**
     * Finds the player used as the current snapshot player.
     *
     * @param session source session
     * @return current or fallback player
     */
    private static Player resolveSnapshotPlayer(GameSession session) {
        if (session.getGameState().hasActivePlayers()) {
            return session.getGameState().requireCurrentActivePlayer();
        }
        return session.getGameState().getCurrentPlayer();
    }

    /**
     * Finds the player who is viewing the snapshot.
     *
     * @param session source session
     * @param viewerPlayerId requested viewer id
     * @param fallbackPlayer fallback when no viewer is found
     * @return viewer player
     */
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

    /**
     * Counts non-empty tiles in a player's rack.
     *
     * @param player player to inspect
     * @return rack tile count
     */
    private static int countRackTiles(Player player) {
        int count = 0;
        for (RackSlot slot : player.getRack().getSlots()) {
            if (!slot.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Collects board positions marked valid or invalid by preview highlights.
     *
     * @param previewSnapshot preview snapshot to read
     * @param previewValidPositions output set for valid positions
     * @param previewInvalidPositions output set for invalid positions
     */
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

    /**
     * Collects board positions covered by main and cross words.
     *
     * @param previewSnapshot preview snapshot to read
     * @param mainWordPositions output set for main word positions
     * @param crossWordPositions output set for cross word positions
     */
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

    /**
     * Resolves the letter that should be displayed for a tile.
     *
     * @param tile tile to inspect
     * @return assigned blank letter or normal tile letter
     */
    private static Character resolveDisplayLetter(Tile tile) {
        Objects.requireNonNull(tile, "tile cannot be null.");
        if (tile.isBlank() && tile.getAssignedLetter() != null) {
            return tile.getAssignedLetter();
        }
        return tile.getLetter();
    }

    /**
     * Simple key used for board-position maps.
     *
     * @param row board row
     * @param col board column
     */
    private record BoardPositionKey(int row, int col) {
    }

    /**
     * Predicted clock values after applying elapsed time.
     *
     * @param mainTimeRemainingMillis predicted main time
     * @param byoYomiRemainingMillis predicted byo-yomi time
     * @param phase predicted clock phase
     */
    private record PredictedClock(
            long mainTimeRemainingMillis,
            long byoYomiRemainingMillis,
            ClockPhase phase) {
    }
}
