package com.kotva.domain.utils;

import java.util.List;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Position;

/**
 * 移动合法性校验器
 * 负责执行 Scrabble 游戏落子的核心物理规则校验
 */
public final class MoveValidator {

    private MoveValidator() {
        // 私有构造方法，防止被实例化
    }

    /**
     * FR25.1: 校验所有落子是否在同一直线上（同一行或同一列），禁止斜对角放置
     */
    public static boolean isStraightLine(List<Position> placements) {
        if (placements == null || placements.size() <= 1) {
            return true;
        }

        int firstRow = placements.get(0).getRow();
        int firstCol = placements.get(0).getCol();

        boolean isHorizontal = true;
        boolean isVertical = true;

        for (int i = 1; i < placements.size(); i++) {
            Position p = placements.get(i);
            // 校验行一致性
            if (p.getRow() != firstRow) {
                isHorizontal = false;
            }
            // 校验列一致性
            if (p.getCol() != firstCol) {
                isVertical = false;
            }
        }
        return isHorizontal || isVertical;
    }

    /**
     * FR25.5: 首回合校验。首位玩家落子必须覆盖中心点 (7, 7)
     */
    public static boolean firstMove(List<Position> placements) {
        for (int i = 0; i < placements.size(); i++) {
            if (placements.get(i).getRow() == 7 && placements.get(i).getCol() == 7) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重叠校验：确保落子目标格当前没有任何既有棋子
     */
    public static boolean isNotOverlapping(List<Position> placements, Board board) {
        for (int i = 0; i < placements.size(); i++) {
            Cell cell = board.getCell(placements.get(i));
            if (!cell.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * FR25.3: 连接校验。除首回合外，新落子必须至少与棋盘上已有的一个棋子相邻
     */
    public static boolean isConnected(List<Position> placements, Board board) {
        for (int i = 0; i < placements.size(); i++) {
            int row = placements.get(i).getRow();
            int col = placements.get(i).getCol();

            // 定义并校验上下左右四个邻居方向
            if (checkNeighbor(row - 1, col, board)) return true; // 上
            if (checkNeighbor(row + 1, col, board)) return true; // 下
            if (checkNeighbor(row, col - 1, board)) return true; // 左
            if (checkNeighbor(row, col + 1, board)) return true; // 右
        }
        return false;
    }

    /**
     * 辅助方法：检查指定坐标是否存在既有棋子，包含边界保护
     */
    private static boolean checkNeighbor(int row, int col, Board board) {
        if (row >= 0 && row < Board.SIZE && col >= 0 && col < Board.SIZE) {
            Cell cell = board.getCell(new Position(row, col));
            return !cell.isEmpty();
        }
        return false;
    }
}