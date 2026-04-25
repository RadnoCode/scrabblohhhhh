
package com.kotva.domain.model;

import com.kotva.policy.BonusType;
import java.io.Serializable;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int SIZE = 15;

    private static final String[] BONUS_MAP = {
        "T..d...T...d..T", // 0
        ".D...t...t...D.", // 1
        "..D...d.d...D..", // 2
        "d..D...d...D..d", // 3
        "....D.....D....", // 4
        ".t...t...t...t.", // 5
        "..d...d.d...d..", // 6
        "T..d...D...d..T", // 7
        "..d...d.d...d..", // 8
        ".t...t...t...t.", // 9
        "....D.....D....", // 10
        "d..D...d...D..d", // 11
        "..D...d.d...D..", // 12
        ".D...t...t...D.", // 13
        "T..d...T...d..T"  // 14
    };

    private final Cell[][] cells;

    public Board() {

        cells = new Cell[SIZE][SIZE];

        for(int row=0; row<SIZE;row++){
            for(int col=0; col<SIZE;col++){
                Position position = new Position(row,col);
                char c = BONUS_MAP[row].charAt(col);
                BonusType bonusType = parseBonusType(c);
                Cell cell = new Cell(position,bonusType);
                cells[row][col] = cell;

            }
        }
    }

    public Cell getCell(Position position) {

        int col = position.getCol();
        int row = position.getRow();

        if(row<0||row>=SIZE||col<0||col>=SIZE){
            throw new IllegalArgumentException("Position is out of bounds.");
        }

        return cells[row][col];
    }

    private BonusType parseBonusType(char c) {
        switch (c) {
        case 'T': return BonusType.TRIPLE_WORD;
        case 'D': return BonusType.DOUBLE_WORD;
        case 't': return BonusType.TRIPLE_LETTER;
        case 'd': return BonusType.DOUBLE_LETTER;
        default:  return BonusType.NONE;
        }
    }

    public boolean isEmpty() {
        for (int row = 0; row < Board.SIZE; row++) {

            for (int col = 0; col < Board.SIZE; col++) {

                Position pos = new Position(row, col);
                Cell cell = this.getCell(pos);

                if (!cell.isEmpty()) {
                    return false;
                }
            }

        }
        return true;
    }

    public boolean isFull() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Position pos = new Position(row, col);
                Cell cell = this.getCell(pos);

                if (cell.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
