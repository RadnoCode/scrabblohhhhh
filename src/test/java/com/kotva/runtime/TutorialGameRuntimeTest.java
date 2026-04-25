package com.kotva.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.policy.DictionaryType;
import com.kotva.tutorial.TutorialScriptId;
import com.kotva.tutorial.TutorialUiEvent;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class TutorialGameRuntimeTest {

    @Test
    public void tutorialRuntimeStartsOnBoardIntroAndAdvancesToFirstMove() {
        TutorialGameRuntime runtime = createRuntime();

        runtime.start(null);
        GameSessionSnapshot firstSnapshot = runtime.getSessionSnapshot();
        assertNotNull(firstSnapshot.getTutorial());
        assertEquals(1, firstSnapshot.getTutorial().getStepNumber());
        assertTrue(firstSnapshot.getTutorial().isTapToContinue());

        runtime.advanceTutorialInstruction();
        GameSessionSnapshot secondSnapshot = runtime.getSessionSnapshot();
        assertEquals(2, secondSnapshot.getTutorial().getStepNumber());
        assertFalse(secondSnapshot.getTutorial().isTapToContinue());
    }

    @Test
    public void tutorialRuntimeRejectsWrongFirstMovePlacementAndAcceptsCorrectSequence() {
        TutorialGameRuntime runtime = createRuntime();
        runtime.start(null);
        runtime.advanceTutorialInstruction();

        Tile tileC = findRackTile(runtime, 'C');
        runtime.placeDraftTile(tileC.getTileID(), new Position(0, 0));

        assertFalse(runtime.getSession().getLatestActionResult().isSuccess());
        assertTrue(runtime.getSession().getLatestActionResult().getMessage().contains("highlighted position"));

        runtime.placeDraftTile(tileC.getTileID(), new Position(7, 6));
        runtime.placeDraftTile(findRackTile(runtime, 'A').getTileID(), new Position(7, 7));
        runtime.placeDraftTile(findRackTile(runtime, 'T').getTileID(), new Position(7, 8));

        GameSessionSnapshot snapshot = runtime.getSessionSnapshot();
        assertEquals(3, snapshot.getTutorial().getStepNumber());
        assertFalse(snapshot.getPreview().isValid());
    }

    @Test
    public void tutorialRuntimeDoesNotAdvanceFirstMoveUntilCatIsComplete() {
        TutorialGameRuntime runtime = createRuntime();
        runtime.start(null);
        runtime.advanceTutorialInstruction();

        runtime.placeDraftTile(findRackTile(runtime, 'C').getTileID(), new Position(7, 6));
        runtime.placeDraftTile(findRackTile(runtime, 'A').getTileID(), new Position(7, 7));

        assertEquals(2, runtime.getSessionSnapshot().getTutorial().getStepNumber());

        runtime.placeDraftTile(findRackTile(runtime, 'T').getTileID(), new Position(7, 8));

        assertEquals(3, runtime.getSessionSnapshot().getTutorial().getStepNumber());
    }

    @Test
    public void tutorialRuntimeAllowsRemovingDraftTilesDuringBoardEditSteps() {
        TutorialGameRuntime runtime = createRuntime();
        runtime.start(null);
        runtime.advanceTutorialInstruction();

        Tile tileC = findRackTile(runtime, 'C');
        runtime.placeDraftTile(tileC.getTileID(), new Position(7, 6));
        assertEquals(1, runtime.getSession().getTurnDraft().getPlacements().size());

        runtime.removeDraftTile(tileC.getTileID());

        assertTrue(runtime.getSession().getTurnDraft().getPlacements().isEmpty());
        assertEquals(2, runtime.getSessionSnapshot().getTutorial().getStepNumber());
    }

    @Test
    public void tutorialRuntimeShowsInvalidAhExampleBeforePlacingFinalA() {
        TutorialGameRuntime runtime = createRuntime();
        advanceToLegalityStep(runtime);

        GameSessionSnapshot snapshot = runtime.getSessionSnapshot();
        assertEquals(3, snapshot.getTutorial().getStepNumber());
        assertFalse(snapshot.getPreview().isValid());
        assertEquals("AH", snapshot.getPreview().getWords().get(0).getWord());
        assertEquals(List.of(0), snapshot.getTutorial().getHighlightedRackSlots());
        assertEquals("A", snapshot.getTutorial().getGhostTiles().get(0).getLetter());
    }

    @Test
    public void tutorialRuntimeCarriesValidPreviewIntoSubmitStep() {
        TutorialGameRuntime runtime = createRuntime();
        advanceToLegalityStep(runtime);

        runtime.placeDraftTile(findRackTile(runtime, 'A').getTileID(), new Position(7, 6));

        GameSessionSnapshot previewValidSnapshot = runtime.getSessionSnapshot();
        assertEquals(4, previewValidSnapshot.getTutorial().getStepNumber());
        assertTrue(previewValidSnapshot.getPreview().isValid());
        assertEquals("AAH", previewValidSnapshot.getPreview().getWords().get(0).getWord());

        runtime.submitDraft();

        GameSessionSnapshot nextSnapshot = runtime.getSessionSnapshot();
        assertEquals(5, nextSnapshot.getTutorial().getStepNumber());
    }

    @Test
    public void tutorialRuntimeRequiresRearrangeBeforeFinalMoveAndEndsOnCompletionScreen() {
        TutorialGameRuntime runtime = createRuntime();
        advanceToRearrangeStep(runtime);

        runtime.placeDraftTile(findRackTile(runtime, 'D').getTileID(), new Position(7, 6));
        assertFalse(runtime.getSession().getLatestActionResult().isSuccess());
        assertTrue(runtime.getSession().getLatestActionResult().getMessage().contains("tutorial step"));

        runtime.recordTutorialEvent(TutorialUiEvent.REARRANGE_USED);
        runtime.placeDraftTile(findRackTile(runtime, 'D').getTileID(), new Position(7, 6));
        runtime.placeDraftTile(findRackTile(runtime, 'G').getTileID(), new Position(7, 8));
        runtime.submitDraft();

        GameSessionSnapshot finalSnapshot = runtime.getSessionSnapshot();
        assertEquals(8, finalSnapshot.getTutorial().getStepNumber());
        assertTrue(finalSnapshot.getTutorial().isShowReturnHomeButton());
        assertFalse(finalSnapshot.getTutorial().isShowExitButton());
    }

    private void advanceToLegalityStep(TutorialGameRuntime runtime) {
        runtime.start(null);
        runtime.advanceTutorialInstruction();
        runtime.placeDraftTile(findRackTile(runtime, 'C').getTileID(), new Position(7, 6));
        runtime.placeDraftTile(findRackTile(runtime, 'A').getTileID(), new Position(7, 7));
        runtime.placeDraftTile(findRackTile(runtime, 'T').getTileID(), new Position(7, 8));
        assertEquals(3, runtime.getSessionSnapshot().getTutorial().getStepNumber());
    }

    private void advanceToRearrangeStep(TutorialGameRuntime runtime) {
        advanceToLegalityStep(runtime);
        runtime.placeDraftTile(findRackTile(runtime, 'A').getTileID(), new Position(7, 6));
        runtime.submitDraft();
        runtime.placeDraftTile(findRackTile(runtime, 'S').getTileID(), new Position(7, 10));
        runtime.submitDraft();
        runtime.advanceTutorialInstruction();
        assertEquals(7, runtime.getSessionSnapshot().getTutorial().getStepNumber());
    }

    private Tile findRackTile(TutorialGameRuntime runtime, char letter) {
        Player player = runtime.getSession().getGameState().requireCurrentActivePlayer();
        for (RackSlot slot : player.getRack().getSlots()) {
            Tile tile = slot.getTile();
            if (tile != null && Character.toUpperCase(tile.getLetter()) == Character.toUpperCase(letter)) {
                return tile;
            }
        }
        throw new AssertionError("Expected tile " + letter + " to be present on the rack.");
    }

    private TutorialGameRuntime createRuntime() {
        return new TutorialGameRuntime(
            TutorialScriptId.BASIC_ONBOARDING,
            new GameApplicationServiceImpl(new ClockServiceImpl(), new TutorialDictionaryRepository()));
    }

    private static class TutorialDictionaryRepository extends DictionaryRepository {

        @Override
        public void loadDictionary(DictionaryType dictionaryType) {
        }

        @Override
        public Set<String> getDictionary() {
            return Set.of("AAH", "CAT", "CATS", "DOG");
        }

        @Override
        public DictionaryType getLoadedDictionaryType() {
            return DictionaryType.AM;
        }

        @Override
        public boolean isAccepted(String word) {
            return getDictionary().contains(word.toUpperCase());
        }
    }
}
