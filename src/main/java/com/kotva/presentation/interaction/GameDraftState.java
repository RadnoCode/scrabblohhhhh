package com.kotva.presentation.interaction;

import com.kotva.presentation.viewmodel.BoardCoordinate;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameDraftState {
    private static final int RACK_SLOT_COUNT = 7;

    private final List<GameViewModel.TileModel> baseRackTiles;
    private final List<String> rackOrderTileIds;
    private final Map<BoardCoordinate, GameViewModel.BoardTileModel> baseBoardTiles;
    private final Map<String, DraftPlacementModel> draftPlacementsByTileId;

    public GameDraftState() {
        this.baseRackTiles = new ArrayList<>();
        this.rackOrderTileIds = new ArrayList<>();
        this.baseBoardTiles = new LinkedHashMap<>();
        this.draftPlacementsByTileId = new LinkedHashMap<>();
        resetBaseRackTiles();
    }

    public void syncSnapshot(
        List<GameViewModel.TileModel> rackTiles, List<GameViewModel.BoardTileModel> boardTiles) {
        syncBaseRackTiles(Objects.requireNonNull(rackTiles, "rackTiles cannot be null."));

        baseBoardTiles.clear();
        draftPlacementsByTileId.clear();
        for (GameViewModel.BoardTileModel boardTile :
            Objects.requireNonNull(boardTiles, "boardTiles cannot be null.")) {
            baseBoardTiles.put(
                boardTile.getCoordinate(),
                boardTile.isDraft() ? createUnderlyingBoardCell(boardTile) : boardTile);
            if (boardTile.isDraft()) {
                draftPlacementsByTileId.put(
                    boardTile.getTile().getTileId(),
                    new DraftPlacementModel(boardTile, resolveRackIndex(boardTile.getTile().getTileId())));
            }
        }
    }

    public boolean rearrangeRackTiles() {
        List<GameViewModel.TileModel> occupiedTiles = new ArrayList<>();
        for (GameViewModel.TileModel tileModel : baseRackTiles) {
            if (!tileModel.isEmpty()) {
                occupiedTiles.add(tileModel);
            }
        }
        if (occupiedTiles.size() <= 1) {
            return false;
        }

        List<String> currentOrder = toTileIdOrder(occupiedTiles);
        List<String> shuffledOrder = new ArrayList<>(currentOrder);
        for (int attempt = 0; attempt < 8 && shuffledOrder.equals(currentOrder); attempt++) {
            Collections.shuffle(occupiedTiles);
            shuffledOrder = toTileIdOrder(occupiedTiles);
        }
        if (shuffledOrder.equals(currentOrder)) {
            return false;
        }

        baseRackTiles.clear();
        baseRackTiles.addAll(occupiedTiles);
        while (baseRackTiles.size() < RACK_SLOT_COUNT) {
            baseRackTiles.add(GameViewModel.TileModel.empty());
        }

        syncRackOrderTileIdsFromBaseRack();
        remapDraftPlacementRackIndexes();
        return true;
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
        GameViewModel.BoardTileModel baseBoardTile = baseBoardTiles.get(coordinate);
        if (baseBoardTile != null && !baseBoardTile.getTile().isEmpty()) {
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
        rackOrderTileIds.clear();
        for (int index = 0; index < RACK_SLOT_COUNT; index++) {
            baseRackTiles.add(GameViewModel.TileModel.empty());
        }
    }

    private void syncBaseRackTiles(List<GameViewModel.TileModel> rackTiles) {
        Map<String, GameViewModel.TileModel> remainingTilesById = new LinkedHashMap<>();
        for (GameViewModel.TileModel tileModel : rackTiles) {
            if (!tileModel.isEmpty()) {
                remainingTilesById.put(tileModel.getTileId(), tileModel);
            }
        }

        baseRackTiles.clear();
        for (String tileId : rackOrderTileIds) {
            GameViewModel.TileModel orderedTile = remainingTilesById.remove(tileId);
            if (orderedTile != null) {
                baseRackTiles.add(orderedTile);
            }
        }
        baseRackTiles.addAll(remainingTilesById.values());
        while (baseRackTiles.size() < RACK_SLOT_COUNT) {
            baseRackTiles.add(GameViewModel.TileModel.empty());
        }

        syncRackOrderTileIdsFromBaseRack();
    }

    private void syncRackOrderTileIdsFromBaseRack() {
        rackOrderTileIds.clear();
        for (GameViewModel.TileModel tileModel : baseRackTiles) {
            if (!tileModel.isEmpty()) {
                rackOrderTileIds.add(tileModel.getTileId());
            }
        }
    }

    private void remapDraftPlacementRackIndexes() {
        if (draftPlacementsByTileId.isEmpty()) {
            return;
        }

        Map<String, DraftPlacementModel> remappedPlacements = new LinkedHashMap<>();
        for (Map.Entry<String, DraftPlacementModel> entry : draftPlacementsByTileId.entrySet()) {
            DraftPlacementModel placement = entry.getValue();
            remappedPlacements.put(
                entry.getKey(),
                new DraftPlacementModel(
                placement.getBoardTile(),
                resolveRackIndex(placement.getTile().getTileId())));
        }

        draftPlacementsByTileId.clear();
        draftPlacementsByTileId.putAll(remappedPlacements);
    }

    private List<String> toTileIdOrder(List<GameViewModel.TileModel> tileModels) {
        List<String> tileIds = new ArrayList<>();
        for (GameViewModel.TileModel tileModel : tileModels) {
            tileIds.add(tileModel.getTileId());
        }
        return tileIds;
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

    private GameViewModel.BoardTileModel createUnderlyingBoardCell(
        GameViewModel.BoardTileModel boardTile) {
        return new GameViewModel.BoardTileModel(
            boardTile.getCoordinate(),
            GameViewModel.TileModel.empty(),
            boardTile.getBonusType(),
            false,
            boardTile.isPreviewValid(),
            boardTile.isPreviewInvalid(),
            boardTile.isMainWordHighlighted(),
            boardTile.isCrossWordHighlighted(),
            boardTile.isMainWordPreviewValid(),
            boardTile.isMainWordPreviewInvalid());
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