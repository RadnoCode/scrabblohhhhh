package com.kotva.application.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Position;
import org.junit.Test;

public class TurnDraftActionMapperTest {

        @Test
    public void mapsTurnDraftPlacementsIntoDomainPlaceAction() {
        TurnDraft turnDraft = new TurnDraft();
        turnDraft.getPlacements().add(new DraftPlacement("tile-1", new Position(7, 7)));
        turnDraft.getPlacements().add(new DraftPlacement("tile-2", new Position(7, 8)));

        PlayerAction action = TurnDraftActionMapper.toPlaceAction("p1", turnDraft);

        assertEquals("p1", action.playerId());
        assertEquals(2, action.placements().size());
        assertEquals("tile-1", action.placements().get(0).tileId());
        assertEquals(7, action.placements().get(0).position().getRow());
        assertEquals(7, action.placements().get(0).position().getCol());
        assertNull(action.placements().get(0).assignedLetter());
        assertEquals("tile-2", action.placements().get(1).tileId());
        assertEquals(8, action.placements().get(1).position().getCol());
    }

        @Test
    public void mapsAssignedLetterForBlankPlacements() {
        TurnDraft turnDraft = new TurnDraft();
        turnDraft.getPlacements().add(new DraftPlacement("blank-1", new Position(7, 7), 'e'));

        PlayerAction action = TurnDraftActionMapper.toPlaceAction("p1", turnDraft);

        assertEquals(Character.valueOf('E'), action.placements().get(0).assignedLetter());
    }

        @Test
    public void passAndLoseActionsCarryNoPlacements() {
        assertTrue(PlayerAction.pass("p1").placements().isEmpty());
        assertTrue(PlayerAction.lose("p1").placements().isEmpty());
    }
}
