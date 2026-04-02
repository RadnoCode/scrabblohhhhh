package com.kotva.domain.model;

import java.util.List;

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
