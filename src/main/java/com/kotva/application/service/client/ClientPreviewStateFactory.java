package com.kotva.application.service.client;

import com.kotva.application.result.BoardCellSnapshot;
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

    static GameState fromSnapshot(GameSessionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");

        GameState gameState = new GameState(buildPlayers(snapshot));
        restoreBoard(gameState, snapshot);
        restoreRack(gameState, snapshot);
        return gameState;
    }

    private static List<Player> buildPlayers(GameSessionSnapshot snapshot) {
        List<Player> players = new ArrayList<>();

        GamePlayerSnapshot currentPlayer = findCurrentPlayer(snapshot);
        if (currentPlayer != null) {
            players.add(toPlayer(currentPlayer));
        }

        for (GamePlayerSnapshot playerSnapshot : snapshot.getPlayers()) {
            if (currentPlayer != null
                    && Objects.equals(
                            currentPlayer.getPlayerId(), playerSnapshot.getPlayerId())) {
                continue;
            }
            players.add(toPlayer(playerSnapshot));
        }

        if (players.isEmpty()) {
            throw new IllegalStateException("Snapshot does not contain any players.");
        }
        return players;
    }

    private static GamePlayerSnapshot findCurrentPlayer(GameSessionSnapshot snapshot) {
        for (GamePlayerSnapshot playerSnapshot : snapshot.getPlayers()) {
            if (Objects.equals(snapshot.getCurrentPlayerId(), playerSnapshot.getPlayerId())) {
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
        for (BoardCellSnapshot cellSnapshot : snapshot.getBoardSnapshot().getCells()) {
            if (cellSnapshot.getLetter() == null) {
                continue;
            }

            Tile tile = toBoardTile(cellSnapshot);
            gameState.getTileBag().registerTile(tile);
            gameState
                    .getBoard()
                    .getCell(new Position(cellSnapshot.getRow(), cellSnapshot.getCol()))
                    .setPlacedTile(tile);
        }
    }

    private static Tile toBoardTile(BoardCellSnapshot cellSnapshot) {
        String tileId = "__snapshot_board_" + cellSnapshot.getRow() + "_" + cellSnapshot.getCol();
        if (cellSnapshot.isBlank()) {
            Tile tile = new Tile(tileId, ' ', cellSnapshot.getScore(), true);
            tile.setAssignedLetter(cellSnapshot.getLetter());
            return tile;
        }
        return new Tile(tileId, cellSnapshot.getLetter(), cellSnapshot.getScore(), false);
    }

    private static void restoreRack(GameState gameState, GameSessionSnapshot snapshot) {
        Player currentPlayer = gameState.requireCurrentActivePlayer();
        for (RackTileSnapshot rackTileSnapshot : snapshot.getCurrentRackTiles()) {
            if (rackTileSnapshot.getTileId() == null || rackTileSnapshot.getLetter() == null) {
                continue;
            }

            Tile tile = toRackTile(rackTileSnapshot);
            gameState.getTileBag().registerTile(tile);
            currentPlayer.getRack().setTileAt(rackTileSnapshot.getSlotIndex(), tile);
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
