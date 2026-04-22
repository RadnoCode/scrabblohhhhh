package com.kotva.tutorial;

import java.util.List;
import java.util.Map;

public final class TutorialScenarioCatalog {
    private static final TutorialPlan BASIC_PLAN = buildBasicPlan();

    private TutorialScenarioCatalog() {
    }

    public static TutorialPlan getPlan(TutorialScriptId scriptId) {
        if (scriptId != TutorialScriptId.BASIC_ONBOARDING) {
            throw new IllegalArgumentException("Unsupported tutorial script: " + scriptId);
        }
        return BASIC_PLAN;
    }

    private static TutorialPlan buildBasicPlan() {
        Map<TutorialScenarioKey, TutorialScenarioDefinition> scenarios = Map.of(
            TutorialScenarioKey.BOARD_INTRO,
            new TutorialScenarioDefinition(
                List.of(),
                List.of('A', 'E', 'I', 'O', 'N', 'R', 'S'),
                List.of()),
            TutorialScenarioKey.FIRST_MOVE,
            new TutorialScenarioDefinition(
                List.of(),
                List.of('C', 'A', 'T', 'E', 'N', 'O', 'R'),
                List.of()),
            TutorialScenarioKey.LEGALITY,
            new TutorialScenarioDefinition(
                List.of(new TutorialPlacedTile('A', 7, 7)),
                List.of('A', 'H', 'E', 'N', 'O', 'R', 'S'),
                List.of(new TutorialPlacedTile('H', 7, 8))),
            TutorialScenarioKey.CONNECTED_PLAY,
            new TutorialScenarioDefinition(
                List.of(
                    new TutorialPlacedTile('C', 7, 7),
                    new TutorialPlacedTile('A', 7, 8),
                    new TutorialPlacedTile('T', 7, 9)),
                List.of('S', 'E', 'N', 'O', 'R', 'L', 'I'),
                List.of()),
            TutorialScenarioKey.REARRANGE_PLAY,
            new TutorialScenarioDefinition(
                List.of(new TutorialPlacedTile('O', 7, 7)),
                List.of('G', 'D', 'A', 'E', 'N', 'R', 'S'),
                List.of()));

        List<TutorialStepDefinition> steps = List.of(
            new TutorialStepDefinition(
                1,
                8,
                "Board&Bonus",
                "Colourful cells are bonus cells: TW is triple word score, DW is double word score, TL is triple letter score, DL is double letter score.\nThe first word must also go through the center cell.",
                TutorialScenarioKey.BOARD_INTRO,
                true,
                TutorialAdvanceCondition.TAP,
                true,
                false,
                List.of(
                    new TutorialCell(0, 0),
                    new TutorialCell(0, 3),
                    new TutorialCell(1, 1),
                    new TutorialCell(1, 5),
                    new TutorialCell(7, 7)),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false),
            new TutorialStepDefinition(
                2,
                8,
                "First Move",
                "Drag C, A, and T from your rack to the highlighted positions to form a horizontal word passing through the center cell.",
                TutorialScenarioKey.FIRST_MOVE,
                true,
                TutorialAdvanceCondition.PREVIEW_VALID,
                true,
                true,
                List.of(
                    new TutorialCell(7, 6),
                    new TutorialCell(7, 7),
                    new TutorialCell(7, 8)),
                List.of(0, 1, 2),
                List.of(
                    new TutorialGhostTile('C', 7, 6),
                    new TutorialGhostTile('A', 7, 7),
                    new TutorialGhostTile('T', 7, 8)),
                List.of(),
                List.of(TutorialActionKey.BOARD_EDIT),
                List.of(
                    new TutorialExpectedPlacement('C', 7, 6),
                    new TutorialExpectedPlacement('A', 7, 7),
                    new TutorialExpectedPlacement('T', 7, 8)),
                false),
            new TutorialStepDefinition(
                3,
                8,
                "Validity",
                "Check the Preview section. The current draft is invalid.\nPlace A on the highlighted position, complete AH to the valid word AAH, and observe the Preview color change.",
                TutorialScenarioKey.LEGALITY,
                true,
                TutorialAdvanceCondition.PREVIEW_VALID,
                true,
                true,
                List.of(
                    new TutorialCell(7, 6),
                    new TutorialCell(7, 7),
                    new TutorialCell(7, 8)),
                List.of(0),
                List.of(new TutorialGhostTile('A', 7, 6)),
                List.of(),
                List.of(TutorialActionKey.BOARD_EDIT),
                List.of(new TutorialExpectedPlacement('A', 7, 6)),
                true),
            new TutorialStepDefinition(
                4,
                8,
                "Submit",
                "The current draft is now valid. Click Submit to complete this step.",
                TutorialScenarioKey.LEGALITY,
                false,
                TutorialAdvanceCondition.SUBMIT,
                false,
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(TutorialActionKey.SUBMIT),
                List.of(TutorialActionKey.SUBMIT),
                List.of(),
                true),
            new TutorialStepDefinition(
                5,
                8,
                "Subsequent Placements",
                "Subsequent placements must connect to existing letters. Place S on the highlighted position and click Submit.",
                TutorialScenarioKey.CONNECTED_PLAY,
                true,
                TutorialAdvanceCondition.SUBMIT,
                true,
                true,
                List.of(
                    new TutorialCell(7, 7),
                    new TutorialCell(7, 8),
                    new TutorialCell(7, 9),
                    new TutorialCell(7, 10)),
                List.of(0),
                List.of(new TutorialGhostTile('S', 7, 10)),
                List.of(TutorialActionKey.SUBMIT),
                List.of(TutorialActionKey.BOARD_EDIT, TutorialActionKey.SUBMIT),
                List.of(new TutorialExpectedPlacement('S', 7, 10)),
                false),
            new TutorialStepDefinition(
                6,
                8,
                "Skip&Resign",
                "Skip will skip the current turn; Resign will concede and exit the game.\nThese buttons are only for demonstration in the tutorial and will not actually be executed.",
                TutorialScenarioKey.CONNECTED_PLAY,
                true,
                TutorialAdvanceCondition.TAP,
                false,
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(TutorialActionKey.SKIP, TutorialActionKey.RESIGN),
                List.of(),
                List.of(),
                false),
            new TutorialStepDefinition(
                7,
                8,
                "Rearrange",
                "First, click Rearrange to experience reordering your rack. Then, place D and G on the highlighted positions and click Submit.",
                TutorialScenarioKey.REARRANGE_PLAY,
                true,
                TutorialAdvanceCondition.SUBMIT,
                true,
                true,
                List.of(
                    new TutorialCell(7, 6),
                    new TutorialCell(7, 7),
                    new TutorialCell(7, 8)),
                List.of(0, 1),
                List.of(
                    new TutorialGhostTile('D', 7, 6),
                    new TutorialGhostTile('G', 7, 8)),
                List.of(TutorialActionKey.REARRANGE),
                List.of(TutorialActionKey.REARRANGE, TutorialActionKey.BOARD_EDIT, TutorialActionKey.SUBMIT),
                List.of(
                    new TutorialExpectedPlacement('D', 7, 6),
                    new TutorialExpectedPlacement('G', 7, 8)),
                false),
            new TutorialStepDefinition(
                8,
                8,
                "Complete Tutorial",
                "Tutorial complete. You have learned the basic operations.\nClick Return Home to go back to the main page.",
                TutorialScenarioKey.REARRANGE_PLAY,
                false,
                TutorialAdvanceCondition.RETURN_HOME,
                false,
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false));

        return new TutorialPlan(TutorialScriptId.BASIC_ONBOARDING, steps, scenarios);
    }

