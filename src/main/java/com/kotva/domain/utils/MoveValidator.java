package com.kotva.domain.utils;

import java.util.List;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Position;

public final class MoveValidator {

    private MoveValidator() {
    }

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
            if (p.getRow() != firstRow) {
                isHorizontal = false;
            }
            if (p.getCol() != firstCol) {
                isVertical = false;
            }
        }
        return isHorizontal || isVertical;
    }

    public static boolean firstMove(List<Position> placements) {
        for (int i = 0; i < placements.size(); i++) {
            if (placements.get(i).getRow() == 7 && placements.get(i).getCol() == 7) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotOverlapping(List<Position> placements, Board board) {
        for (int i = 0; i < placements.size(); i++) {
            Cell cell = board.getCell(placements.get(i));
            if (!cell.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isConnected(List<Position> placements, Board board) {
        for (int i = 0; i < placements.size(); i++) {
            int row = placements.get(i).getRow();
            int col = placements.get(i).getCol();

        }
        return false;
    }

    private static boolean checkNeighbor(int row, int col, Board board) {
        if (row >= 0 && row < Board.SIZE && col >= 0 && col < Board.SIZE) {
            Cell cell = board.getCell(new Position(row, col));
            return !cell.isEmpty();
        }
        return false;
    }
}