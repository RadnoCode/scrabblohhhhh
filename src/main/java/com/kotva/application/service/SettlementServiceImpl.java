package com.kotva.application.service;

import com.kotva.application.result.BoardCellSnapshot;
import com.kotva.application.result.BoardSnapshot;
import com.kotva.application.result.BoardSnapshotFactory;
import com.kotva.application.result.PlayerSettlement;
import com.kotva.application.result.SettlementResult;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SettlementServiceImpl implements SettlementService {
    private final SettlementNavigationPort settlementNavigationPort;

    public SettlementServiceImpl() {
        this(new NoOpSettlementNavigationPort());
    }

    public SettlementServiceImpl(SettlementNavigationPort settlementNavigationPort) {
        this.settlementNavigationPort =
                Objects.requireNonNull(
                        settlementNavigationPort, "settlementNavigationPort cannot be null.");
    }

    @Override
    public SettlementResult settle(GameState gameState, GameEndReason endReason) {
        Objects.requireNonNull(gameState, "gameState cannot be null.");
        Objects.requireNonNull(endReason, "endReason cannot be null.");

        SettlementResult result =
                new SettlementResult(
                        endReason,
                        buildRankings(gameState),
                        buildSummaryMessages(gameState, endReason),
                        BoardSnapshotFactory.fromBoard(gameState.getBoard()));
        settlementNavigationPort.showSettlement(result);
        return result;
    }

    private List<PlayerSettlement> buildRankings(GameState gameState) {
        List<Player> sortedPlayers = new ArrayList<>(gameState.getPlayers());
        sortedPlayers.sort(Comparator.comparingInt(Player::getScore).reversed());

        List<PlayerSettlement> rankings = new ArrayList<>();
        int lastScore = Integer.MIN_VALUE;
        int currentRank = 0;

        for (int index = 0; index < sortedPlayers.size(); index++) {
            Player player = sortedPlayers.get(index);
            if (player.getScore() != lastScore) {
                currentRank = index + 1;
                lastScore = player.getScore();
            }
            rankings.add(new PlayerSettlement(player.getPlayerName(), player.getScore(), currentRank));
        }

        return rankings;
    }

    private List<String> buildSummaryMessages(GameState gameState, GameEndReason endReason) {
        List<String> messages = new ArrayList<>();
        messages.add(buildEndReasonMessage(endReason));

        List<PlayerSettlement> rankings = buildRankings(gameState);
        if (rankings.isEmpty()) {
            messages.add("No players are available for settlement.");
            return messages;
        }

        int winningRank = rankings.get(0).getRank();
        List<String> topNames =
                rankings.stream()
                        .filter(playerSettlement -> playerSettlement.getRank() == winningRank)
                        .map(PlayerSettlement::getPlayerName)
                        .collect(Collectors.toList());
        if (topNames.size() == 1) {
            messages.add("Winner: " + topNames.get(0));
        } else {
            messages.add("Shared first place: " + String.join(", ", topNames));
        }
        return messages;
    }

    private String buildEndReasonMessage(GameEndReason endReason) {
        return switch (endReason) {
            case ALL_PLAYERS_PASSED -> "Game ended because all active players passed in the round.";
            case ONLY_ONE_PLAYER_REMAINING -> "Game ended because only one active player remained.";
            case TILE_BAG_EMPTY_AND_PLAYER_FINISHED ->
                    "Game ended because the tile bag was empty and a player emptied their rack.";
            case BOARD_FULL -> "Game ended because the board became full.";
            case TARGET_SCORE_REACHED -> "Game ended because the target score was reached.";
            case NO_LEGAL_PLACEMENT_AVAILABLE ->
                    "Game ended because no legal placement was available.";
            case NORMAL_FINISH -> "Game ended normally.";
        };
    }
}
