package com.kotva.domain.model;

import java.io.Serializable;

 public class Position implements Serializable {
        private int row;
        private int col;

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

    }
