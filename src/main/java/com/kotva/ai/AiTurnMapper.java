package com.kotva.ai;

import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maps AI move output onto real rack tiles.
 */
public final class AiTurnMapper {

    private AiTurnMapper() {
    }

    /**
     * Resolves an AI move against the given player's rack.
     *
     * @param player player whose rack will be used
     * @param move AI move to resolve
     * @return resolved move with real tiles
     */
    public static ResolvedMove resolve(Player player, AiMove move) {
        Objects.requireNonNull(player, "player cannot be null.");
        Objects.requireNonNull(move, "move cannot be null.");

        if (move.action() == AiMove.Action.PASS) {
            return new ResolvedMove(AiMove.Action.PASS, List.of());
        }

        List<Tile> availableTiles = new ArrayList<>();
        for (RackSlot slot : player.getRack().getSlots()) {
            if (!slot.isEmpty()) {
                availableTiles.add(slot.getTile());
            }
        }

        List<ResolvedPlacement> resolvedPlacements = new ArrayList<>(move.placements().size());
        for (AiMove.Placement placement : move.placements()) {
            if (placement.blank() && placement.assignedLetter() == null) {
                throw new IllegalStateException("Blank AI placements must include an assigned letter.");
            }
            Tile selectedTile = selectTile(availableTiles, placement);
            availableTiles.remove(selectedTile);
            resolvedPlacements.add(
                new ResolvedPlacement(
                selectedTile,
                new Position(placement.row(), placement.col()),
                placement.blank() ? placement.assignedLetter() : null));
        }

        return new ResolvedMove(AiMove.Action.PLACE, resolvedPlacements);
    }

    private static Tile selectTile(List<Tile> availableTiles, AiMove.Placement placement) {
        for (Tile tile : availableTiles) {
            if (placement.blank()) {
                if (tile.isBlank()) {
                    return tile;
                }
                continue;
            }

            if (!tile.isBlank() && Character.toUpperCase(tile.getLetter()) == placement.letter()) {
                return tile;
            }
        }

        throw new IllegalStateException(
            "No rack tile matched AI placement letter="
            + placement.letter()
            + " blank="
            + placement.blank());
    }

    /**
     * Stores an AI move after rack tiles are selected.
     *
     * @param action resolved move action
     * @param placements resolved tile placements
     */
    public record ResolvedMove(AiMove.Action action, List<ResolvedPlacement> placements) {

        /**
         * Validates the resolved move.
         */
        public ResolvedMove {
            action = Objects.requireNonNull(action, "action cannot be null.");
            placements = List.copyOf(Objects.requireNonNull(placements, "placements cannot be null."));
        }
    }

    /**
     * Stores one resolved tile placement.
     *
     * @param tile real rack tile
     * @param position target board position
     * @param assignedLetter letter chosen for a blank tile
     */
    public record ResolvedPlacement(Tile tile, Position position, Character assignedLetter) {

        /**
         * Validates and normalizes the resolved placement.
         */
        public ResolvedPlacement {
            tile = Objects.requireNonNull(tile, "tile cannot be null.");
            position = Objects.requireNonNull(position, "position cannot be null.");
            if (assignedLetter != null) {
                assignedLetter = Character.toUpperCase(assignedLetter);
            }
        }
    }
}
