package com.kotva.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TileBag implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String LETTER_POOL =
    "AAAAAAAAABBCCDDDDEEEEEEEEEEEEFFGGGHHIIIIIIIIIJKLLLLMMNNNNNNOOOOOOOOOPPQRRRRRRSSSSTTTTTTUUUUVVWWXYYZ  ";
    private final Random random = new Random();
    private final List<Tile> tiles = new ArrayList<>();
    private final Map<String, Tile> allTilesById = new HashMap<>();
    private final boolean infiniteSupply;

    public TileBag() {
        this(false);
    }

    private TileBag(boolean infiniteSupply) {
        this.infiniteSupply = infiniteSupply;
        if (!infiniteSupply) {
            initialize();
        }
    }

    public static TileBag infinite() {
        return new TileBag(true);
    }

    public void initialize() {
        tiles.clear();
        allTilesById.clear();
        if (infiniteSupply) {
            return;
        }
        for (int i = 0; i < LETTER_POOL.length(); i++) {
            char letter = LETTER_POOL.charAt(i);
            boolean isBlank = (letter == ' ');
            Tile tile = createTile(letter);
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
        if (infiniteSupply) {
            return createAndIndexRandomTile();
        }
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
        if (infiniteSupply) {
            if (normalizedLetter < 'A' || normalizedLetter > 'Z') {
                throw new IllegalStateException("No tile available for letter " + normalizedLetter + ".");
            }
            return createAndIndexTile(normalizedLetter);
        }
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
        if (infiniteSupply) {
            return createAndIndexTile(' ');
        }
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
        if (infiniteSupply) {
            return false;
        }
        return tiles.isEmpty();
    }

    public int size() {
        if (infiniteSupply) {
            return Integer.MAX_VALUE;
        }
        return tiles.size();
    }

    public List<Tile> getRemainingTiles() {
        if (infiniteSupply) {
            return List.of();
        }
        return List.copyOf(tiles);
    }

    public boolean isInfiniteSupply() {
        return infiniteSupply;
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
        if (infiniteSupply) {
            return;
        }
        tiles.add(tile);
    }

    public boolean removeTileById(String tileId) {
        if (tileId == null) {
            return false;
        }
        if (infiniteSupply) {
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

    private Tile createAndIndexRandomTile() {
        int index = random.nextInt(LETTER_POOL.length());
        return createAndIndexTile(LETTER_POOL.charAt(index));
    }

    private Tile createAndIndexTile(char letter) {
        Tile tile = createTile(letter);
        allTilesById.put(tile.getTileID(), tile);
        return tile;
    }

    private Tile createTile(char letter) {
        boolean isBlank = letter == ' ';
        return new Tile(UUID.randomUUID().toString(), letter, getScoreForLetter(letter), isBlank);
    }
}
