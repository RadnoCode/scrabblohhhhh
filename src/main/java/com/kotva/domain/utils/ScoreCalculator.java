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

/**
 * 算分计算器
 * 负责根据提取出的单词、棋盘上的奖励格子以及新落子坐标，计算最终得分。
 */
public final class ScoreCalculator {

    private ScoreCalculator() {
        // 工具类，私有化构造方法
    }

    /**
     * 计算本次落子形成的所有单词的总得分
     *
     * @param words 本次提取出的所有候选单词（主词 + 交叉词）
     * @param state 当前游戏状态（包含棋盘和牌袋）
     * @param draft 玩家当前回合的落子草稿
     * @return 本次动作的总得分
     */
    public static int calculate(List<CandidateWord> words, GameState state, TurnDraft draft) {
        int totalScore = 0;
        Board board = state.getBoard();
        TileBag tileBag = state.getTileBag();

        // 遍历本次形成的所有单词
        for (CandidateWord candidate : words) {
            int wordScore = 0;
            int wordMultiplier = 1;

            int startRow = candidate.getStartPosition().getRow();
            int endRow = candidate.getEndPosition().getRow();
            int startCol = candidate.getStartPosition().getCol();
            int endCol = candidate.getEndPosition().getCol();

            // 遍历该单词覆盖的每一个坐标
            for (int i = startRow; i <= endRow; i++) {
                for (int j = startCol; j <= endCol; j++) {
                    Position currentPos = new Position(i, j);

                    boolean isNew = false;
                    String newTileId = null;

                    // 判断当前坐标上的牌是否为本次新落子
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
                        // 处理新牌：获取基础分并叠加地块奖励
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
                        // 处理老牌：仅获取基础分，不触发地块奖励
                        Tile oldTile = board.getCell(currentPos).getPlacedTile();
                        if (oldTile != null) {
                            currentTileScore = oldTile.getScore();
                        }
                    }

                    // 累加当前字母的分数到单词基础分
                    wordScore += currentTileScore;
                }
            }

            // 计算单个单词最终得分并累加到总分
            totalScore += (wordScore * wordMultiplier);
        }

        return totalScore;
    }
}