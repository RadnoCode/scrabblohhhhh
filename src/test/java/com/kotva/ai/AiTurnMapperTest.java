package com.kotva.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.kotva.domain.model.Player;
import com.kotva.domain.model.Tile;
import com.kotva.policy.PlayerType;
import java.util.List;
import org.junit.Test;

public class AiTurnMapperTest {

        @Test
    public void resolveSelectsRackTilesAndPreservesBlankAssignment() {
        Player player = new Player("ai-1", "AI", PlayerType.AI);
        Tile blank = new Tile("blank", ' ', 0, true);
        Tile letterA = new Tile("tile-a", 'A', 1, false);
        Tile letterT = new Tile("tile-t", 'T', 1, false);
        player.getRack().setTileAt(0, blank);
        player.getRack().setTileAt(1, letterA);
        player.getRack().setTileAt(2, letterT);

        AiMove move = new AiMove(
            AiMove.Action.PLACE,
            List.of(
            new AiMove.Placement(7, 7, 'A', false, null),
            new AiMove.Placement(7, 8, 'E', true, 'E'),
            new AiMove.Placement(7, 9, 'T', false, null)),
            22,
            0.0,
            0.0);

        AiTurnMapper.ResolvedMove resolvedMove = AiTurnMapper.resolve(player, move);

        assertEquals(AiMove.Action.PLACE, resolvedMove.action());
        assertEquals(3, resolvedMove.placements().size());
        assertEquals("tile-a", resolvedMove.placements().get(0).tile().getTileID());
        assertNull(resolvedMove.placements().get(0).assignedLetter());
        assertEquals("blank", resolvedMove.placements().get(1).tile().getTileID());
        assertEquals(Character.valueOf('E'), resolvedMove.placements().get(1).assignedLetter());
        assertEquals("tile-t", resolvedMove.placements().get(2).tile().getTileID());
    }

        @Test
    public void resolveUsesBlankPlacementLetterAsDefaultAssignment() {
        Player player = new Player("ai-1", "AI", PlayerType.AI);
        player.getRack().setTileAt(0, new Tile("blank", ' ', 0, true));

        AiMove move = new AiMove(
            AiMove.Action.PLACE,
            List.of(new AiMove.Placement(7, 7, 'E', true, null)),
            0,
            0.0,
            0.0);

        AiTurnMapper.ResolvedMove resolvedMove = AiTurnMapper.resolve(player, move);

        assertEquals(1, resolvedMove.placements().size());
        assertEquals(Character.valueOf('E'), resolvedMove.placements().get(0).assignedLetter());
    }
}