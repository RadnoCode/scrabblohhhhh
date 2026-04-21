package com.kotva.presentation.component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class BlankTilePickerView extends StackPane {
    private static final int COLUMN_COUNT = 7;
    private static final double TILE_SIZE = BoardView.CELL_SIZE;
    private static final double TILE_GAP = 4;
    private static final String OPTION_SELECTED_STYLE = "blank-tile-picker-option-selected";

    private final Label titleLabel;
    private final Map<Character, StackPane> optionViews;
    private Consumer<Character> onLetterSelected;
    private String currentAssignedLetter;

    public BlankTilePickerView() {
        this.titleLabel = new Label("Choose a letter");
        this.optionViews = new LinkedHashMap<>();
        this.currentAssignedLetter = "";
        initialize();
    }

    private void initialize() {
        getStyleClass().add("blank-tile-picker");
        setManaged(false);
        setVisible(false);

        titleLabel.getStyleClass().add("blank-tile-picker-title");

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(TILE_GAP);
        gridPane.setVgap(TILE_GAP);

        for (int index = 0; index < 26; index++) {
            char letter = (char) ('A' + index);
            StackPane optionView = createOptionView(letter);
            optionViews.put(letter, optionView);

            int row = index / COLUMN_COUNT;
            int col = index % COLUMN_COUNT;
            if (row == 3) {
                col += 1;
            }
            gridPane.add(optionView, col, row);
        }

        VBox content = new VBox(8, titleLabel, gridPane);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));
        getChildren().add(content);

        double width = COLUMN_COUNT * TILE_SIZE + (COLUMN_COUNT - 1) * TILE_GAP + 20;
        double height = 4 * TILE_SIZE + 3 * TILE_GAP + 54;
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
    }

    private StackPane createOptionView(char letter) {
        Label label = new Label(String.valueOf(letter));
        label.getStyleClass().add("blank-tile-picker-option-label");
        label.setMouseTransparent(true);

        StackPane optionView = new StackPane(label);
        optionView.getStyleClass().add("blank-tile-picker-option");
        optionView.setAlignment(Pos.CENTER);
        optionView.setPrefSize(TILE_SIZE, TILE_SIZE);
        optionView.setMinSize(TILE_SIZE, TILE_SIZE);
        optionView.setMaxSize(TILE_SIZE, TILE_SIZE);
        optionView.setOnMouseClicked(event -> {
            if (onLetterSelected != null) {
                onLetterSelected.accept(letter);
            }
            event.consume();
        });
        return optionView;
    }

    public void showPicker(String assignedLetter) {
        currentAssignedLetter = normalizeAssignedLetter(assignedLetter);
        updateSelectedState();
        setVisible(true);
        setManaged(true);
    }

    public void hidePicker() {
        setVisible(false);
        setManaged(false);
        currentAssignedLetter = "";
        updateSelectedState();
    }

    public boolean isPickerVisible() {
        return isVisible();
    }

    public void setOnLetterSelected(Consumer<Character> onLetterSelected) {
        this.onLetterSelected = onLetterSelected;
    }

    public double getPickerWidth() {
        return getPrefWidth();
    }

    public double getPickerHeight() {
        return getPrefHeight();
    }

    private void updateSelectedState() {
        for (Map.Entry<Character, StackPane> entry : optionViews.entrySet()) {
            StackPane optionView = entry.getValue();
            optionView.getStyleClass().remove(OPTION_SELECTED_STYLE);
            if (!currentAssignedLetter.isBlank()
                && entry.getKey() == currentAssignedLetter.charAt(0)
                && !optionView.getStyleClass().contains(OPTION_SELECTED_STYLE)) {
                optionView.getStyleClass().add(OPTION_SELECTED_STYLE);
            }
        }
    }

    private String normalizeAssignedLetter(String assignedLetter) {
        Objects.requireNonNullElse(assignedLetter, "");
        return assignedLetter == null ? "" : assignedLetter.trim().toUpperCase();
    }
}
