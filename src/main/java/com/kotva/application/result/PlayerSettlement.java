package com.kotva.application.result;

import java.io.Serializable;
import java.util.Objects;

/**
 * Stores one player's final ranking information.
 */
public class PlayerSettlement implements Serializable {
    private final String playerName;
    private final int finalScore;
    private final int rank;

    /**
     * Creates a player settlement row.
     *
     * @param playerName player display name
     * @param finalScore final score after settlement
     * @param rank final rank
     */
    public PlayerSettlement(String playerName, int finalScore, int rank) {
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
        this.finalScore = finalScore;
        this.rank = rank;
    }

    /**
     * Gets the player name.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the final score.
     *
     * @return final score
     */
    public int getFinalScore() {
        return finalScore;
    }

    /**
     * Gets the final rank.
     *
     * @return final rank
     */
    public int getRank() {
        return rank;
    }
}
