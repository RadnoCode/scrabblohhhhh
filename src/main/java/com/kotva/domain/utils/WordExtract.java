package com.kotva.domain.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.domain.model.GameState;


public final class WordExtractor {
    private WordExtractor() {
    }

    public static List<CandidateWord> extract(TurnDraft draft) {
        return extract(draft, null);
    }

    public static List<CandidateWord> extract(TurnDraft draft, TileBag tileBag) {
        if (draft == null || draft.getPlacements() == null || draft.getPlacements().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, DraftPlacement> draftTileByPoint = buildIndex(draft.getPlacements());

        LinkedHashSet<CandidateWord> candidateWords = new LinkedHashSet<>();
        for (DraftPlacement placement : draft.getPlacements()) {
            if (placement == null || placement.getPosition() == null) {
                continue;
            }

            Position position = placement.getPosition();
            int row = position.getRow();
            int col = position.getCol();

            if (hasHorizontalNeighbor(row, col, draftTileByPoint)) {
                List<DraftPlacement> horizontalWord = collectHorizontalWord(row, col, draftTileByPoint);
                if (horizontalWord.size() >= 2) {
                    candidateWords.add(toCandidateWord(horizontalWord, tileBag));
                }
            }

            if (hasVerticalNeighbor(row, col, draftTileByPoint)) {
                List<DraftPlacement> verticalWord = collectVerticalWord(row, col, draftTileByPoint);
                if (verticalWord.size() >= 2) {
                    candidateWords.add(toCandidateWord(verticalWord, tileBag));
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

    private static boolean hasHorizontalNeighbor(int row, int col, Map<String, DraftPlacement> index) {
        return index.containsKey(toPointKey(row, col - 1))
                || index.containsKey(toPointKey(row, col + 1));
    }

    private static boolean hasVerticalNeighbor(int row, int col, Map<String, DraftPlacement> index) {
        return index.containsKey(toPointKey(row - 1, col))
                || index.containsKey(toPointKey(row + 1, col));
    }

    private static List<DraftPlacement> collectHorizontalWord(int row, int col, Map<String, DraftPlacement> index) {
        int left = col;
        while (index.containsKey(toPointKey(row, left - 1))) {
            left--;
        }

        List<DraftPlacement> word = new ArrayList<>();
        int cursor = left;
        while (index.containsKey(toPointKey(row, cursor))) {
            word.add(index.get(toPointKey(row, cursor)));
            cursor++;
        }
        return word;
    }

    private static List<DraftPlacement> collectVerticalWord(int row, int col, Map<String, DraftPlacement> index) {
        int top = row;
        while (index.containsKey(toPointKey(top - 1, col))) {
            top--;
        }

        List<DraftPlacement> word = new ArrayList<>();
        int cursor = top;
        while (index.containsKey(toPointKey(cursor, col))) {
            word.add(index.get(toPointKey(cursor, col)));
            cursor++;
        }
        return word;
    }

    private static CandidateWord toCandidateWord(List<DraftPlacement> wordTiles, TileBag tileBag) {
        StringJoiner joiner = new StringJoiner("");
        for (DraftPlacement placement : wordTiles) {
            joiner.add(resolveLetter(placement, tileBag));
        }

        Position start = wordTiles.get(0).getPosition();
        Position end = wordTiles.get(wordTiles.size() - 1).getPosition();
        return new CandidateWord(joiner.toString(), start, end);
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
··
