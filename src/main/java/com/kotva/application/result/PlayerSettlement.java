package com.kotva.application.result;

import java.util.Objects;

public class PlayerSettlement {
    private final String playerName;
    private final int finalScore;
    private final int rank;

    public PlayerSettlement(String playerName, int finalScore, int rank) {
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
        this.finalScore = finalScore;
        this.rank = rank;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public int getRank() {
        return rank;
    }
}
