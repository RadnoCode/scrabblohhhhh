package com.kotva.application.result;

import com.kotva.domain.model.Board;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BoardSnapshotFactory {

    private BoardSnapshotFactory() {
    }

    public static BoardSnapshot fromBoard(Board board) {
        Objects.requireNonNull(board, "board cannot be null.");

        List<BoardCellSnapshot> cells = new ArrayList<>(Board.SIZE * Board.SIZE);
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Cell cell = board.getCell(new Position(row, col));
                Tile placedTile = cell.getPlacedTile();
                Character letter = null;
                boolean blank = false;
                if (placedTile != null) {
                    blank = placedTile.isBlank();
                    letter =
                    placedTile.getAssignedLetter() != null
                    ? placedTile.getAssignedLetter()
                    : placedTile.getLetter();
                }
                cells.add(new BoardCellSnapshot(row, col, cell.getBonusType(), letter, blank));
            }
        }
        return new BoardSnapshot(cells);
    }
}