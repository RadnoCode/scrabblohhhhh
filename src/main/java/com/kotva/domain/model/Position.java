package com.kotva.domain.model;

import java.util.Objects;
/**
 * A specific coordinate (row and column) on the game board.
 * * This class is immutable, meaning once a position is created, it cannot be changed.
 * It properly overrides equals() and hashCode() so we can easily check if two
 * positions point to the exact same spot, or use them safely in Lists and Maps.
 */
//position是final吗   是 因为Position是固定的，代码后面还要用于哈希查找（为了在100个里面找的时候更快），
// 如果玩家把牌从 (7,7) 移到了 (7,8)，代码里的做法是直接给这块牌换一个新的对象：
// this.position = new Position(7, 8);，而不是去修改旧坐标

    public class Position {
        private final int row;
        private final int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        // Overrides equals to ensure that two Position objects with the same row and col are considered equal.
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return row == position.row && col == position.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

