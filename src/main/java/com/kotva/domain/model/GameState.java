package com.kotva.domain.model;

import java.util.List;
import java.util.Objects;

import com.kotva.application.result.GameEndReason;

/**
 * The central hub for the current state of the game.
 * * This class keeps track of everything happening in the match: the game board,
 * the infinite tile bag, all the players, and whose turn it is right now.
 * It also handles basic game flow, like moving to the next player (nextTurn)
 * and dealing the starting 7 tiles to everyone (initialDraw).
 */
public class GameState {
    private final Board board;  // The game board
    private final TileBag tileBag;  // The infinite tile bag
    private final List<Player> players;  // All players in the game, with their current state (rack, score, active status)
    private int currentPlayerIndex;  // Index in the players list indicating whose turn it is
    private boolean gameOver;  // Flag to indicate if the game has ended
    private GameEndReason gameEndReason;  // Reason for game end, including    ALL_PLAYERS_PASSED, ONLY_ONE_PLAYER_REMAINING, TILE_BAG_EMPTY_AND_PLAYER_FINISHED, BOARD_FULL,NO_LEGAL_PLACEMENT_AVAILABLE, NORMAL_FINISH

    public GameState(List<Player> players) {
        this.players = List.copyOf(players);
        //TODO: Add a method to initialize players based on game config. Controller, id,and list.
        this.board = new Board();
        this.tileBag = new TileBag();
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.gameEndReason = null;
    }

    public Board getBoard(){
        return board;
    }

    public TileBag getTileBag(){
        return tileBag;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getCurrentPlayerIndex(){
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

    public void advanceToNextActivePlayer(){
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

    public void nextTurn(){
        advanceToNextActivePlayer();
    }

    public void markGameOver(GameEndReason gameEndReason) {
        this.gameOver = true;
        this.gameEndReason = Objects.requireNonNull(gameEndReason, "gameEndReason cannot be null.");
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public GameEndReason getGameEndReason() {
        return gameEndReason;
    }

    public void initialDraw(){
        for(int i = 0;i<players.size();i++){
            Player player = players.get(i);
            for(int j=0;j<7;j++){
                Tile newTile = tileBag.drawTile();
                player.getRack().setTileAt(j, newTile);

            }
        }
    }
}
