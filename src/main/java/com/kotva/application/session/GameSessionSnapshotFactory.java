package com.kotva.application.session;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.preview.BoardHighlight;
import com.kotva.application.preview.HighlightType;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.preview.PreviewWord;
import com.kotva.application.result.BoardSnapshotFactory;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
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
        Objects.requireNonNull(session, "session cannot be null.");

        Player currentPlayer = resolveSnapshotPlayer(session);
        PlayerClock currentClock = currentPlayer.getClock();
        PreviewSnapshot previewSnapshot = buildPreviewSnapshot(session);

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
                BoardSnapshotFactory.fromBoard(session.getGameState().getBoard()),
                buildBoardCells(session, currentPlayer, previewSnapshot),
                buildCurrentRackTiles(currentPlayer),
                buildDraftPlacements(session),
                previewSnapshot,
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
                            tile != null ? resolveDisplayLetter(tile) : null,
                            tile != null ? tile.getScore() : 0,
                            tile != null && tile.isBlank(),
                            tile != null ? tile.getAssignedLetter() : null));
        }
        return currentRackTiles;
    }

    private static List<BoardCellRenderSnapshot> buildBoardCells(
            GameSession session, Player currentPlayer, PreviewSnapshot previewSnapshot) {
        Map<BoardPositionKey, DraftPlacement> draftPlacementsByPosition = new HashMap<>();
        for (DraftPlacement placement : session.getTurnDraft().getPlacements()) {
            if (placement == null || placement.getPosition() == null) {
                continue;
            }
            draftPlacementsByPosition.put(
                    new BoardPositionKey(
                            placement.getPosition().getRow(), placement.getPosition().getCol()),
                    placement);
        }

        Map<String, Tile> currentRackTilesById = new HashMap<>();
        for (RackSlot slot : currentPlayer.getRack().getSlots()) {
            Tile tile = slot.getTile();
            if (tile != null) {
                currentRackTilesById.put(tile.getTileID(), tile);
            }
        }

        Set<BoardPositionKey> previewValidPositions = new HashSet<>();
        Set<BoardPositionKey> previewInvalidPositions = new HashSet<>();
        collectPreviewHighlightPositions(previewSnapshot, previewValidPositions, previewInvalidPositions);

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

                Tile tile = currentRackTilesById.get(draftPlacement.getTileId());
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
