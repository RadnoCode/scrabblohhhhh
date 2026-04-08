package com.kotva.domain.utils;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.policy.BonusType;

import java.util.List;

public final class ScoreCalculator {

    private ScoreCalculator() {
    }

    /**
     *
     */
    public static int calculate(List<CandidateWord> words, GameState state, TurnDraft draft) {
        int totalScore = 0;
        Board board = state.getBoard();
        TileBag tileBag = state.getTileBag();

        for (CandidateWord candidate : words) {
            int wordScore = 0;
            int wordMultiplier = 1;

            int startRow = candidate.getStartPosition().getRow();
            int endRow = candidate.getEndPosition().getRow();
            int startCol = candidate.getStartPosition().getCol();
            int endCol = candidate.getEndPosition().getCol();

            for (int i = startRow; i <= endRow; i++) {
                for (int j = startCol; j <= endCol; j++) {
                    Position currentPos = new Position(i, j);

                    boolean isNew = false;
                    String newTileId = null;

                    if (draft != null && draft.getPlacements() != null) {
                        for (DraftPlacement dp : draft.getPlacements()) {
                            if (dp.getPosition().getRow() == i && dp.getPosition().getCol() == j) {
                                isNew = true;
                                newTileId = dp.getTileId();
                                break;
                            }
                        }
                    }

                    int currentTileScore = 0;

                    if (isNew) {
                        Tile tile = tileBag.getTileById(newTileId);
                        currentTileScore = tile.getScore();

                        Cell cell = board.getCell(currentPos);
                        BonusType bonus = cell.getBonusType();

                        if (bonus == BonusType.DOUBLE_LETTER) {
                            currentTileScore *= 2;
                        } else if (bonus == BonusType.TRIPLE_LETTER) {
                            currentTileScore *= 3;
                        } else if (bonus == BonusType.DOUBLE_WORD) {
                            wordMultiplier *= 2;
                        } else if (bonus == BonusType.TRIPLE_WORD) {
                            wordMultiplier *= 3;
                        }
                    } else {
                        Tile oldTile = board.getCell(currentPos).getPlacedTile();
                        if (oldTile != null) {
                            currentTileScore = oldTile.getScore();
                        }
                    }

                    wordScore += currentTileScore;
                }
            }

            totalScore += (wordScore * wordMultiplier);
        }

        return totalScore;
    }
}