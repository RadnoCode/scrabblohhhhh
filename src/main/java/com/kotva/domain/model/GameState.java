package com.kotva.domain.model;

import java.util.List;

/**
 * The central hub for the current state of the game.
 * * This class keeps track of everything happening in the match: the game board,
 * the infinite tile bag, all the players, and whose turn it is right now.
 * It also handles basic game flow, like moving to the next player (nextTurn)
 * and dealing the starting 7 tiles to everyone (initialDraw).
 */
public class GameState {
    private Board board;
    private TileBag tileBag;
    private List<Player> players;
    private int currentPlayerIndex;

    public GameState(List<Player> players) {
        this.players = players;
        this.board = new Board();
        this.tileBag = new TileBag();
        this.currentPlayerIndex = 0;
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

    public void nextTurn(){
        currentPlayerIndex =(currentPlayerIndex + 1) % players.size();
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
