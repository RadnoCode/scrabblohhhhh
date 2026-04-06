package com.kotva.domain.utils;

import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.PlayerAction;
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

    public static int calculate(List<CandidateWord> words, GameState state, PlayerAction action) {
        int totalScore = 0;
        for (CandidateWord candidate : words) {
            totalScore += calculateWordScore(candidate, state, action);
        }
        return totalScore;
    }

    public static int calculateWordScore(
            CandidateWord candidate, GameState state, PlayerAction action) {
        Board board = state.getBoard();
        TileBag tileBag = state.getTileBag();
        int wordScore = 0;
        int wordMultiplier = 1;

        int startRow = candidate.getStartPosition().getRow();
        int endRow = candidate.getEndPosition().getRow();
        int startCol = candidate.getStartPosition().getCol();
        int endCol = candidate.getEndPosition().getCol();

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Position currentPos = new Position(row, col);
                boolean isNew = false;
                String newTileId = null;

                if (action != null) {
                    for (ActionPlacement placement : action.placements()) {
                        if (placement.position().getRow() == row
                                && placement.position().getCol() == col) {
                            isNew = true;
                            newTileId = placement.tileId();
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

        return wordScore * wordMultiplier;
    }
}
