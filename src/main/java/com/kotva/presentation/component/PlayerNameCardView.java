package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PlayerNameCardView extends StackPane {
    public static final int MAX_NAME_CODE_POINTS = 8;
    private static final double CARD_WIDTH = 420;
    private static final double CARD_HEIGHT = 104;

    private final Label titleLabel;
    private final TextField nameField;

    public PlayerNameCardView(String titleText, String initialName) {
        this.titleLabel = new Label(titleText == null ? "" : titleText.trim());
        this.nameField = new TextField(initialName == null ? "" : initialName.trim());
        initializeCard();
    }

    private void initializeCard() {
        getStyleClass().add("player-name-card");
        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMinSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxWidth(CARD_WIDTH);
        setPadding(new Insets(18, 22, 18, 22));

        titleLabel.getStyleClass().add("player-name-card-title");

        nameField.getStyleClass().add("player-name-card-field");
        nameField.setPromptText("UTF-8 nickname");
        TextInputLimiter.limitCodePoints(nameField, MAX_NAME_CODE_POINTS);

        VBox content = new VBox(10, titleLabel, nameField);
        content.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(content);
    }

    public String getPlayerName() {
        return nameField.getText();
    }

    public TextField getNameField() {
        return nameField;
    }
}
