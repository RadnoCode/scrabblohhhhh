package com.kotva.domain.model;

import com.kotva.application.result.GameEndReason;
import java.util.List;
import java.util.Objects;

/**
 * The central hub for the current state of the game.
 * * This class keeps track of everything happening in the match: the game board,
 * the infinite tile bag, all the players, and whose turn it is right now.
 * It also handles basic game flow, like moving to the next player (nextTurn)
 * and dealing the starting 7 tiles to everyone (initialDraw).
 */
public class GameState {
    private final Board board;
    private final TileBag tileBag;
    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean gameOver;
    private GameEndReason gameEndReason;
    private int consecutivePasses; // 记录全场连续跳过的次数

    public GameState(List<Player> players) {
        this.players = List.copyOf(players);
        //TODO: Add a method to initialize players based on game config. Controller, id,and list.
        this.board = new Board();
        this.tileBag = new TileBag();
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.gameEndReason = null;
        this.consecutivePasses = 0;
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

    /**
     * 获取当前连续跳过的总次数（给 EndEvaluator 裁判用的）
     */
    public int getConsecutivePasses() {
        return consecutivePasses;
    }

    /**
     * 当有玩家跳过时调用，次数 +1
     */
    public void incrementPass() {
        this.consecutivePasses++;
    }

    /**
     * 当有玩家正常下棋（或换牌）时调用，打断了跳过链，次数清零
     */
    public void resetPasses() {
        this.consecutivePasses = 0;
    }
}
