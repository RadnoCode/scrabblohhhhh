package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ActionPanelView extends StackPane {
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
        skipTurnButton.setDisable(interactionLocked);
        rearrangeButton.setDisable(interactionLocked);
        recallButton.setDisable(interactionLocked);
        resignButton.setDisable(interactionLocked);
        submitButton.setDisable(interactionLocked);
    }
}