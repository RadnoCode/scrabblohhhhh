package com.kotva.ai;

import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import com.kotva.policy.PlayerType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Captures the board and rack state needed by the AI engine.
 *
 * @param boardCells board cells in row-major order
 * @param rack current AI rack
 * @param unseenTiles tiles not visible to the AI
 * @param aiScore current AI score
 * @param opponentScore current opponent score
 */
public record AiPositionSnapshot(
    List<BoardCell> boardCells,
    String rack,
    String unseenTiles,
    int aiScore,
    int opponentScore) {
    public static final int BOARD_SIDE = 15;
    public static final int BOARD_CELL_COUNT = BOARD_SIDE * BOARD_SIDE;

    /**
     * Validates the AI position snapshot.
     */
    public AiPositionSnapshot {
        boardCells = List.copyOf(Objects.requireNonNull(boardCells, "boardCells cannot be null."));
        if (boardCells.size() != BOARD_CELL_COUNT) {
            throw new IllegalArgumentException(
                "boardCells must contain exactly " + BOARD_CELL_COUNT + " cells.");
        }

        rack = Objects.requireNonNull(rack, "rack cannot be null.");
        unseenTiles = Objects.requireNonNull(unseenTiles, "unseenTiles cannot be null.");
    }

    /**
     * Builds an AI position snapshot from the current session.
     *
     * @param session active game session
     * @return AI position snapshot
     */
    public static AiPositionSnapshot fromSession(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");

        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        if (currentPlayer.getPlayerType() != PlayerType.AI) {
            throw new IllegalStateException("Current player is not AI.");
        }

        Player opponent = resolveOpponent(session, currentPlayer);
        return new AiPositionSnapshot(
            buildBoardCells(session.getGameState().getBoard()),
            encodeRack(currentPlayer),
            buildUnseenTiles(session, opponent),
            currentPlayer.getScore(),
            opponent.getScore());
    }

    private static Player resolveOpponent(GameSession session, Player currentPlayer) {
        return session.getGameState().getPlayers().stream()
            .filter(player -> !Objects.equals(player.getPlayerId(), currentPlayer.getPlayerId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("AI game requires an opponent."));
    }

    private static List<BoardCell> buildBoardCells(Board board) {
        List<BoardCell> cells = new ArrayList<>(BOARD_CELL_COUNT);
        for (int row = 0; row < BOARD_SIDE; row++) {
            for (int col = 0; col < BOARD_SIDE; col++) {
                Cell cell = board.getCell(new Position(row, col));
                if (cell.isEmpty()) {
                    cells.add(new BoardCell(false, '\0', false, null));
                    continue;
                }

                Tile tile = cell.getPlacedTile();
                Character assignedLetter = tile.isBlank() ? resolveVisibleLetter(tile) : null;
                cells.add(new BoardCell(true, resolveVisibleLetter(tile), tile.isBlank(), assignedLetter));
            }
        }
        return cells;
    }

    private static String encodeRack(Player player) {
        StringBuilder builder = new StringBuilder();
        for (RackSlot slot : player.getRack().getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }
            builder.append(encodeTile(slot.getTile()));
        }
        return builder.toString();
    }

    private static String buildUnseenTiles(GameSession session, Player opponent) {
        StringBuilder builder = new StringBuilder();
        for (Tile tile : session.getGameState().getTileBag().getRemainingTiles()) {
            builder.append(encodeTile(tile));
        }
        for (RackSlot slot : opponent.getRack().getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }
            builder.append(encodeTile(slot.getTile()));
        }
        return builder.toString();
    }

    private static char encodeTile(Tile tile) {
        Objects.requireNonNull(tile, "tile cannot be null.");
        if (tile.isBlank()) {
            return '?';
        }
        return Character.toUpperCase(tile.getLetter());
    }

    private static char resolveVisibleLetter(Tile tile) {
        if (tile.isBlank() && tile.getAssignedLetter() != null) {
            return Character.toUpperCase(tile.getAssignedLetter());
        }
        return Character.toUpperCase(tile.getLetter());
    }

    /**
     * Stores the AI-visible state of one board cell.
     *
     * @param occupied whether the cell has a tile
     * @param letter visible letter in the cell
     * @param blank whether the tile is blank
     * @param assignedLetter letter assigned to a blank tile
     */
    public record BoardCell(boolean occupied, char letter, boolean blank, Character assignedLetter) {

        /**
         * Validates and normalizes the board cell.
         */
        public BoardCell {
            if (!occupied) {
                letter = '\0';
                assignedLetter = null;
            } else {
                letter = Character.toUpperCase(letter);
                if (!Character.isAlphabetic(letter)) {
                    throw new IllegalArgumentException("Occupied board cells must contain a letter.");
                }
                if (blank && assignedLetter != null) {
                    assignedLetter = Character.toUpperCase(assignedLetter);
                }
            }
        }
    }
}
