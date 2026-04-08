package com.kotva.presentation.interaction;

import com.kotva.domain.model.TilePlacement;
import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * GameDraftState 保存“真的草稿状态”。
 * 它不处理拖拽动画，只负责合并后端快照和本地草稿，产出可渲染的数据。
 */
public class GameDraftState {
    private static final int RACK_SLOT_COUNT = 7;

    private final List<GameViewModel.TileModel> baseRackTiles;
    private final Map<BoardCoordinate, GameViewModel.BoardTileModel> baseBoardTiles;
    private final Map<String, DraftPlacementModel> draftPlacementsByTileId;

    public GameDraftState() {
        this.baseRackTiles = new ArrayList<>();
        this.baseBoardTiles = new LinkedHashMap<>();
        this.draftPlacementsByTileId = new LinkedHashMap<>();
        resetBaseRackTiles();
    }

    public void syncSnapshot(
            List<GameViewModel.TileModel> rackTiles, List<GameViewModel.BoardTileModel> boardTiles) {
        // 每次收到后端快照后，先用快照重置基础 rack 状态。
        baseRackTiles.clear();
        baseRackTiles.addAll(Objects.requireNonNull(rackTiles, "rackTiles cannot be null."));
        while (baseRackTiles.size() < RACK_SLOT_COUNT) {
            baseRackTiles.add(GameViewModel.TileModel.empty());
        }

        // 棋盘基础状态也完全以最新快照为准。
        baseBoardTiles.clear();
        for (GameViewModel.BoardTileModel boardTile : Objects.requireNonNull(boardTiles, "boardTiles cannot be null.")) {
            baseBoardTiles.put(boardTile.getCoordinate(), boardTile);
        }
    }

    public List<GameViewModel.TileModel> getRenderedRackTiles(Integer suppressedRackIndex) {
        // 先从基础 rack 拷一份可渲染数据。
        List<GameViewModel.TileModel> renderedTiles = new ArrayList<>(baseRackTiles);
        while (renderedTiles.size() < RACK_SLOT_COUNT) {
            renderedTiles.add(GameViewModel.TileModel.empty());
        }

        // 已经落到棋盘草稿里的 Tile，在牌架里都应该显示为空槽。
        for (DraftPlacementModel placement : draftPlacementsByTileId.values()) {
            renderedTiles.set(placement.getOriginalRackIndex(), GameViewModel.TileModel.empty());
        }

        // 如果当前正在从 rack 拖拽，也要临时把源位置隐藏。
        if (suppressedRackIndex != null && suppressedRackIndex >= 0 && suppressedRackIndex < renderedTiles.size()) {
            renderedTiles.set(suppressedRackIndex, GameViewModel.TileModel.empty());
        }

        return renderedTiles;
    }

    public List<GameViewModel.BoardTileModel> getRenderedBoardTiles(BoardCoordinate suppressedBoardCoordinate) {
        // 先放入已经真正存在于棋盘上的旧 Tile。
        Map<BoardCoordinate, GameViewModel.BoardTileModel> renderedTiles = new LinkedHashMap<>(baseBoardTiles);
        for (DraftPlacementModel placement : draftPlacementsByTileId.values()) {
            // 如果某块草稿 Tile 正在从棋盘被拖起，就先别画回原格。
            if (!placement.getCoordinate().equals(suppressedBoardCoordinate)) {
                renderedTiles.put(
                        placement.getCoordinate(),
                        new GameViewModel.BoardTileModel(placement.getCoordinate(), placement.getTile(), true));
            }
        }
        return new ArrayList<>(renderedTiles.values());
    }

    public GameViewModel.TileModel getRackTileAt(int rackIndex) {
        List<GameViewModel.TileModel> renderedTiles = getRenderedRackTiles(null);
        if (rackIndex < 0 || rackIndex >= renderedTiles.size()) {
            return null;
        }

        GameViewModel.TileModel tileModel = renderedTiles.get(rackIndex);
        return tileModel.isEmpty() ? null : tileModel;
    }

    public DraftPlacementModel getDraftTileAt(BoardCoordinate coordinate) {
        Objects.requireNonNull(coordinate, "coordinate cannot be null.");
        for (DraftPlacementModel placement : draftPlacementsByTileId.values()) {
            if (placement.getCoordinate().equals(coordinate)) {
                return placement;
            }
        }
        return null;
    }

    public boolean isCellOccupied(BoardCoordinate coordinate, String ignoredTileId) {
        Objects.requireNonNull(coordinate, "coordinate cannot be null.");
        if (baseBoardTiles.containsKey(coordinate)) {
            return true;
        }

        for (DraftPlacementModel placement : draftPlacementsByTileId.values()) {
            if (!placement.getTile().getTileId().equals(ignoredTileId) && placement.getCoordinate().equals(coordinate)) {
                return true;
            }
        }
        return false;
    }

    public void placeRackTile(int rackIndex, BoardCoordinate coordinate) {
        // 从指定 rack 位置取出 Tile，并在草稿层登记它的新坐标。
        GameViewModel.TileModel tileModel = Objects.requireNonNull(
                getRackTileAt(rackIndex), "rack slot does not contain a draggable tile.");
        draftPlacementsByTileId.put(
                tileModel.getTileId(), new DraftPlacementModel(tileModel, rackIndex, coordinate));
    }

    public void moveDraftTile(String tileId, BoardCoordinate coordinate) {
        // 已经在棋盘草稿里的 Tile 只更新位置，不改原始 rack 来源。
        DraftPlacementModel placement = Objects.requireNonNull(
                draftPlacementsByTileId.get(tileId), "tileId is not present in draft placements.");
        placement.setCoordinate(Objects.requireNonNull(coordinate, "coordinate cannot be null."));
    }

    public void removeDraftTile(String tileId) {
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        draftPlacementsByTileId.remove(tileId);
    }

    public void recallAllDraftTiles() {
        draftPlacementsByTileId.clear();
    }

    public List<TilePlacement> toTilePlacements() {
        // 这里把前端草稿转换成后端更容易消费的 TilePlacement 列表。
        List<TilePlacement> placements = new ArrayList<>();
        for (DraftPlacementModel placement : draftPlacementsByTileId.values()) {
            placements.add(new TilePlacement(
                    placement.getTile().getTileId(),
                    placement.getCoordinate().toPosition(),
                    null));
        }
        return placements;
    }

    private void resetBaseRackTiles() {
        baseRackTiles.clear();
        for (int index = 0; index < RACK_SLOT_COUNT; index++) {
            baseRackTiles.add(GameViewModel.TileModel.empty());
        }
    }

    public static final class DraftPlacementModel {
        private final GameViewModel.TileModel tile;
        private final int originalRackIndex;
        private BoardCoordinate coordinate;

        public DraftPlacementModel(GameViewModel.TileModel tile, int originalRackIndex, BoardCoordinate coordinate) {
            this.tile = Objects.requireNonNull(tile, "tile cannot be null.");
            this.originalRackIndex = originalRackIndex;
            this.coordinate = Objects.requireNonNull(coordinate, "coordinate cannot be null.");
        }

        public GameViewModel.TileModel getTile() {
            return tile;
        }

        public int getOriginalRackIndex() {
            return originalRackIndex;
        }

        public BoardCoordinate getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(BoardCoordinate coordinate) {
            this.coordinate = Objects.requireNonNull(coordinate, "coordinate cannot be null.");
        }
    }
}
