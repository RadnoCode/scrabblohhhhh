
package com.kotva.domain.model;

import com.kotva.policy.BonusType;

/**
 * The 15x15 Scrabble game board.
 * * This class uses a 2D array of Cell objects to represent the grid.
 * To make the board setup easy, we use a visual string array (BONUS_MAP)
 * to quickly map out all the special bonus squares (like DL, TL, DW, TW).
 */
public class Board {

    public static final int SIZE = 15;

    private static final String[] BONUS_MAP = {
            "T..d...T...d..T", // 0
            ".D...t...t...D.", // 1
            "..D...d.d...D..", // 2
            "d..D...d...D..d", // 3
            "....D.....D....", // 4
            ".t...t...t...t.", // 5
            "..d...d.d...d..", // 6
            "T..d...D...d..T", // 7 (中心点)
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

    /**
     * Gets a specific cell from the board using a Position object.
     *
     * @param position The position of the requested cell.
     * @return The Cell at the given position.
     * @throws IllegalArgumentException If the position is out of bounds.
     */
    public Cell getCell(Position position) {

        int col = position.getCol();
        int row = position.getRow();

        //If get an error, put tile back.
        if(row<0||row>=SIZE||col<0||col>=SIZE){
            throw new IllegalArgumentException("Position is out of bounds.");
        }

        return cells[row][col];
    }
    /**
     * Returns the corresponding BonusType based on the given character.
     * * @param c The character representing the bonus type from the map.
     * @return The corresponding BonusType enum.
     */
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
        // 外层循环：从第 0 行扫到第 14 行
        for (int row = 0; row < Board.SIZE; row++) {

            // 内层循环：在当前行里，从第 0 列扫到第 14 列
            for (int col = 0; col < Board.SIZE; col++) {

                // 1. 拿到当前这个坐标的格子 (Cell)
                Position pos = new Position(row, col);
                Cell cell = this.getCell(pos); // this. 可以省略

                if (!cell.isEmpty()) {
                    return false;
                }
            }

        }
        return true;
    }
    /**
     * 判断棋盘是否已经全满（没有空位了）
     */
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
