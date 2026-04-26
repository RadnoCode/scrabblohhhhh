package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Shows the player's tile rack.
 */
public class RackView extends StackPane {
    private static final int RACK_SIZE = 7;
    private static final String RACK_HOVER_STYLE = "game-rack-hover";
    private static final String SLOT_HOVER_STYLE = "game-rack-slot-hover";
    private static final String SLOT_TUTORIAL_HIGHLIGHT_STYLE = "game-rack-slot-tutorial-highlight";
    private static final String SLOT_TUTORIAL_DIM_STYLE = "game-rack-slot-tutorial-dim";

    private final List<TileView> tileSlots;
    private final List<GameViewModel.TileModel> currentTiles;
    private Integer hoveredRackIndex;

    public RackView() {
        this.tileSlots = new ArrayList<>();
        this.currentTiles = new ArrayList<>();
        this.hoveredRackIndex = null;
        initializeRack();
    }

    private void initializeRack() {
        getStyleClass().add("game-rack");
        setPadding(new Insets(14, 16, 14, 16));

        HBox slotRow = new HBox(16);
        slotRow.setAlignment(Pos.CENTER);

        for (int index = 0; index < RACK_SIZE; index++) {
            TileView tileView = new TileView();
            tileView.clearTile();
            tileSlots.add(tileView);
            currentTiles.add(GameViewModel.TileModel.empty());
            slotRow.getChildren().add(tileView);
        }

        getChildren().add(slotRow);
    }

    public void setTiles(List<GameViewModel.TileModel> tileModels) {
        Objects.requireNonNull(tileModels, "tileModels cannot be null.");

        for (int index = 0; index < tileSlots.size(); index++) {
            TileView tileView = tileSlots.get(index);
            tileView.getStyleClass().removeAll(
                SLOT_TUTORIAL_HIGHLIGHT_STYLE,
                SLOT_TUTORIAL_DIM_STYLE);
            if (index < tileModels.size() && !tileModels.get(index).isEmpty()) {
                GameViewModel.TileModel tileModel = tileModels.get(index);
                currentTiles.set(index, tileModel);
                tileView.setTile(tileModel);
                applyTutorialState(tileView, tileModel);
            } else {
                GameViewModel.TileModel emptyTile =
                    index < tileModels.size() ? tileModels.get(index) : GameViewModel.TileModel.empty();
                currentTiles.set(index, emptyTile);
                tileView.clearTile();
                applyTutorialState(tileView, emptyTile);
            }
        }
    }

    public TileView getTileView(int index) {
        return tileSlots.get(index);
    }

    public Bounds getTileBoundsInScene(int index) {
        TileView tileView = tileSlots.get(index);
        return tileView.localToScene(tileView.getBoundsInLocal());
    }

    public Bounds getRackBoundsInScene() {
        return localToScene(getBoundsInLocal());
    }

    public GameViewModel.TileModel getTileModel(int index) {
        return currentTiles.get(index);
    }

    public Integer resolveRackIndex(double sceneX, double sceneY) {
        for (int index = 0; index < tileSlots.size(); index++) {
            Bounds sceneBounds = tileSlots.get(index).localToScene(tileSlots.get(index).getBoundsInLocal());
            if (sceneBounds != null && sceneBounds.contains(sceneX, sceneY)) {
                return index;
            }
        }
        return null;
    }

    public void setHoveredSlot(Integer hoveredRackIndex) {
        this.hoveredRackIndex = hoveredRackIndex;
        updateHoverStyles();
    }

    public int getSlotCount() {
        return tileSlots.size();
    }

    public void setOnSlotEntered(BiConsumer<Integer, MouseEvent> handler) {
        Objects.requireNonNull(handler, "handler cannot be null.");
        for (int index = 0; index < tileSlots.size(); index++) {
            final int slotIndex = index;
            tileSlots.get(index).setOnMouseEntered(event -> handler.accept(slotIndex, event));
        }
    }

    public void setOnSlotExited(BiConsumer<Integer, MouseEvent> handler) {
        Objects.requireNonNull(handler, "handler cannot be null.");
        for (int index = 0; index < tileSlots.size(); index++) {
            final int slotIndex = index;
            tileSlots.get(index).setOnMouseExited(event -> handler.accept(slotIndex, event));
        }
    }

    private void updateHoverStyles() {
        getStyleClass().remove(RACK_HOVER_STYLE);
        if (hoveredRackIndex != null && !getStyleClass().contains(RACK_HOVER_STYLE)) {
            getStyleClass().add(RACK_HOVER_STYLE);
        }

        for (int index = 0; index < tileSlots.size(); index++) {
            TileView tileView = tileSlots.get(index);
            tileView.getStyleClass().remove(SLOT_HOVER_STYLE);
            if (hoveredRackIndex != null
                && hoveredRackIndex == index
                && !tileView.getStyleClass().contains(SLOT_HOVER_STYLE)) {
                tileView.getStyleClass().add(SLOT_HOVER_STYLE);
            }
        }
    }

    private void applyTutorialState(TileView tileView, GameViewModel.TileModel tileModel) {
        if (tileModel.isTutorialHighlighted()
            && !tileView.getStyleClass().contains(SLOT_TUTORIAL_HIGHLIGHT_STYLE)) {
            tileView.getStyleClass().add(SLOT_TUTORIAL_HIGHLIGHT_STYLE);
        }
        if (tileModel.isTutorialDimmed()
            && !tileView.getStyleClass().contains(SLOT_TUTORIAL_DIM_STYLE)) {
            tileView.getStyleClass().add(SLOT_TUTORIAL_DIM_STYLE);
        }
    }
}
