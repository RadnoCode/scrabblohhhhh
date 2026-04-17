package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class WorkbenchButton extends CommonButton {

    public WorkbenchButton(String text) {
        super(text);
        initializeWorkbenchButton();
    }

    private void initializeWorkbenchButton() {
        getStyleClass().add("workbench-button");
        setAlignment(Pos.CENTER);
        setPrefSize(180, 44);
        setMinSize(180, 44);
        setMaxSize(180, 44);
        setPadding(new Insets(0));
    }
}
