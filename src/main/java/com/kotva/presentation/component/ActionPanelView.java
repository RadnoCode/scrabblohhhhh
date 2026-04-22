package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class ActionPanelView extends StackPane {
    private static final String HIGHLIGHT_STYLE = "action-panel-button-highlight";
    private static final double PANEL_WIDTH = 188;
    private static final double PANEL_HEIGHT = 276;
    private static final double BUTTON_WIDTH = 160;
    private static final double BUTTON_HEIGHT = 40;

    private static final double SKIP_CENTER_Y = 43.8;
    private static final double REARRANGE_CENTER_Y = 92.9;
    private static final double RECALL_CENTER_Y = 137.9;
    private static final double RESIGN_CENTER_Y = 190.0;
    private static final double SUBMIT_CENTER_Y = 237.8;

    private final WorkbenchButton skipTurnButton;
    private final WorkbenchButton rearrangeButton;
    private final WorkbenchButton recallButton;
    private final WorkbenchButton resignButton;
    private final WorkbenchButton submitButton;

    public ActionPanelView() {
        this.skipTurnButton = createButton("Skip Turn");
        this.rearrangeButton = createButton("Rearrange");
        this.recallButton = createButton("Recall");
        this.resignButton = createButton("Resign");
        this.submitButton = createButton("Submit");
        initializePanel();
    }

    private void initializePanel() {
        getStyleClass().add("game-workbench-panel");
        setPrefSize(PANEL_WIDTH, PANEL_HEIGHT);
        setMinSize(PANEL_WIDTH, PANEL_HEIGHT);
        setMaxSize(PANEL_WIDTH, PANEL_HEIGHT);

        Pane buttonLayer = new Pane(
            skipTurnButton,
            rearrangeButton,
            recallButton,
            resignButton,
            submitButton);
        buttonLayer.setPrefSize(PANEL_WIDTH, PANEL_HEIGHT);
        buttonLayer.setMinSize(PANEL_WIDTH, PANEL_HEIGHT);
        buttonLayer.setMaxSize(PANEL_WIDTH, PANEL_HEIGHT);

        positionButton(skipTurnButton, SKIP_CENTER_Y);
        positionButton(rearrangeButton, REARRANGE_CENTER_Y);
        positionButton(recallButton, RECALL_CENTER_Y);
        positionButton(resignButton, RESIGN_CENTER_Y);
        positionButton(submitButton, SUBMIT_CENTER_Y);

        getChildren().add(buttonLayer);
    }

    private WorkbenchButton createButton(String text) {
        WorkbenchButton button = new WorkbenchButton(text);
        button.getStyleClass().add("action-panel-button");
        button.setWorkbenchSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        return button;
    }

    private void positionButton(WorkbenchButton button, double centerY) {
        button.relocate(
            (PANEL_WIDTH - BUTTON_WIDTH) / 2.0,
            centerY - (BUTTON_HEIGHT / 2.0));
    }

    public WorkbenchButton getSkipTurnButton() {
        return skipTurnButton;
    }

    public WorkbenchButton getRearrangeButton() {
        return rearrangeButton;
    }

    public WorkbenchButton getRecallButton() {
        return recallButton;
    }

    public WorkbenchButton getResignButton() {
        return resignButton;
    }

    public WorkbenchButton getSubmitButton() {
        return submitButton;
    }

    public void setInteractionLocked(boolean interactionLocked) {
        applyButtonState(skipTurnButton, GameViewModel.ActionButtonModel.enabled(), interactionLocked);
        applyButtonState(rearrangeButton, GameViewModel.ActionButtonModel.enabled(), interactionLocked);
        applyButtonState(recallButton, GameViewModel.ActionButtonModel.enabled(), interactionLocked);
        applyButtonState(resignButton, GameViewModel.ActionButtonModel.enabled(), interactionLocked);
        applyButtonState(submitButton, GameViewModel.ActionButtonModel.enabled(), interactionLocked);
    }

    public void applyModel(GameViewModel.ActionPanelModel model, boolean interactionLocked) {
        GameViewModel.ActionPanelModel safeModel =
            model == null ? GameViewModel.ActionPanelModel.defaultState() : model;
        applyButtonState(skipTurnButton, safeModel.getSkipButton(), interactionLocked);
        applyButtonState(rearrangeButton, safeModel.getRearrangeButton(), interactionLocked);
        applyButtonState(recallButton, safeModel.getRecallButton(), interactionLocked);
        applyButtonState(resignButton, safeModel.getResignButton(), interactionLocked);
        applyButtonState(submitButton, safeModel.getSubmitButton(), interactionLocked);
    }

    private void applyButtonState(
        WorkbenchButton button,
        GameViewModel.ActionButtonModel model,
        boolean interactionLocked) {
        button.getStyleClass().remove(HIGHLIGHT_STYLE);
        if (model != null && model.isHighlighted() && !button.getStyleClass().contains(HIGHLIGHT_STYLE)) {
            button.getStyleClass().add(HIGHLIGHT_STYLE);
        }
        boolean enabled = model == null || model.isEnabled();
        button.setDisable(interactionLocked || !enabled);
    }
}
