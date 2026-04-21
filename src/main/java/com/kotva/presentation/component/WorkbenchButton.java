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
        setPrefSize(172, 38);
        setMinSize(172, 38);
        setMaxSize(172, 38);
        setPadding(new Insets(0));
    }
}
