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

final class ClientPreviewStateFactory {
    private ClientPreviewStateFactory() {
    }

    static GameState fromSnapshot(GameSessionSnapshot snapshot, String localPlayerId) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        Objects.requireNonNull(localPlayerId, "localPlayerId cannot be null.");

        GameState gameState = new GameState(buildPlayers(snapshot, localPlayerId));
        restoreBoard(gameState, snapshot);
        restoreFirstMoveState(gameState);
        restoreRack(gameState, snapshot, localPlayerId);
        return gameState;
    }

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

    private static GamePlayerSnapshot findPlayer(GameSessionSnapshot snapshot, String playerId) {
        for (GamePlayerSnapshot playerSnapshot : snapshot.getPlayers()) {
            if (Objects.equals(playerId, playerSnapshot.getPlayerId())) {
                return playerSnapshot;
            }
        }
        return null;
    }

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

    private static void restoreFirstMoveState(GameState gameState) {
        if (!gameState.getBoard().isEmpty()) {
            gameState.markFirstMoveMade();
        }
    }

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
