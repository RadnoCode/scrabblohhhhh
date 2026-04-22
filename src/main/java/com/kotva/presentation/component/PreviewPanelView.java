package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.StringJoiner;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class PreviewPanelView extends StackPane {
    private static final String VALID_STYLE = "game-preview-panel-valid";
    private static final String INVALID_STYLE = "game-preview-panel-invalid";
    private static final double PANEL_WIDTH = 296;
    private static final double PANEL_HEIGHT = 178;
    private static final double WORD_VALUE_X = 93;
    private static final double SCORE_VALUE_X = 104;
    private static final double WORD_VALUE_WIDTH = PANEL_WIDTH - WORD_VALUE_X - 8;
    private static final double SCORE_VALUE_WIDTH = PANEL_WIDTH - SCORE_VALUE_X - 8;
    private static final double WORD_VALUE_Y = 59;
    private static final double SCORE_VALUE_Y = 89;
    private static final double HINT_VALUE_Y = 130;
    private static final double HINT_WIDTH = PANEL_WIDTH;

    private final Label wordLabel;
    private final Label scoreLabel;
    private final Label hintLabel;
    private final Label accessibilityLabel;

    public PreviewPanelView() {
        this.wordLabel = new Label();
        this.scoreLabel = new Label();
        this.hintLabel = new Label();
        this.accessibilityLabel = new Label();
        initialize();
    }

    private void initialize() {
        getStyleClass().add("game-preview-panel");
        setPrefSize(PANEL_WIDTH, PANEL_HEIGHT);
        setMinSize(PANEL_WIDTH, PANEL_HEIGHT);
        setMaxSize(PANEL_WIDTH, PANEL_HEIGHT);

        wordLabel.getStyleClass().add("game-preview-word");
        scoreLabel.getStyleClass().add("game-preview-score");
        hintLabel.getStyleClass().add("game-preview-hint");
        accessibilityLabel.getStyleClass().add("game-preview-messages");
        accessibilityLabel.setVisible(false);
        accessibilityLabel.setManaged(false);

        wordLabel.setPrefWidth(WORD_VALUE_WIDTH);
        wordLabel.setMinWidth(WORD_VALUE_WIDTH);
        wordLabel.setMaxWidth(WORD_VALUE_WIDTH);
        wordLabel.relocate(WORD_VALUE_X, WORD_VALUE_Y);

        scoreLabel.setPrefWidth(SCORE_VALUE_WIDTH);
        scoreLabel.setMinWidth(SCORE_VALUE_WIDTH);
        scoreLabel.setMaxWidth(SCORE_VALUE_WIDTH);
        scoreLabel.relocate(SCORE_VALUE_X, SCORE_VALUE_Y);

        hintLabel.setPrefWidth(HINT_WIDTH);
        hintLabel.setMinWidth(HINT_WIDTH);
        hintLabel.setMaxWidth(HINT_WIDTH);
        hintLabel.setWrapText(true);
        hintLabel.relocate(0, HINT_VALUE_Y);

        Pane textLayer = new Pane(wordLabel, scoreLabel, hintLabel, accessibilityLabel);
        textLayer.setPickOnBounds(false);
        textLayer.setPrefSize(PANEL_WIDTH, PANEL_HEIGHT);
        textLayer.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        textLayer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        Rectangle clip = new Rectangle(PANEL_WIDTH, PANEL_HEIGHT);
        textLayer.setClip(clip);
        getChildren().add(textLayer);

        setModel(GameViewModel.PreviewPanelModel.hidden());
    }

    public void setModel(GameViewModel.PreviewPanelModel model) {
        getStyleClass().removeAll(VALID_STYLE, INVALID_STYLE);
        if (model == null || !model.isVisible()) {
            wordLabel.setText("--");
            scoreLabel.setText("--");
            hintLabel.setText("See preview here");
            accessibilityLabel.setText("Preview unavailable.");
            setAccessibleText("Preview unavailable.");
            setOpacity(0.72);
            return;
        }

        setOpacity(1.0);
        if (model.isValid()) {
            getStyleClass().add(VALID_STYLE);
        } else {
            getStyleClass().add(INVALID_STYLE);
        }
        wordLabel.setText(stripValuePrefix(model.getMainWordText()));
        scoreLabel.setText(stripValuePrefix(model.getScoreText()));
        StringJoiner joiner = new StringJoiner("\n");
        model.getMessages().forEach(joiner::add);
        hintLabel.setText(resolveHintText(model));
        String accessibilityText = model.getStatusText() + "\n" + model.getMainWordText() + "\n"
            + model.getScoreText() + "\n" + joiner;
        accessibilityLabel.setText(accessibilityText);
        setAccessibleText(accessibilityText);
    }

    private String resolveHintText(GameViewModel.PreviewPanelModel model) {
        if (model.getMessages() != null && !model.getMessages().isEmpty()) {
            return model.getMessages().get(0);
        }
        if (model.getStatusText() != null && !model.getStatusText().isBlank()) {
            return model.getStatusText().trim();
        }
        return "";
    }

    private String stripValuePrefix(String text) {
        if (text == null || text.isBlank()) {
            return "--";
        }
        int colonIndex = text.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < text.length()) {
            return text.substring(colonIndex + 1).trim();
        }
        return text.trim();
    }
}
