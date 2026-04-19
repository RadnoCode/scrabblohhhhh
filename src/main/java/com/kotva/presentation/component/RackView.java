package com.kotva.presentation.component;

import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * RackView 负责底部牌架的 JavaFX 布局与刷新。
 */
public class RackView extends StackPane {
    private static final int RACK_SIZE = 7;

    private final List<TileView> tileSlots;
    private final List<GameViewModel.TileModel> currentTiles;

    public RackView() {
        this.tileSlots = new ArrayList<>();
        this.currentTiles = new ArrayList<>();
        initializeRack();
    }

    private void initializeRack() {
        // 设置牌架自身的背景和内边距。
        getStyleClass().add("game-rack");
        setPadding(new Insets(14, 16, 14, 16));

        // 用一行 HBox 摆放 7 个 Tile 槽位。
        HBox slotRow = new HBox(16);
        slotRow.setAlignment(Pos.CENTER);

        for (int index = 0; index < RACK_SIZE; index++) {
            // 每个槽位先放一个空 TileView，后面渲染时再填内容。
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

        // 逐格刷新牌架内容，同时把当前渲染结果缓存下来供交互层读取。
        for (int index = 0; index < tileSlots.size(); index++) {
            TileView tileView = tileSlots.get(index);
            if (index < tileModels.size() && !tileModels.get(index).isEmpty()) {
                GameViewModel.TileModel tileModel = tileModels.get(index);
                // 有字母时显示真实 Tile。
                currentTiles.set(index, tileModel);
                tileView.setTile(tileModel);
            } else {
                // 没有字母时恢复为空槽状态。
                currentTiles.set(index, GameViewModel.TileModel.empty());
                tileView.clearTile();
            }
        }
    }

    public TileView getTileView(int index) {
        return tileSlots.get(index);
    }

    public GameViewModel.TileModel getTileModel(int index) {
        return currentTiles.get(index);
    }

    public int getSlotCount() {
        return tileSlots.size();
    }
}
