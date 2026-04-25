package com.kotva.application.service.client;

import com.kotva.application.session.BoardCellRenderSnapshot;
import com.kotva.application.session.GamePlayerSnapshot;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.RackTileSnapshot;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rebuilds a lightweight domain game state from a client snapshot for preview.
 */
final class ClientPreviewStateFactory {
    /**
     * Prevents creating this utility class.
     */
    private ClientPreviewStateFactory() {
    }

    /**
     * Builds a preview game state from the latest snapshot.
     *
     * @param snapshot source snapshot
     * @param localPlayerId local player id
     * @return rebuilt game state
     */
    static GameState fromSnapshot(GameSessionSnapshot snapshot, String localPlayerId) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        Objects.requireNonNull(localPlayerId, "localPlayerId cannot be null.");

        GameState gameState = new GameState(buildPlayers(snapshot, localPlayerId));
        restoreBoard(gameState, snapshot);
        restoreFirstMoveState(gameState);
        restoreRack(gameState, snapshot, localPlayerId);
        return gameState;
    }

    /**
     * Builds players with the local player first.
     *
     * @param snapshot source snapshot
     * @param localPlayerId local player id
     * @return domain players
     */
    private static List<Player> buildPlayers(GameSessionSnapshot snapshot, String localPlayerId) {
        List<Player> players = new ArrayList<>();
        GamePlayerSnapshot localPlayer = findPlayer(snapshot, localPlayerId);
        if (localPlayer != null) {
            players.add(toPlayer(localPlayer));
        }

        for (GamePlayerSnapshot playerSnapshot : snapshot.getPlayers()) {
            if (localPlayer != null
                    && Objects.equals(localPlayer.getPlayerId(), playerSnapshot.getPlayerId())) {
                continue;
            }
            players.add(toPlayer(playerSnapshot));
        }

        if (players.isEmpty()) {
            throw new IllegalStateException("Snapshot does not contain any players.");
        }
        return players;
    }

    /**
     * Finds a player snapshot by id.
     *
     * @param snapshot source snapshot
     * @param playerId player id
     * @return player snapshot, or {@code null}
     */
    private static GamePlayerSnapshot findPlayer(GameSessionSnapshot snapshot, String playerId) {
        for (GamePlayerSnapshot playerSnapshot : snapshot.getPlayers()) {
            if (Objects.equals(playerId, playerSnapshot.getPlayerId())) {
                return playerSnapshot;
            }
        }
        return null;
    }

    /**
     * Converts a player snapshot into a domain player.
     *
     * @param snapshot player snapshot
     * @return domain player
     */
    private static Player toPlayer(GamePlayerSnapshot snapshot) {
        Player player =
                new Player(
                        snapshot.getPlayerId(),
                        snapshot.getPlayerName(),
                        snapshot.getPlayerType());
        player.addScore(snapshot.getScore());
        player.setActive(snapshot.isActive());
        return player;
    }

    /**
     * Restores committed board tiles from a snapshot.
     *
     * @param gameState game state to modify
     * @param snapshot source snapshot
     */
    private static void restoreBoard(GameState gameState, GameSessionSnapshot snapshot) {
        for (BoardCellRenderSnapshot boardCell : snapshot.getBoardCells()) {
            if (boardCell.isDraft()) {
                continue;
            }
            Tile tile = toBoardTile(boardCell);
            gameState.getTileBag().indexTile(tile);
            gameState
                    .getBoard()
                    .getCell(new Position(boardCell.getRow(), boardCell.getCol()))
                    .setPlacedTile(tile);
        }
    }

    /**
     * Marks the first move as made when the restored board is not empty.
     *
     * @param gameState game state to update
     */
    private static void restoreFirstMoveState(GameState gameState) {
        if (!gameState.getBoard().isEmpty()) {
            gameState.markFirstMoveMade();
        }
    }

    /**
     * Converts a rendered board cell into a domain tile.
     *
     * @param boardCell board cell snapshot
     * @return domain tile
     */
    private static Tile toBoardTile(BoardCellRenderSnapshot boardCell) {
        if (boardCell.isBlank()) {
            Tile tile = new Tile(boardCell.getTileId(), ' ', boardCell.getScore(), true);
            if (boardCell.getDisplayLetter() != null) {
                tile.setAssignedLetter(boardCell.getDisplayLetter());
            }
            return tile;
        }
        return new Tile(
                boardCell.getTileId(),
                boardCell.getDisplayLetter(),
                boardCell.getScore(),
                false);
    }

    /**
     * Restores the local player's rack from visible rack tiles.
     *
     * @param gameState game state to modify
     * @param snapshot source snapshot
     * @param localPlayerId local player id
     */
    private static void restoreRack(
            GameState gameState,
            GameSessionSnapshot snapshot,
            String localPlayerId) {
        Player localPlayer = gameState.getPlayerById(localPlayerId);
        if (localPlayer == null) {
            throw new IllegalStateException("Snapshot does not contain local player " + localPlayerId + ".");
        }

        for (RackTileSnapshot rackTileSnapshot : snapshot.getVisibleRackTiles()) {
            if (rackTileSnapshot.getTileId() == null || rackTileSnapshot.getLetter() == null) {
                continue;
            }

            Tile tile = toRackTile(rackTileSnapshot);
            gameState.getTileBag().indexTile(tile);
            localPlayer.getRack().setTileAt(rackTileSnapshot.getSlotIndex(), tile);
        }
    }

    /**
     * Converts a rack snapshot into a domain tile.
     *
     * @param snapshot rack tile snapshot
     * @return domain tile
     */
    private static Tile toRackTile(RackTileSnapshot snapshot) {
        if (snapshot.isBlank()) {
            Tile tile = new Tile(snapshot.getTileId(), ' ', snapshot.getScore(), true);
            if (snapshot.getAssignedLetter() != null) {
                tile.setAssignedLetter(snapshot.getAssignedLetter());
            }
            return tile;
        }
        return new Tile(snapshot.getTileId(), snapshot.getLetter(), snapshot.getScore(), false);
    }
}
