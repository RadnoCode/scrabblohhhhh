package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class WorkbenchButton extends CommonButton {

    public WorkbenchButton(String text) {
        super(text);
        initializeWorkbenchButton();
    }

    private void initializeWorkbenchButton() {
        setTemplateEnabled(false);
        getStyleClass().add("workbench-button");
        setAlignment(Pos.CENTER);
        applyFixedSize(172, 38);
        setPadding(new Insets(0));
    }

    public void setWorkbenchSize(double width, double height) {
        applyFixedSize(width, height);
    }
}
