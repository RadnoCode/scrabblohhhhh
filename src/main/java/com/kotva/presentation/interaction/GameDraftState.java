package com.kotva.presentation.interaction;

import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * GameDraftState 保存最近一次 snapshot 的 UI 投影。
 * 它不再维护真实 draft，只给渲染和拖拽命中测试提供只读数据。
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
        baseRackTiles.clear();
        baseRackTiles.addAll(Objects.requireNonNull(rackTiles, "rackTiles cannot be null."));
        while (baseRackTiles.size() < RACK_SLOT_COUNT) {
            baseRackTiles.add(GameViewModel.TileModel.empty());
        }

        baseBoardTiles.clear();
        draftPlacementsByTileId.clear();
        for (GameViewModel.BoardTileModel boardTile :
                Objects.requireNonNull(boardTiles, "boardTiles cannot be null.")) {
            if (boardTile.isDraft()) {
                draftPlacementsByTileId.put(
                        boardTile.getTile().getTileId(),
                        new DraftPlacementModel(boardTile, resolveRackIndex(boardTile.getTile().getTileId())));
            } else {
                baseBoardTiles.put(boardTile.getCoordinate(), boardTile);
            }
        }
    }

    public List<GameViewModel.TileModel> getRenderedRackTiles(Integer suppressedRackIndex) {
        List<GameViewModel.TileModel> renderedTiles = new ArrayList<>(baseRackTiles);
        while (renderedTiles.size() < RACK_SLOT_COUNT) {
            renderedTiles.add(GameViewModel.TileModel.empty());
        }

        for (DraftPlacementModel placement : draftPlacementsByTileId.values()) {
            int rackIndex = placement.getOriginalRackIndex();
            if (rackIndex >= 0 && rackIndex < renderedTiles.size()) {
                renderedTiles.set(rackIndex, GameViewModel.TileModel.empty());
            }
        }

        if (suppressedRackIndex != null && suppressedRackIndex >= 0 && suppressedRackIndex < renderedTiles.size()) {
            renderedTiles.set(suppressedRackIndex, GameViewModel.TileModel.empty());
        }

        return renderedTiles;
    }

    public List<GameViewModel.BoardTileModel> getRenderedBoardTiles(BoardCoordinate suppressedBoardCoordinate) {
        Map<BoardCoordinate, GameViewModel.BoardTileModel> renderedTiles = new LinkedHashMap<>(baseBoardTiles);
        for (DraftPlacementModel placement : draftPlacementsByTileId.values()) {
            if (!placement.getCoordinate().equals(suppressedBoardCoordinate)) {
                renderedTiles.put(placement.getCoordinate(), placement.getBoardTile());
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

    public boolean hasDraftPlacements() {
        return !draftPlacementsByTileId.isEmpty();
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

    private void resetBaseRackTiles() {
        baseRackTiles.clear();
        for (int index = 0; index < RACK_SLOT_COUNT; index++) {
            baseRackTiles.add(GameViewModel.TileModel.empty());
        }
    }

    private int resolveRackIndex(String tileId) {
        for (int index = 0; index < baseRackTiles.size(); index++) {
            GameViewModel.TileModel tileModel = baseRackTiles.get(index);
            if (!tileModel.isEmpty() && tileModel.getTileId().equals(tileId)) {
                return index;
            }
        }
        return -1;
    }

    public static final class DraftPlacementModel {
        private final GameViewModel.BoardTileModel boardTile;
        private final int originalRackIndex;

        public DraftPlacementModel(GameViewModel.BoardTileModel boardTile, int originalRackIndex) {
            this.boardTile = Objects.requireNonNull(boardTile, "boardTile cannot be null.");
            this.originalRackIndex = originalRackIndex;
        }

        public GameViewModel.TileModel getTile() {
            return boardTile.getTile();
        }

        public int getOriginalRackIndex() {
            return originalRackIndex;
        }

        public BoardCoordinate getCoordinate() {
            return boardTile.getCoordinate();
        }

        public GameViewModel.BoardTileModel getBoardTile() {
            return boardTile;
        }
    }
}
