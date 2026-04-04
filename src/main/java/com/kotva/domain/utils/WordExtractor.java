package com.kotva.domain.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.Cell;

public final class WordExtractor {
    private WordExtractor() {
    }

    public static List<CandidateWord> extract(TurnDraft draft, Board board) {
        return extract(draft, null, board);
    }

    // 引入 Board 参数。因为提词不仅要看玩家刚刚放下的 draft 新牌，
    // 还必须顺藤摸瓜，把棋盘上连着的 Cell 里的老牌也一起扫出来，否则没法和老词接龙。
    public static List<CandidateWord> extract(TurnDraft draft, TileBag tileBag, Board board) {
        if (draft == null || draft.getPlacements() == null || draft.getPlacements().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, DraftPlacement> draftTileByPoint = buildIndex(draft.getPlacements());
        LinkedHashSet<CandidateWord> candidateWords = new LinkedHashSet<>();

        for (DraftPlacement placement : draft.getPlacements()) {
            if (placement == null || placement.getPosition() == null) {
                continue;
            }

            int row = placement.getPosition().getRow();
            int col = placement.getPosition().getCol();

            // 横向扫描
            if (hasTileAt(row, col - 1, draftTileByPoint, board) || hasTileAt(row, col + 1, draftTileByPoint, board)) {
                CandidateWord hWord = collectHorizontalWord(row, col, draftTileByPoint, board, tileBag);
                if (hWord.getWord().length() >= 2) {
                    candidateWords.add(hWord);
                }
            }

            // 纵向扫描
            if (hasTileAt(row - 1, col, draftTileByPoint, board) || hasTileAt(row + 1, col, draftTileByPoint, board)) {
                CandidateWord vWord = collectVerticalWord(row, col, draftTileByPoint, board, tileBag);
                if (vWord.getWord().length() >= 2) {
                    candidateWords.add(vWord);
                }
            }
        }

        return new ArrayList<>(candidateWords);
    }

    private static Map<String, DraftPlacement> buildIndex(List<DraftPlacement> placements) {
        java.util.LinkedHashMap<String, DraftPlacement> index = new java.util.LinkedHashMap<>();
        for (DraftPlacement placement : placements) {
            if (placement == null || placement.getPosition() == null) {
                continue;
            }
            Position position = placement.getPosition();
            index.put(toPointKey(position.getRow(), position.getCol()), placement);
        }
        return index;
    }

    // 统一的坐标探测雷达。
    // 无论是 draft 里的新牌，还是 Board 上的老牌，只要这个坐标有实体字母，就返回 true。
    private static boolean hasTileAt(int row, int col, Map<String, DraftPlacement> index, Board board) {
        // 越界保护
        if (row < 0 || row >= Board.SIZE || col < 0 || col >= Board.SIZE) {
            return false;
        }
        // 1. 查新牌
        if (index.containsKey(toPointKey(row, col))) {
            return true;
        }
        // 2. 查老牌（调用了 Cell 的 isEmpty 方法）
        if (board != null) {
            Cell cell = board.getCell(new Position(row, col));
            return cell != null && !cell.isEmpty();
        }
        return false;
    }

    // 为什么不返回 List<DraftPlacement> 而是直接返回 CandidateWord？
    // 因为老牌是 Cell 里的 Tile，没法强转成 DraftPlacement！
    // 既然收集列表会引发类型冲突，咱们干脆在扫描时直接把字母抠出来拼成 String，一步到位。
    private static CandidateWord collectHorizontalWord(int row, int col, Map<String, DraftPlacement> index, Board board, TileBag tileBag) {
        int left = col;
        while (hasTileAt(row, left - 1, index, board)) {
            left--;
        }

        int right = col;
        while (hasTileAt(row, right + 1, index, board)) {
            right++;
        }

        StringBuilder wordBuilder = new StringBuilder();
        for (int c = left; c <= right; c++) {
            wordBuilder.append(getLetterAt(row, c, index, board, tileBag));
        }
        return new CandidateWord(wordBuilder.toString(), new Position(row, left), new Position(row, right));
    }

    private static CandidateWord collectVerticalWord(int row, int col, Map<String, DraftPlacement> index, Board board, TileBag tileBag) {
        int top = row;
        while (hasTileAt(top - 1, col, index, board)) {
            top--;
        }

        int bottom = row;
        while (hasTileAt(bottom + 1, col, index, board)) {
            bottom++;
        }

        StringBuilder wordBuilder = new StringBuilder();
        for (int r = top; r <= bottom; r++) {
            wordBuilder.append(getLetterAt(r, col, index, board, tileBag));
        }
        return new CandidateWord(wordBuilder.toString(), new Position(top, col), new Position(bottom, col));
    }

    // 统一获取字母的方法
    // 负责把新牌的字面值，和 Board 老牌的字面值统一提取成字符串。
    private static String getLetterAt(int row, int col, Map<String, DraftPlacement> index, Board board, TileBag tileBag) {
        String key = toPointKey(row, col);
        // 如果是新下的牌
        if (index.containsKey(key)) {
            return resolveLetter(index.get(key), tileBag);
        }
        // 如果是棋盘上的老牌
        if (board != null) {
            Cell cell = board.getCell(new Position(row, col));
            if (cell != null && !cell.isEmpty()) {
                Tile oldTile = cell.getPlacedTile();
                // 兼容空白牌(Blank Tile)的逻辑
                if (oldTile.isBlank() && oldTile.getAssignedLetter() != null) {
                    return String.valueOf(oldTile.getAssignedLetter());
                }
                return String.valueOf(oldTile.getLetter());
            }
        }
        return "";
    }

    private static String resolveLetter(DraftPlacement placement, TileBag tileBag) {
        if (placement == null || placement.getTileId() == null) {
            return "";
        }
        if (tileBag == null) {
            return placement.getTileId();
        }

        Tile tile = tileBag.getTileById(placement.getTileId());
        if (tile == null) {
            return placement.getTileId();
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