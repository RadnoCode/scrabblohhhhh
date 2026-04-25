package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Shows one Scrabble tile.
 */
public class TileView extends StackPane {
    private final double tileSize;
    private final Label letterLabel;
    private final Label scoreLabel;

    public TileView() {
        this(56);
    }

    public TileView(double tileSize) {
        this.tileSize = tileSize;
        this.letterLabel = new Label();
        this.scoreLabel = new Label();
        initializeTile();
    }

    private void initializeTile() {
        getStyleClass().add("game-tile");
        if (tileSize <= 36) {
            getStyleClass().add("game-tile-small");
        }

        setPrefSize(tileSize, tileSize);
        setMinSize(tileSize, tileSize);
        setMaxSize(tileSize, tileSize);

        letterLabel.getStyleClass().add("game-tile-letter");
        scoreLabel.getStyleClass().add("game-tile-score");
        if (tileSize <= 36) {
            letterLabel.getStyleClass().add("game-tile-letter-small");
            scoreLabel.getStyleClass().add("game-tile-score-small");
        }

        letterLabel.setManaged(false);
        scoreLabel.setManaged(false);
        letterLabel.setMouseTransparent(true);
        scoreLabel.setMouseTransparent(true);

        getChildren().addAll(letterLabel, scoreLabel);
        clearTile();
    }

    public void setTile(GameViewModel.TileModel tileModel) {
        if (tileModel == null || tileModel.isEmpty()) {
            clearTile();
            return;
        }
        setTile(tileModel.getLetter(), tileModel.getScore());
    }

    public void setTile(String letter, int score) {
        letterLabel.setText(letter);
        scoreLabel.setText(Integer.toString(score));
        getStyleClass().remove("game-tile-empty");
        if (!getStyleClass().contains("game-tile-filled")) {
            getStyleClass().add("game-tile-filled");
        }
        requestLayout();
    }

    public void clearTile() {
        letterLabel.setText("");
        scoreLabel.setText("");
        getStyleClass().remove("game-tile-filled");
        if (!getStyleClass().contains("game-tile-empty")) {
            getStyleClass().add("game-tile-empty");
        }
        requestLayout();
    }

    public double getTileSize() {
        return tileSize;
    }

        @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();

        double letterWidth = snapSizeX(letterLabel.prefWidth(-1));
        double letterHeight = snapSizeY(letterLabel.prefHeight(-1));
        double scoreWidth = snapSizeX(scoreLabel.prefWidth(-1));
        double scoreHeight = snapSizeY(scoreLabel.prefHeight(-1));

        double letterX = snapPositionX((width - letterWidth) / 2.0);
        double letterY;
        double scoreX;
        double scoreY;

        if (tileSize <= 36) {
            letterY = snapPositionY((height - letterHeight) / 2.0);
            scoreX = snapPositionX(width - scoreWidth - 4.0);
            double letterBottom = letterY + letterHeight;
            scoreY = snapPositionY(letterBottom - scoreHeight - 1.0);
        } else {
            double scoreRightInset = 7.0;
            double scoreBottomInset = 5.0;
            letterY = snapPositionY((height - letterHeight) / 2.0);
            scoreX = snapPositionX(width - scoreWidth - scoreRightInset);
            scoreY = snapPositionY(height - scoreHeight - scoreBottomInset);
        }

        letterLabel.resizeRelocate(letterX, letterY, letterWidth, letterHeight);
        scoreLabel.resizeRelocate(scoreX, scoreY, scoreWidth, scoreHeight);
    }
}
