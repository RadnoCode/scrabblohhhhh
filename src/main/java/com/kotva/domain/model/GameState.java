package com.kotva.domain.model;

import com.kotva.domain.endgame.GameEndReason;
import java.util.List;
import java.util.Objects;

public class GameState {
    private final Board board;
    private final TileBag tileBag;
    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean gameOver;
    private GameEndReason gameEndReason;
    private boolean firstMoveMade;

    public GameState(List<Player> players) {
        this.players = List.copyOf(players);
        this.board = new Board();
        this.tileBag = new TileBag();
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.gameEndReason = null;
        this.firstMoveMade = false;
    }

    public Board getBoard() {
        return board;
    }

    public TileBag getTileBag() {
        return tileBag;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Player requireCurrentActivePlayer() {
        if (!hasActivePlayers()) {
            throw new IllegalStateException("No active players left.");
        }

        for (int checked = 0; checked < players.size(); checked++) {
            int index = (currentPlayerIndex + checked) % players.size();
            Player candidate = players.get(index);
            if (candidate.getActive()) {
                currentPlayerIndex = index;
                return candidate;
            }
        }

        throw new IllegalStateException("No active player found in player list.");
    }

    public void advanceToNextActivePlayer() {
        if (!hasActivePlayers()) {
            return;
        }

        for (int offset = 1; offset <= players.size(); offset++) {
            int index = (currentPlayerIndex + offset) % players.size();
            if (players.get(index).getActive()) {
                currentPlayerIndex = index;
                return;
            }
        }
    }

    public int getActivePlayerCount() {
        int activePlayerCount = 0;
        for (Player player : players) {
            if (player.getActive()) {
                activePlayerCount++;
            }
        }
        return activePlayerCount;
    }

    public boolean hasActivePlayers() {
        return getActivePlayerCount() > 0;
    }

    public Player getPlayerById(String playerId) {
        Objects.requireNonNull(playerId, "playerId cannot be null.");
        for (Player player : players) {
            if (playerId.equals(player.getPlayerId())) {
                return player;
            }
        }
        return null;
    }

    public void nextTurn() {
        advanceToNextActivePlayer();
    }

    public void markGameOver(GameEndReason gameEndReason) {
        this.gameOver = true;
        this.gameEndReason = Objects.requireNonNull(gameEndReason, "gameEndReason cannot be null.");
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isFirstMoveMade() {
        return firstMoveMade;
    }

    public void markFirstMoveMade() {
        this.firstMoveMade = true;
    }

    public GameEndReason getGameEndReason() {
        return gameEndReason;
    }

    public void initialDraw() {
        for (Player player : players) {
            for (int slotIndex = 0; slotIndex < 7; slotIndex++) {
                Tile newTile = tileBag.drawTile();
                player.getRack().setTileAt(slotIndex, newTile);
            }
        }
    }
}