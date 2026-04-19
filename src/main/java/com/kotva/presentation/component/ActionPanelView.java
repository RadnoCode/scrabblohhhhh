package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ActionPanelView extends StackPane {
    private static final String HIGHLIGHT_STYLE = "action-panel-button-highlight";

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
        this.submitButton = createButton("Submmit");
        initializePanel();
    }

    private void initializePanel() {
        getStyleClass().add("game-placeholder-panel");
        setPrefSize(236, 320);
        setMinSize(236, 320);
        setMaxSize(236, 320);
        setPadding(new Insets(20, 18, 20, 18));

        VBox buttonColumn = new VBox(12,
            skipTurnButton,
            rearrangeButton,
            recallButton,
            resignButton,
            submitButton);
        buttonColumn.setAlignment(Pos.CENTER);
        getChildren().add(buttonColumn);
    }

    private WorkbenchButton createButton(String text) {
        WorkbenchButton button = new WorkbenchButton(text);
        button.getStyleClass().add("action-panel-button");
        return button;
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
