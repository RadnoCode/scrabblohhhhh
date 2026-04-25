package com.kotva.presentation.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PlayerNameSetupContext {
    private static final String DEFAULT_TITLE = "SCRABBLE";
    private static final String DEFAULT_ROOM_NAME = "LAN Room";
    private static final String DEFAULT_HINT_TEXT = "UTF-8 nicknames, up to 8 characters each.";

    public enum Flow {
        HOT_SEAT,
        LAN_HOST,
        LAN_CLIENT
    }

    private final Flow flow;
    private final String titleText;
    private final String viceTitleText;
    private final String roomTitle;
    private final String summaryText;
    private final String hintText;
    private final String confirmButtonText;
    private final String gameTimeLabel;
    private final String stepTimeSecondsLabel;
    private final String languageLabel;
    private final String playerCountLabel;
    private final String rulesetLabel;
    private final String targetScoreLabel;
    private final String endpoint;
    private final List<String> cardTitles;
    private final List<String> defaultNames;

    private PlayerNameSetupContext(
            Flow flow,
            String viceTitleText,
            String roomTitle,
            String summaryText,
            String confirmButtonText,
            String gameTimeLabel,
            String stepTimeSecondsLabel,
            String languageLabel,
            String playerCountLabel,
            String rulesetLabel,
            String targetScoreLabel,
            String endpoint,
            List<String> cardTitles,
            List<String> defaultNames) {
        this.flow = Objects.requireNonNull(flow, "flow cannot be null.");
        this.titleText = DEFAULT_TITLE;
        this.viceTitleText = Objects.requireNonNull(viceTitleText, "viceTitleText cannot be null.");
        this.roomTitle = roomTitle == null ? "" : roomTitle.trim();
        this.summaryText = Objects.requireNonNull(summaryText, "summaryText cannot be null.");
        this.hintText = DEFAULT_HINT_TEXT;
        this.confirmButtonText =
                Objects.requireNonNull(confirmButtonText, "confirmButtonText cannot be null.");
        this.gameTimeLabel = Objects.requireNonNull(gameTimeLabel, "gameTimeLabel cannot be null.");
        this.stepTimeSecondsLabel =
                Objects.requireNonNull(stepTimeSecondsLabel, "stepTimeSecondsLabel cannot be null.");
        this.languageLabel = Objects.requireNonNull(languageLabel, "languageLabel cannot be null.");
        this.playerCountLabel =
                Objects.requireNonNull(playerCountLabel, "playerCountLabel cannot be null.");
        this.rulesetLabel = normalizeRulesetLabel(rulesetLabel);
        this.targetScoreLabel = normalizeTargetScoreLabel(targetScoreLabel);
        this.endpoint = endpoint == null ? "" : endpoint.trim();
        this.cardTitles = List.copyOf(Objects.requireNonNull(cardTitles, "cardTitles cannot be null."));
        this.defaultNames =
                List.copyOf(Objects.requireNonNull(defaultNames, "defaultNames cannot be null."));
        if (this.cardTitles.size() != this.defaultNames.size()) {
            throw new IllegalArgumentException("cardTitles size must match defaultNames size.");
        }
    }

    public static PlayerNameSetupContext forHotSeat(
            String gameTimeLabel,
            String stepTimeSecondsLabel,
            String languageLabel,
            String playerCountLabel) {
        return forHotSeat(
                gameTimeLabel,
                stepTimeSecondsLabel,
                languageLabel,
                playerCountLabel,
                "Traditional Scrabble",
                null);
    }

    public static PlayerNameSetupContext forHotSeat(
            String gameTimeLabel,
            String stepTimeSecondsLabel,
            String languageLabel,
            String playerCountLabel,
            String rulesetLabel,
            String targetScoreLabel) {
        int playerCount = Integer.parseInt(playerCountLabel);
        List<String> titles = buildSequentialLabels("Player ", playerCount);
        return new PlayerNameSetupContext(
                Flow.HOT_SEAT,
                "Player Nicknames",
                "",
                buildSettingsSummary(gameTimeLabel, languageLabel, playerCountLabel, rulesetLabel, targetScoreLabel),
                "Start Game",
                gameTimeLabel,
                stepTimeSecondsLabel,
                languageLabel,
                playerCountLabel,
                rulesetLabel,
                targetScoreLabel,
                "",
                titles,
                titles);
    }

    public static PlayerNameSetupContext forLanHost(
            String roomTitle,
            String gameTimeLabel,
            String stepTimeSecondsLabel,
            String languageLabel,
            String playerCountLabel) {
        String normalizedRoomTitle = normalizeRoomTitle(roomTitle);
        return new PlayerNameSetupContext(
                Flow.LAN_HOST,
                "Host Nickname",
                normalizedRoomTitle,
                buildSettingsSummary(gameTimeLabel, languageLabel, playerCountLabel),
                "Create Lobby",
                gameTimeLabel,
                stepTimeSecondsLabel,
                languageLabel,
                playerCountLabel,
                "Traditional Scrabble",
                null,
                "",
                List.of("Host"),
                List.of("Host"));
    }

    public static PlayerNameSetupContext forLanClient(
            String roomTitle,
            String endpoint,
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel) {
        String normalizedEndpoint = endpoint == null ? "" : endpoint.trim();
        String normalizedRoomTitle = normalizeRoomTitle(roomTitle);
        String summaryText = hasKnownRoomMetadata(gameTimeLabel, languageLabel, playerCountLabel)
                ? buildSettingsSummary(gameTimeLabel, languageLabel, playerCountLabel)
                : normalizedEndpoint;
        return new PlayerNameSetupContext(
                Flow.LAN_CLIENT,
                "Join Lobby",
                normalizedRoomTitle,
                summaryText,
                "Join Lobby",
                gameTimeLabel == null ? "--" : gameTimeLabel,
                "30",
                languageLabel == null ? "--" : languageLabel,
                playerCountLabel == null ? "--" : playerCountLabel,
                "Traditional Scrabble",
                null,
                normalizedEndpoint,
                List.of("Player"),
                List.of("Guest"));
    }

    public Flow getFlow() {
        return flow;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getViceTitleText() {
        return viceTitleText;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public String getHintText() {
        return hintText;
    }

    public String getConfirmButtonText() {
        return confirmButtonText;
    }

    public String getGameTimeLabel() {
        return gameTimeLabel;
    }

    public String getStepTimeSecondsLabel() {
        return stepTimeSecondsLabel;
    }

    public String getLanguageLabel() {
        return languageLabel;
    }

    public String getPlayerCountLabel() {
        return playerCountLabel;
    }

    public String getRulesetLabel() {
        return rulesetLabel;
    }

    public String getTargetScoreLabel() {
        return targetScoreLabel;
    }

    public int getActivePlayerCount() {
        if (flow != Flow.HOT_SEAT) {
            return cardTitles.size();
        }

        try {
            int count = Integer.parseInt(playerCountLabel.trim());
            return Math.max(1, Math.min(4, count));
        } catch (NumberFormatException exception) {
            return Math.max(1, Math.min(4, cardTitles.size()));
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public List<String> getCardTitles() {
        return cardTitles;
    }

    public List<String> getDefaultNames() {
        return defaultNames;
    }

    private static boolean hasKnownRoomMetadata(
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel) {
        return isMeaningful(gameTimeLabel) && isMeaningful(languageLabel) && isMeaningful(playerCountLabel);
    }

    private static boolean isMeaningful(String value) {
        return value != null && !value.isBlank() && !"--".equals(value.trim());
    }

    private static String buildSettingsSummary(
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel) {
        return formatLanguageLabel(languageLabel)
                + " | "
                + formatGameTimeLabel(gameTimeLabel)
                + " | "
                + formatPlayerCountLabel(playerCountLabel);
    }

    private static String buildSettingsSummary(
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel,
            String rulesetLabel,
            String targetScoreLabel) {
        String baseSummary =
                formatRulesetLabel(rulesetLabel)
                + " | "
                + formatLanguageLabel(languageLabel)
                + " | "
                + formatGameTimeLabel(gameTimeLabel)
                + " | "
                + formatPlayerCountLabel(playerCountLabel);
        if (isScribbleRuleset(rulesetLabel) && isMeaningful(targetScoreLabel)) {
            return baseSummary + " | Target " + targetScoreLabel.trim();
        }
        return baseSummary;
    }

    private static String formatRulesetLabel(String rulesetLabel) {
        return isMeaningful(rulesetLabel) ? rulesetLabel.trim() : "Traditional Scrabble";
    }

    private static boolean isScribbleRuleset(String rulesetLabel) {
        return isMeaningful(rulesetLabel) && "Scribble".equalsIgnoreCase(rulesetLabel.trim());
    }

    private static String normalizeRulesetLabel(String rulesetLabel) {
        return isMeaningful(rulesetLabel) ? rulesetLabel.trim() : "Traditional Scrabble";
    }

    private static String normalizeTargetScoreLabel(String targetScoreLabel) {
        return targetScoreLabel == null ? "" : targetScoreLabel.trim();
    }

    private static String formatGameTimeLabel(String gameTimeLabel) {
        if (!isMeaningful(gameTimeLabel)) {
            return "--";
        }
        String trimmed = gameTimeLabel.trim();
        return trimmed.matches(".*[A-Za-z].*") ? trimmed : trimmed + "min";
    }

    private static String formatLanguageLabel(String languageLabel) {
        return isMeaningful(languageLabel) ? languageLabel.trim() : "--";
    }

    private static String formatPlayerCountLabel(String playerCountLabel) {
        return isMeaningful(playerCountLabel) ? playerCountLabel.trim() + " players" : "--";
    }

    private static List<String> buildSequentialLabels(String prefix, int count) {
        List<String> labels = new ArrayList<>(count);
        for (int index = 1; index <= count; index++) {
            labels.add(prefix + index);
        }
        return labels;
    }

    private static String normalizeRoomTitle(String roomTitle) {
        if (roomTitle == null || roomTitle.isBlank()) {
            return DEFAULT_ROOM_NAME;
        }
        return roomTitle.trim();
    }
}
