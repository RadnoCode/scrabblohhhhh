package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AiStatusBannerView extends VBox {
    private final Label summaryLabel;
    private final Label detailsLabel;

    public AiStatusBannerView() {
        this.summaryLabel = new Label();
        this.detailsLabel = new Label();
        initialize();
        clear();
    }

    private void initialize() {
        getStyleClass().add("game-ai-status-banner");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(4);
        setPadding(new Insets(10, 14, 10, 14));

        summaryLabel.getStyleClass().add("game-ai-status-summary");
        summaryLabel.setWrapText(true);
        detailsLabel.getStyleClass().add("game-ai-status-details");
        detailsLabel.setWrapText(true);

        getChildren().addAll(summaryLabel, detailsLabel);
    }

    public void showMessage(String summary, String details) {
        summaryLabel.setText(summary);
        detailsLabel.setText(details);
        boolean showDetails = details != null && !details.isBlank();
        detailsLabel.setManaged(showDetails);
        detailsLabel.setVisible(showDetails);
        setManaged(true);
        setVisible(true);
    }

    public void clear() {
        summaryLabel.setText("");
        detailsLabel.setText("");
        detailsLabel.setManaged(false);
        detailsLabel.setVisible(false);
        setManaged(false);
        setVisible(false);
    }
}
