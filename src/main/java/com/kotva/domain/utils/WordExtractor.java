package com.kotva.domain.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;

public final class WordExtractor {
    private WordExtractor() {
    }

    public static List<CandidateWord> extract(PlayerAction action, Board board) {
        return extract(action, null, board);
    }

    public static List<CandidateWord> extract(PlayerAction action, TileBag tileBag, Board board) {
        if (action == null || action.placements().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, ActionPlacement> draftTileByPoint = buildIndex(action.placements());
        LinkedHashSet<CandidateWord> candidateWords = new LinkedHashSet<>();

        for (ActionPlacement placement : action.placements()) {
            Position position = placement.position();
            int row = position.getRow();
            int col = position.getCol();

            if (hasTileAt(row, col - 1, draftTileByPoint, board)
                    || hasTileAt(row, col + 1, draftTileByPoint, board)) {
                CandidateWord horizontal =
                        collectHorizontalWord(row, col, draftTileByPoint, board, tileBag);
                if (horizontal.getWord().length() >= 2) {
                    candidateWords.add(horizontal);
                }
            }

            if (hasTileAt(row - 1, col, draftTileByPoint, board)
                    || hasTileAt(row + 1, col, draftTileByPoint, board)) {
                CandidateWord vertical =
                        collectVerticalWord(row, col, draftTileByPoint, board, tileBag);
                if (vertical.getWord().length() >= 2) {
                    candidateWords.add(vertical);
                }
            }
        }

        return new ArrayList<>(candidateWords);
    }

    private static Map<String, ActionPlacement> buildIndex(List<ActionPlacement> placements) {
        LinkedHashMap<String, ActionPlacement> index = new LinkedHashMap<>();
        for (ActionPlacement placement : placements) {
            Position position = placement.position();
            index.put(toPointKey(position.getRow(), position.getCol()), placement);
        }
        return index;
    }

    private static boolean hasTileAt(
            int row, int col, Map<String, ActionPlacement> index, Board board) {
        if (row < 0 || row >= Board.SIZE || col < 0 || col >= Board.SIZE) {
            return false;
        }
        if (index.containsKey(toPointKey(row, col))) {
            return true;
        }
        if (board != null) {
            Cell cell = board.getCell(new Position(row, col));
            return cell != null && !cell.isEmpty();
        }
        return false;
    }

    private static CandidateWord collectHorizontalWord(
            int row, int col, Map<String, ActionPlacement> index, Board board, TileBag tileBag) {
        int left = col;
        while (hasTileAt(row, left - 1, index, board)) {
            left--;
        }

        int right = col;
        while (hasTileAt(row, right + 1, index, board)) {
            right++;
        }

        StringBuilder wordBuilder = new StringBuilder();
        for (int currentCol = left; currentCol <= right; currentCol++) {
            wordBuilder.append(getLetterAt(row, currentCol, index, board, tileBag));
        }
        return new CandidateWord(
                wordBuilder.toString(), new Position(row, left), new Position(row, right));
    }

    private static CandidateWord collectVerticalWord(
            int row, int col, Map<String, ActionPlacement> index, Board board, TileBag tileBag) {
        int top = row;
        while (hasTileAt(top - 1, col, index, board)) {
            top--;
        }

        int bottom = row;
        while (hasTileAt(bottom + 1, col, index, board)) {
            bottom++;
        }

        StringBuilder wordBuilder = new StringBuilder();
        for (int currentRow = top; currentRow <= bottom; currentRow++) {
            wordBuilder.append(getLetterAt(currentRow, col, index, board, tileBag));
        }
        return new CandidateWord(
                wordBuilder.toString(), new Position(top, col), new Position(bottom, col));
    }

    private static String getLetterAt(
            int row, int col, Map<String, ActionPlacement> index, Board board, TileBag tileBag) {
        String key = toPointKey(row, col);
        if (index.containsKey(key)) {
            return resolveLetter(index.get(key), tileBag);
        }

        if (board != null) {
            Cell cell = board.getCell(new Position(row, col));
            if (cell != null && !cell.isEmpty()) {
                Tile oldTile = cell.getPlacedTile();
                if (oldTile.isBlank() && oldTile.getAssignedLetter() != null) {
                    return String.valueOf(oldTile.getAssignedLetter());
                }
                return String.valueOf(oldTile.getLetter());
            }
        }
        return "";
    }

    private static String resolveLetter(ActionPlacement placement, TileBag tileBag) {
        if (tileBag == null) {
            return placement.tileId();
        }

        Tile tile = tileBag.getTileById(placement.tileId());
        if (tile == null) {
            return placement.tileId();
        }
        if (tile.isBlank() && tile.getAssignedLetter() != null) {
            return String.valueOf(tile.getAssignedLetter());
        }
        return String.valueOf(tile.getLetter());
    }

    private static String toPointKey(int row, int col) {
        return row + ":" + col;
    }
}
