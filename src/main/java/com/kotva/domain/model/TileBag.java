package com.kotva.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TileBag {
    private static final String LETTER_pool =
            "AAAAAAAAABBCCDDDDEEEEEEEEEEEEFFGGGHHIIIIIIIIIJKLLLLMMNNNNNNOOOOOOOOOPPQRRRRRRSSSSTTTTTTUUUUVVWWXYYZ  ";
    private final Random random = new Random();

     int getScoreForLetter(char letter) {


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

    public Tile drawTile() {

        int index = random.nextInt(LETTER_pool.length());
        char c = LETTER_pool.charAt(index);
        int score = getScoreForLetter(c);
        boolean isBlank = (c == ' ');

        return new Tile(UUID.randomUUID().toString(), c, score, isBlank);
    }
}