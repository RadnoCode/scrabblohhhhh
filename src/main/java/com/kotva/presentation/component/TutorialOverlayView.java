package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TutorialOverlayView extends StackPane {
    private final StackPane blockerPane;
    private final VBox card;
    private final Label progressLabel;
    private final Label titleLabel;
    private final Label bodyLabel;
    private final Label hintLabel;
    private final WorkbenchButton exitButton;
    private final WorkbenchButton returnHomeButton;

    private Runnable onAdvanceRequested;
    private Runnable onExitRequested;
    private Runnable onReturnHomeRequested;

    public TutorialOverlayView() {
        this.blockerPane = new StackPane();
        this.card = new VBox(10);
        this.progressLabel = new Label();
        this.titleLabel = new Label();
        this.bodyLabel = new Label();
        this.hintLabel = new Label();
        this.exitButton = new WorkbenchButton("退出教程");
        this.returnHomeButton = new WorkbenchButton("返回主页");
        initialize();
    }

    private void initialize() {
        setPickOnBounds(false);
        blockerPane.getStyleClass().add("tutorial-overlay-blocker");
        blockerPane.setVisible(false);
        blockerPane.setOnMouseClicked(event -> {
            if (onAdvanceRequested != null) {
                onAdvanceRequested.run();
            }
            event.consume();
        });

        card.getStyleClass().add("tutorial-overlay-card");
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_LEFT);
        card.setMaxWidth(420);
        StackPane.setAlignment(card, Pos.BOTTOM_LEFT);
        StackPane.setMargin(card, new Insets(0, 0, 44, 44));

        progressLabel.getStyleClass().add("tutorial-overlay-progress");
        titleLabel.getStyleClass().add("tutorial-overlay-title");
        bodyLabel.getStyleClass().add("tutorial-overlay-body");
        bodyLabel.setWrapText(true);
        hintLabel.getStyleClass().add("tutorial-overlay-hint");
        hintLabel.setWrapText(true);

        exitButton.setOnAction(event -> {
            if (onExitRequested != null) {
                onExitRequested.run();
            }
        });
        returnHomeButton.setOnAction(event -> {
            if (onReturnHomeRequested != null) {
                onReturnHomeRequested.run();
            }
        });

        Region spacer = new Region();
        spacer.setMinHeight(4);
        VBox buttonRow = new VBox(8, returnHomeButton, exitButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(progressLabel, titleLabel, bodyLabel, spacer, hintLabel, buttonRow);
        getChildren().addAll(blockerPane, card);
        setModel(GameViewModel.TutorialOverlayModel.hidden());
    }

    public void setOnAdvanceRequested(Runnable onAdvanceRequested) {
        this.onAdvanceRequested = onAdvanceRequested;
    }

    public void setOnExitRequested(Runnable onExitRequested) {
        this.onExitRequested = onExitRequested;
    }

    public void setOnReturnHomeRequested(Runnable onReturnHomeRequested) {
        this.onReturnHomeRequested = onReturnHomeRequested;
    }

    public void setModel(GameViewModel.TutorialOverlayModel model) {
        GameViewModel.TutorialOverlayModel safeModel =
            model == null ? GameViewModel.TutorialOverlayModel.hidden() : model;
        setVisible(safeModel.isVisible());
        setManaged(safeModel.isVisible());
        if (!safeModel.isVisible()) {
            blockerPane.setVisible(false);
            blockerPane.setMouseTransparent(true);
            return;
        }

        progressLabel.setText(safeModel.getProgressText());
        titleLabel.setText(safeModel.getTitle());
        bodyLabel.setText(safeModel.getBody());
        boolean tapToContinue = safeModel.isTapToContinue();
        hintLabel.setText(tapToContinue ? "点击屏幕继续" : "");
        blockerPane.setVisible(tapToContinue);
        blockerPane.setMouseTransparent(!tapToContinue);
        exitButton.setVisible(safeModel.isShowExitButton());
        exitButton.setManaged(safeModel.isShowExitButton());
        returnHomeButton.setVisible(safeModel.isShowReturnHomeButton());
        returnHomeButton.setManaged(safeModel.isShowReturnHomeButton());
    }
}
