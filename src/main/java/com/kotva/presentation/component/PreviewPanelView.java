package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.StringJoiner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PreviewPanelView extends StackPane {
    private static final String VALID_STYLE = "game-preview-panel-valid";
    private static final String INVALID_STYLE = "game-preview-panel-invalid";

    private final Label titleLabel;
    private final Label statusLabel;
    private final Label wordLabel;
    private final Label scoreLabel;
    private final Label messagesLabel;

    public PreviewPanelView() {
        this.titleLabel = new Label("Preview");
        this.statusLabel = new Label();
        this.wordLabel = new Label();
        this.scoreLabel = new Label();
        this.messagesLabel = new Label();
        initialize();
    }

    private void initialize() {
        getStyleClass().add("game-preview-panel");
        setPrefSize(220, 120);
        setMinSize(220, 120);
        setMaxSize(220, 120);
        setPadding(new Insets(12));

        titleLabel.getStyleClass().add("game-preview-title");
        statusLabel.getStyleClass().add("game-preview-status");
        wordLabel.getStyleClass().add("game-preview-word");
        scoreLabel.getStyleClass().add("game-preview-score");
        messagesLabel.getStyleClass().add("game-preview-messages");
        messagesLabel.setWrapText(true);

        Region spacer = new Region();
        spacer.setMinHeight(2);

        VBox content = new VBox(4, titleLabel, statusLabel, wordLabel, scoreLabel, spacer, messagesLabel);
        content.setAlignment(Pos.TOP_LEFT);
        getChildren().add(content);

        setModel(GameViewModel.PreviewPanelModel.hidden());
    }

    public void setModel(GameViewModel.PreviewPanelModel model) {
        getStyleClass().removeAll(VALID_STYLE, INVALID_STYLE);
        if (model == null || !model.isVisible()) {
            statusLabel.setText("拖动棋子后在这里查看合法性。");
            wordLabel.setText("");
            scoreLabel.setText("");
            messagesLabel.setText("");
            setOpacity(0.72);
            return;
        }

        setOpacity(1.0);
        if (model.isValid()) {
            getStyleClass().add(VALID_STYLE);
        } else {
            getStyleClass().add(INVALID_STYLE);
        }
        statusLabel.setText(model.getStatusText());
        wordLabel.setText(model.getMainWordText());
        scoreLabel.setText(model.getScoreText());
        StringJoiner joiner = new StringJoiner("\n");
        model.getMessages().forEach(joiner::add);
        messagesLabel.setText(joiner.toString());
    }
}
