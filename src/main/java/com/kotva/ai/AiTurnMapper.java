package com.kotva.ai;

import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AiTurnMapper {
    private AiTurnMapper() {
    }

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

    public record ResolvedMove(AiMove.Action action, List<ResolvedPlacement> placements) {
        public ResolvedMove {
            action = Objects.requireNonNull(action, "action cannot be null.");
            placements = List.copyOf(Objects.requireNonNull(placements, "placements cannot be null."));
        }
    }

    public record ResolvedPlacement(Tile tile, Position position, Character assignedLetter) {
        public ResolvedPlacement {
            tile = Objects.requireNonNull(tile, "tile cannot be null.");
            position = Objects.requireNonNull(position, "position cannot be null.");
            if (assignedLetter != null) {
                assignedLetter = Character.toUpperCase(assignedLetter);
            }
        }
    }
}
