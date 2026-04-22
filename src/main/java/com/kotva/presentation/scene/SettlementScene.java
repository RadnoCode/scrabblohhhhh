package com.kotva.presentation.scene;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.SettlementScoreChartView;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.component.WorkbenchButton;
import com.kotva.presentation.controller.SettlementController;
import com.kotva.presentation.viewmodel.SettlementViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SettlementScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    public SettlementScene(SettlementController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(SettlementController controller) {
        SettlementViewModel viewModel = controller.getViewModel();

        StackPane sceneRoot = new StackPane();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("settlement-root");

        Label reasonCaptionLabel = new Label(viewModel.getReasonCaptionText());
        reasonCaptionLabel.getStyleClass().add("settlement-reason-caption");

        Label reasonLabel = new Label(viewModel.getReasonText());
        reasonLabel.getStyleClass().add("settlement-reason-title");
        reasonLabel.setWrapText(true);

        Label reasonDetailLabel = new Label(viewModel.getReasonDetailText());
        reasonDetailLabel.getStyleClass().add("settlement-reason-detail");
        reasonDetailLabel.setWrapText(true);

        VBox topBox = new VBox(6, reasonCaptionLabel, reasonLabel, reasonDetailLabel);
        topBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(topBox, new Insets(28, 56, 12, 56));
        root.setTop(topBox);

        Label podiumLabel = new Label(viewModel.getPodiumText());
        podiumLabel.getStyleClass().add("settlement-podium-title");

        Region accentLine = new Region();
        accentLine.getStyleClass().add("settlement-podium-line");

        Label summaryLabel = new Label(viewModel.getSummaryText());
        summaryLabel.getStyleClass().add("settlement-summary-label");
        summaryLabel.setWrapText(true);

        SettlementScoreChartView scoreChartView = new SettlementScoreChartView();
        scoreChartView.setRankings(viewModel.getRankings());

        StackPane chartCard = new StackPane(scoreChartView);
        chartCard.getStyleClass().add("settlement-chart-card");

        VBox centerBox = new VBox(14, podiumLabel, accentLine, summaryLabel, chartCard);
        centerBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(centerBox, new Insets(0, 56, 24, 56));
        root.setCenter(centerBox);

        TransientMessageView messageView = new TransientMessageView();
        messageView.getStyleClass().add("settlement-export-message");

        CommonButton exportButton = new WorkbenchButton(viewModel.getExportButtonText());
        exportButton.getStyleClass().add("settlement-action-button");
        CommonButton homeButton = new WorkbenchButton(viewModel.getHomeButtonText());
        homeButton.getStyleClass().add("settlement-action-button");

        HBox actionBar = new HBox(12, exportButton, homeButton);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        BorderPane.setMargin(actionBar, new Insets(0, 32, 28, 32));
        root.setBottom(actionBar);

        controller.bindActions(homeButton, exportButton, sceneRoot, messageView);
        installResponsiveLayout(
            sceneRoot,
            topBox,
            reasonLabel,
            reasonDetailLabel,
            centerBox,
            summaryLabel,
            chartCard,
            scoreChartView,
            actionBar,
            messageView);

        StackPane.setAlignment(messageView, Pos.TOP_CENTER);
        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root, messageView);
        return sceneRoot;
    }

    private static void installResponsiveLayout(
        StackPane sceneRoot,
        VBox topBox,
        Label reasonLabel,
        Label reasonDetailLabel,
        VBox centerBox,
        Label summaryLabel,
        StackPane chartCard,
        SettlementScoreChartView scoreChartView,
        HBox actionBar,
        TransientMessageView messageView) {
        Runnable updater = () -> {
            double width = ResponsiveLayoutUtil.resolvedWidth(sceneRoot, DEFAULT_WIDTH);
            double height = ResponsiveLayoutUtil.resolvedHeight(sceneRoot, DEFAULT_HEIGHT);
            boolean compact = width < 1_040;
            boolean shortHeight = height < 720;

            double horizontalMargin = compact ? 24 : 56;
            double cardWidth = ResponsiveLayoutUtil.clamp(width * (compact ? 0.88 : 0.70), 360, 860);
            double chartWidth = Math.max(280, cardWidth - 56);
            double topWidth = ResponsiveLayoutUtil.clamp(width * 0.72, 320, 780);
            double actionWidth = width - 64;

            BorderPane.setMargin(
                topBox,
                new Insets(shortHeight ? 20 : 28, horizontalMargin, shortHeight ? 8 : 12, horizontalMargin));
            topBox.setSpacing(shortHeight ? 4 : 6);
            reasonLabel.setMaxWidth(topWidth);
            reasonDetailLabel.setMaxWidth(topWidth);

            centerBox.setSpacing(shortHeight ? 10 : 14);
            BorderPane.setMargin(
                centerBox,
                new Insets(0, horizontalMargin, shortHeight ? 16 : 24, horizontalMargin));
            summaryLabel.setMaxWidth(cardWidth);

            chartCard.setPrefWidth(cardWidth);
            chartCard.setMinWidth(cardWidth);
            chartCard.setMaxWidth(cardWidth);
            scoreChartView.setChartWidth(chartWidth);

            actionBar.setPrefWidth(actionWidth);
            BorderPane.setMargin(actionBar, new Insets(0, 32, shortHeight ? 18 : 28, 32));

            double messageWidth = ResponsiveLayoutUtil.clamp(width * 0.42, 260, 460);
            messageView.setPrefWidth(messageWidth);
            messageView.setMaxWidth(messageWidth);
            StackPane.setMargin(messageView, new Insets(shortHeight ? 14 : 20, 0, 0, 0));
        };

        ResponsiveLayoutUtil.install(sceneRoot, updater);
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/settlement.css").toExternalForm());
    }
}
