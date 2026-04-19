package com.kotva.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * An unlimited supply of letter tiles. [cite: 46, 760]
 * * In this "Scribble" version, the bag never runs out of tiles.
 * This class acts as a generator that randomly picks letters from a
 * predefined pool (including blank tiles) and creates new Tile objects
 * whenever a player needs to draw.
 */
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

    // Keep existing API used by current callers.
    public Tile drawTile() {
        return drawRandomTile();
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

    /**
     * Finds a tile by tile id from all tiles created for this game.
     *
     * @param tileId The tile id to look up.
     * @return The matching tile, or null if not found.
     */
    public Tile getTileById(String tileId) {
        if (tileId == null) {
            return null;
        }
        return allTilesById.get(tileId);
    }

    /**
     * Registers an existing tile instance so preview-only client state can
     * reconstruct authoritative tile ids without mutating the remaining bag.
     */
    public void indexTile(Tile tile) {
        if (tile == null) {
            return;
        }
        allTilesById.put(tile.getTileID(), tile);
    }
}
