package com.kotva.presentation.component;

import com.kotva.application.result.PlayerSettlement;
import java.util.List;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class SettlementScoreChartView extends VBox {
    private static final double DEFAULT_CHART_WIDTH = 720;
    private static final double MIN_FILL_RATIO = 0.18;

    private List<PlayerSettlement> rankings;
    private double chartWidth;

    public SettlementScoreChartView() {
        this.rankings = List.of();
        this.chartWidth = DEFAULT_CHART_WIDTH;
        initialize();
    }

    private void initialize() {
        getStyleClass().add("settlement-score-chart");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(14);
    }

    public void setRankings(List<PlayerSettlement> rankings) {
        this.rankings = List.copyOf(Objects.requireNonNull(rankings, "rankings cannot be null."));
        rebuildRows();
    }

    public void setChartWidth(double chartWidth) {
        this.chartWidth = Math.max(320, chartWidth);
        setPrefWidth(this.chartWidth);
        setMinWidth(this.chartWidth);
        setMaxWidth(this.chartWidth);
        rebuildRows();
    }

    private void rebuildRows() {
        getChildren().clear();
        if (rankings.isEmpty()) {
            Label emptyLabel = new Label("No settlement data available.");
            emptyLabel.getStyleClass().add("settlement-empty-label");
            getChildren().add(emptyLabel);
            return;
        }

        int bestScore = rankings.stream()
            .mapToInt(PlayerSettlement::getFinalScore)
            .max()
            .orElse(0);
        double rankWidth = 54;
        double nameWidth = clamp(chartWidth * 0.24, 110, 190);
        double scoreWidth = clamp(chartWidth * 0.14, 74, 102);
        double trackWidth = Math.max(140, chartWidth - rankWidth - nameWidth - scoreWidth - 56);

        for (PlayerSettlement settlement : rankings) {
            boolean winner = settlement.getFinalScore() == bestScore;

            Label rankLabel = new Label("#" + settlement.getRank());
            rankLabel.getStyleClass().add("settlement-rank-badge");
            rankLabel.setPrefWidth(rankWidth);
            rankLabel.setMinWidth(rankWidth);
            rankLabel.setMaxWidth(rankWidth);

            Label nameLabel = new Label(settlement.getPlayerName());
            nameLabel.getStyleClass().add("settlement-player-name");
            nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            nameLabel.setPrefWidth(nameWidth);
            nameLabel.setMinWidth(nameWidth);
            nameLabel.setMaxWidth(nameWidth);

            StackPane track = new StackPane();
            track.getStyleClass().add("settlement-score-track");
            track.setAlignment(Pos.CENTER_LEFT);
            track.setPrefWidth(trackWidth);
            track.setMinWidth(trackWidth);

            Region fill = new Region();
            fill.getStyleClass().add("settlement-score-fill");
            if (winner) {
                fill.getStyleClass().add("settlement-score-fill-winner");
                rankLabel.getStyleClass().add("settlement-rank-badge-winner");
                nameLabel.getStyleClass().add("settlement-player-name-winner");
            }
            double ratio = resolveFillRatio(settlement.getFinalScore(), bestScore);
            fill.prefWidthProperty().bind(Bindings.multiply(track.widthProperty(), ratio));
            fill.maxWidthProperty().bind(Bindings.multiply(track.widthProperty(), ratio));
            StackPane.setAlignment(fill, Pos.CENTER_LEFT);
            track.getChildren().add(fill);
            HBox.setHgrow(track, Priority.ALWAYS);

            Label scoreLabel = new Label(Integer.toString(settlement.getFinalScore()));
            scoreLabel.getStyleClass().add("settlement-score-value");
            if (winner) {
                scoreLabel.getStyleClass().add("settlement-score-value-winner");
            }
            scoreLabel.setPrefWidth(scoreWidth);
            scoreLabel.setMinWidth(scoreWidth);
            scoreLabel.setMaxWidth(scoreWidth);

            HBox row = new HBox(14, rankLabel, nameLabel, track, scoreLabel);
            row.getStyleClass().add("settlement-score-row");
            if (winner) {
                row.getStyleClass().add("settlement-score-row-winner");
            }
            row.setAlignment(Pos.CENTER_LEFT);
            getChildren().add(row);
        }
    }

    private double resolveFillRatio(int score, int bestScore) {
        if (bestScore <= 0) {
            return 0.55;
        }
        return clamp(score / (double) bestScore, MIN_FILL_RATIO, 1.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
