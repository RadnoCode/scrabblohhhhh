package com.kotva.presentation.viewmodel;

import com.kotva.application.result.PlayerSettlement;
import com.kotva.application.result.SettlementResult;
import com.kotva.domain.endgame.GameEndReason;
import java.util.List;
import java.util.Objects;

/**
 * Stores data for the final result screen.
 */
public final class SettlementViewModel {
    private static final String DEFAULT_REASON_CAPTION = "GAME END REASON";
    private static final String DEFAULT_PODIUM_TEXT = "PODIUM";
    private static final String DEFAULT_HOME_BUTTON_TEXT = "Return to Home";
    private static final String DEFAULT_EXPORT_BUTTON_TEXT = "Save PNG";

    private final String reasonCaptionText;
    private final String reasonText;
    private final String reasonDetailText;
    private final String podiumText;
    private final String summaryText;
    private final String homeButtonText;
    private final String exportButtonText;
    private final List<PlayerSettlement> rankings;

    private SettlementViewModel(
        String reasonCaptionText,
        String reasonText,
        String reasonDetailText,
        String podiumText,
        String summaryText,
        String homeButtonText,
        String exportButtonText,
        List<PlayerSettlement> rankings) {
        this.reasonCaptionText =
            Objects.requireNonNull(reasonCaptionText, "reasonCaptionText cannot be null.");
        this.reasonText = Objects.requireNonNull(reasonText, "reasonText cannot be null.");
        this.reasonDetailText =
            Objects.requireNonNull(reasonDetailText, "reasonDetailText cannot be null.");
        this.podiumText = Objects.requireNonNull(podiumText, "podiumText cannot be null.");
        this.summaryText = Objects.requireNonNull(summaryText, "summaryText cannot be null.");
        this.homeButtonText =
            Objects.requireNonNull(homeButtonText, "homeButtonText cannot be null.");
        this.exportButtonText =
            Objects.requireNonNull(exportButtonText, "exportButtonText cannot be null.");
        this.rankings = List.copyOf(Objects.requireNonNull(rankings, "rankings cannot be null."));
    }

    public static SettlementViewModel fromResult(SettlementResult settlementResult) {
        if (settlementResult == null) {
            return new SettlementViewModel(
                DEFAULT_REASON_CAPTION,
                "Settlement Unavailable",
                "No finished match data was captured for this page.",
                DEFAULT_PODIUM_TEXT,
                "Final standings are not available.",
                DEFAULT_HOME_BUTTON_TEXT,
                DEFAULT_EXPORT_BUTTON_TEXT,
                List.of());
        }

        List<String> summaryMessages = settlementResult.getSummaryMessages();
        String reasonDetailText =
            summaryMessages.isEmpty()
                ? buildReasonDetail(settlementResult.getEndReason())
                : summaryMessages.get(0);
        String summaryText =
            summaryMessages.size() >= 2
                ? summaryMessages.get(1)
                : buildSummaryText(settlementResult.getRankings());

        return new SettlementViewModel(
            DEFAULT_REASON_CAPTION,
            formatEndReason(settlementResult.getEndReason()),
            reasonDetailText,
            DEFAULT_PODIUM_TEXT,
            summaryText,
            DEFAULT_HOME_BUTTON_TEXT,
            DEFAULT_EXPORT_BUTTON_TEXT,
            settlementResult.getRankings());
    }

    public String getReasonCaptionText() {
        return reasonCaptionText;
    }

    public String getReasonText() {
        return reasonText;
    }

    public String getReasonDetailText() {
        return reasonDetailText;
    }

    public String getPodiumText() {
        return podiumText;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public String getHomeButtonText() {
        return homeButtonText;
    }

    public String getExportButtonText() {
        return exportButtonText;
    }

    public List<PlayerSettlement> getRankings() {
        return rankings;
    }

    private static String buildSummaryText(List<PlayerSettlement> rankings) {
        if (rankings == null || rankings.isEmpty()) {
            return "Final standings are not available.";
        }

        int bestRank = rankings.get(0).getRank();
        List<String> winnerNames = rankings.stream()
            .filter(settlement -> settlement.getRank() == bestRank)
            .map(PlayerSettlement::getPlayerName)
            .toList();
        return winnerNames.size() == 1
            ? "Winner: " + winnerNames.get(0)
            : "Shared first place: " + String.join(", ", winnerNames);
    }

    private static String formatEndReason(GameEndReason endReason) {
        if (endReason == null) {
            return "Unknown Finish";
        }
        return switch (endReason) {
        case ALL_PLAYERS_PASSED -> "All Players Passed";
        case ONLY_ONE_PLAYER_REMAINING -> "Only One Player Remaining";
        case TILE_BAG_EMPTY_AND_PLAYER_FINISHED -> "Rack Emptied on Empty Bag";
        case BOARD_FULL -> "Board Reached Capacity";
        case TARGET_SCORE_REACHED -> "Target Score Reached";
        case NO_LEGAL_PLACEMENT_AVAILABLE -> "No Legal Placement Available";
        case AI_RUNTIME_FAILURE -> "AI Runtime Failure";
        case NORMAL_FINISH -> "Normal Finish";
        };
    }

    private static String buildReasonDetail(GameEndReason endReason) {
        if (endReason == null) {
            return "The match ended without an explicit reason.";
        }
        return switch (endReason) {
        case ALL_PLAYERS_PASSED -> "All active players passed during the same round.";
        case ONLY_ONE_PLAYER_REMAINING -> "Only one active player remained in the match.";
        case TILE_BAG_EMPTY_AND_PLAYER_FINISHED ->
            "The tile bag was empty and one player cleared their rack.";
        case BOARD_FULL -> "No additional tiles could be placed because the board was full.";
        case TARGET_SCORE_REACHED -> "A player reached the target score threshold.";
        case NO_LEGAL_PLACEMENT_AVAILABLE -> "No legal placement remained for the current match state.";
        case AI_RUNTIME_FAILURE -> "The AI runtime failed, so the match was frozen and settled.";
        case NORMAL_FINISH -> "The match completed normally.";
        };
    }
}
