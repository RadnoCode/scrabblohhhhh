package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * ActionPanelView 是右下角工作台的 JavaFX 组件。
 * 它负责摆放按钮，并把按钮对象暴露给控制器绑定行为。
 */
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
        // 整个工作台沿用占位面板的圆角背景。
        getStyleClass().add("game-placeholder-panel");
        setPrefSize(236, 320);
        setMinSize(236, 320);
        setMaxSize(236, 320);
        setPadding(new Insets(20, 18, 20, 18));

        // 把所有操作按钮竖向排布在中间。
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
        // 所有按钮统一走小号 CommonButton 样式，便于后续整体换皮。
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
