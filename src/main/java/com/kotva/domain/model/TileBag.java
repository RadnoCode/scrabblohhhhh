package com.kotva.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TileBag {
    private static final String LETTER_POOL =
    "AAAAAAAAABBCCDDDDEEEEEEEEEEEEFFGGGHHIIIIIIIIIJKLLLLMMNNNNNNOOOOOOOOOPPQRRRRRRSSSSTTTTTTUUUUVVWWXYYZ  ";
    private final Random random = new Random();
    private final List<Tile> tiles = new ArrayList<>();
    private final Map<String, Tile> allTilesById = new HashMap<>();

    public TileBag() {
        initialize();
    }

    public void initialize() {
        tiles.clear();
        allTilesById.clear();
        for (int i = 0; i < LETTER_POOL.length(); i++) {
            char letter = LETTER_POOL.charAt(i);
            boolean isBlank = (letter == ' ');
            Tile tile = new Tile(UUID.randomUUID().toString(), letter, getScoreForLetter(letter), isBlank);
            tiles.add(tile);
            allTilesById.put(tile.getTileID(), tile);
        }
    }

    private int getScoreForLetter(char letter) {
        switch (letter) {
        case 'A':
        case 'E':
        case 'I':
        case 'O':
        case 'U':
        case 'L':
        case 'N':
        case 'S':
        case 'T':
        case 'R':
            return 1;
        case 'D':
        case 'G':
            return 2;
        case 'B':
        case 'C':
        case 'M':
        case 'P':
            return 3;
        case 'F':
        case 'H':
        case 'V':
        case 'W':
        case 'Y':
            return 4;
        case 'K':
            return 5;
        case 'J':
        case 'X':
            return 8;
        case 'Q':
        case 'Z':
            return 10;
        default:
            return 0;

        }
    }

    public Tile drawRandomTile() {
        if (isEmpty()) {
            return null;
        }

        int index = random.nextInt(tiles.size());
        return tiles.remove(index);
    }

    public Tile drawTile() {
        return drawRandomTile();
    }

    public Tile takeTileByLetter(char letter) {
        char normalizedLetter = Character.toUpperCase(letter);
        for (int index = 0; index < tiles.size(); index++) {
            Tile tile = tiles.get(index);
            if (!tile.isBlank() && Character.toUpperCase(tile.getLetter()) == normalizedLetter) {
                tiles.remove(index);
                return tile;
            }
        }
        throw new IllegalStateException("No tile available for letter " + normalizedLetter + ".");
    }

    public Tile takeBlankTile() {
        for (int index = 0; index < tiles.size(); index++) {
            Tile tile = tiles.get(index);
            if (tile.isBlank()) {
                tiles.remove(index);
                return tile;
            }
        }
        throw new IllegalStateException("No blank tile available.");
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    public int size() {
        return tiles.size();
    }

    public List<Tile> getRemainingTiles() {
        return List.copyOf(tiles);
    }

    public Tile getTileById(String tileId) {
        if (tileId == null) {
            return null;
        }
        return allTilesById.get(tileId);
    }

    public void indexTile(Tile tile) {
        if (tile == null) {
            return;
        }
        allTilesById.put(tile.getTileID(), tile);
    }

    public void returnTile(Tile tile) {
        if (tile == null) {
            return;
        }
        if (tile.isBlank()) {
            tile.clearAssignedLetter();
        }
        indexTile(tile);
        tiles.add(tile);
    }

    public boolean removeTileById(String tileId) {
        if (tileId == null) {
            return false;
        }
        for (int index = 0; index < tiles.size(); index++) {
            if (tileId.equals(tiles.get(index).getTileID())) {
                tiles.remove(index);
                return true;
            }
        }
        return false;
    }
}
