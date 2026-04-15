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
            Position position = placements.get(i);
            if (position.getRow() != firstRow) {
                isHorizontal = false;
            }
            if (position.getCol() != firstCol) {
                isVertical = false;
            }
        }
        return isHorizontal || isVertical;
    }

    public static boolean firstMove(List<Position> placements) {
        for (Position placement : placements) {
            if (placement.getRow() == 7 && placement.getCol() == 7) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotOverlapping(List<Position> placements, Board board) {
        for (Position placement : placements) {
            Cell cell = board.getCell(placement);
            if (!cell.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // A legal Scrabble move must form one continuous span after combining new tiles
    // with any pre-existing tiles already on the board.
    public static boolean isContiguous(List<Position> placements, Board board) {
        if (placements == null || placements.size() <= 1) {
            return true;
        }

        Position first = placements.get(0);
        boolean sameRow = true;
        boolean sameCol = true;
        int minRow = first.getRow();
        int maxRow = first.getRow();
        int minCol = first.getCol();
        int maxCol = first.getCol();

        for (int i = 1; i < placements.size(); i++) {
            Position position = placements.get(i);
            if (position.getRow() != first.getRow()) {
                sameRow = false;
            }
            if (position.getCol() != first.getCol()) {
                sameCol = false;
            }

            minRow = Math.min(minRow, position.getRow());
            maxRow = Math.max(maxRow, position.getRow());
            minCol = Math.min(minCol, position.getCol());
            maxCol = Math.max(maxCol, position.getCol());
        }

        if (!sameRow && !sameCol) {
            return false;
        }

        if (sameRow) {
            int row = first.getRow();
            for (int col = minCol; col <= maxCol; col++) {
                if (!hasPlacementAt(placements, row, col)
                        && board.getCell(new Position(row, col)).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        int col = first.getCol();
        for (int row = minRow; row <= maxRow; row++) {
            if (!hasPlacementAt(placements, row, col)
                    && board.getCell(new Position(row, col)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isConnected(List<Position> placements, Board board) {
        for (Position placement : placements) {
            int row = placement.getRow();
            int col = placement.getCol();
            if (checkNeighbor(row - 1, col, board)) {
                return true;
            }
            if (checkNeighbor(row + 1, col, board)) {
                return true;
            }
            if (checkNeighbor(row, col - 1, board)) {
                return true;
            }
            if (checkNeighbor(row, col + 1, board)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPlacementAt(List<Position> placements, int row, int col) {
        for (Position placement : placements) {
            if (placement.getRow() == row && placement.getCol() == col) {
                return true;
            }
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