    public enum TutorialScenarioKey {
        BOARD_INTRO,
        FIRST_MOVE,
        LEGALITY,
        CONNECTED_PLAY,
        REARRANGE_PLAY,
    }

    public enum TutorialAdvanceCondition {
        TAP,
        PREVIEW_VALID,
        SUBMIT,
        RETURN_HOME,
    }

    public record TutorialPlan(
        TutorialScriptId scriptId,
        List<TutorialStepDefinition> steps,
        Map<TutorialScenarioKey, TutorialScenarioDefinition> scenarios) {
    }

    public record TutorialStepDefinition(
        int stepNumber,
        int stepCount,
        String title,
        String body,
        TutorialScenarioKey scenarioKey,
        boolean resetScenarioOnEnter,
        TutorialAdvanceCondition advanceCondition,
        boolean dimNonTargetBoardCells,
        boolean dimNonTargetRackSlots,
        List<TutorialCell> highlightedBoardCells,
        List<Integer> highlightedRackSlots,
        List<TutorialGhostTile> ghostTiles,
        List<TutorialActionKey> highlightedActions,
        List<TutorialActionKey> enabledActions,
        List<TutorialExpectedPlacement> expectedPlacements,
        boolean lockPresetDraftTiles) {
    }

    public record TutorialScenarioDefinition(
        List<TutorialPlacedTile> committedBoardTiles,
        List<Character> rackLetters,
        List<TutorialPlacedTile> presetDraftTiles) {
    }

    public record TutorialPlacedTile(char letter, int row, int col) {
    }

    public record TutorialExpectedPlacement(char letter, int row, int col) {
    }

    public record TutorialGhostTile(char letter, int row, int col) {
        public int score() {
            return scoreForLetter(letter);
        }
    }

    public record TutorialCell(int row, int col) {
    }

    private static int scoreForLetter(char letter) {
        return switch (Character.toUpperCase(letter)) {
        case 'A', 'E', 'I', 'O', 'U', 'L', 'N', 'S', 'T', 'R' -> 1;
        case 'D', 'G' -> 2;
        case 'B', 'C', 'M', 'P' -> 3;
        case 'F', 'H', 'V', 'W', 'Y' -> 4;
        case 'K' -> 5;
        case 'J', 'X' -> 8;
        case 'Q', 'Z' -> 10;
        default -> 0;
        };
    }
}
