package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

public class TutorialOverlayView extends StackPane {
    private static final double CARD_WIDTH = PlayerInfoCardView.CARD_WIDTH;
    private static final double CARD_HEIGHT = PlayerInfoCardView.CARD_HEIGHT * 2 + 12;

    private final StackPane blockerPane;
    private final VBox card;
    private final Label progressLabel;
    private final Label titleLabel;
    private final Label bodyLabel;
    private final Label hintLabel;
    private final WorkbenchButton exitButton;
    private final WorkbenchButton returnHomeButton;
    private HBox topActionRow;
    private HBox bottomActionRow;
    private boolean tapToContinue;

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
        this.exitButton = new WorkbenchButton("Exit Tutorial");
        this.returnHomeButton = new WorkbenchButton("Return to Home");
        initialize();
    }

    private void initialize() {
        setPickOnBounds(false);
        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMinSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        blockerPane.getStyleClass().add("tutorial-overlay-blocker");
        blockerPane.setVisible(false);
        blockerPane.setMouseTransparent(true);

        card.getStyleClass().add("tutorial-overlay-card");
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        card.setClip(new Rectangle(CARD_WIDTH, CARD_HEIGHT));
        StackPane.setAlignment(card, Pos.TOP_CENTER);

        progressLabel.getStyleClass().add("tutorial-overlay-progress");
        titleLabel.getStyleClass().add("tutorial-overlay-title");
        bodyLabel.getStyleClass().add("tutorial-overlay-body");
        bodyLabel.setWrapText(true);
        bodyLabel.setMaxWidth(CARD_WIDTH - 28);
        hintLabel.getStyleClass().add("tutorial-overlay-hint");
        hintLabel.setWrapText(true);
        hintLabel.setMaxWidth(CARD_WIDTH - 28);

        exitButton.setWorkbenchSize(124, 32);
        returnHomeButton.setWorkbenchSize(124, 32);
        exitButton.getStyleClass().add("tutorial-overlay-button");
        returnHomeButton.getStyleClass().add("tutorial-overlay-button");

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
        VBox.setVgrow(spacer, Priority.ALWAYS);
        topActionRow = new HBox(8, hintLabel, returnHomeButton);
        topActionRow.setAlignment(Pos.CENTER_LEFT);
        bottomActionRow = new HBox(exitButton);
        bottomActionRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(
            progressLabel,
            titleLabel,
            bodyLabel,
            topActionRow,
            spacer,
            bottomActionRow);
        getChildren().addAll(blockerPane, card);
        card.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleCardClicked);
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
        tapToContinue = safeModel.isTapToContinue();
        if (!safeModel.isVisible()) {
            blockerPane.setVisible(false);
            blockerPane.setMouseTransparent(true);
            return;
        }

        progressLabel.setText(safeModel.getProgressText());
        titleLabel.setText(safeModel.getTitle());
        bodyLabel.setText(safeModel.getBody());
        hintLabel.setText(tapToContinue ? "Click the card to continue" : "");
        hintLabel.setVisible(tapToContinue);
        hintLabel.setManaged(tapToContinue);
        blockerPane.setVisible(false);
        blockerPane.setMouseTransparent(!tapToContinue);
        exitButton.setVisible(safeModel.isShowExitButton());
        exitButton.setManaged(safeModel.isShowExitButton());
        returnHomeButton.setVisible(safeModel.isShowReturnHomeButton());
        returnHomeButton.setManaged(safeModel.isShowReturnHomeButton());
        topActionRow.setVisible(tapToContinue || safeModel.isShowReturnHomeButton());
        topActionRow.setManaged(tapToContinue || safeModel.isShowReturnHomeButton());
        bottomActionRow.setVisible(safeModel.isShowExitButton());
        bottomActionRow.setManaged(safeModel.isShowExitButton());
    }

    private void handleCardClicked(MouseEvent event) {
        if (!tapToContinue || onAdvanceRequested == null) {
            return;
        }
        Object target = event.getTarget();
        if (target instanceof Node node
            && (isDescendant(node, exitButton) || isDescendant(node, returnHomeButton))) {
            return;
        }
        onAdvanceRequested.run();
        event.consume();
    }

    private boolean isDescendant(Node node, Node ancestor) {
        Node current = node;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
