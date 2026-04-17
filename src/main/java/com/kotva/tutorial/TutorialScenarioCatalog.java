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
                List.of('B', 'C', 'E', 'N', 'O', 'R', 'S'),
                List.of(new TutorialPlacedTile('B', 7, 8))),
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
                "盘面介绍",
                "彩色奖励格会提高得分：TW 是三倍词分，DW 是双倍词分，TL 是三倍字母分，DL 是双倍字母分。\n首个单词还必须经过中心格。",
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
                "首次落子",
                "把 rack 里的 C、A、T 拖到提示位置，组成一条直线，并让单词经过中心格。",
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
                "合法性",
                "看右侧 Preview。现在草稿是不合法的。\n把 C 放到提示位置，把 AB 补成合法单词 CAB，观察 Preview 颜色变化。",
                TutorialScenarioKey.LEGALITY,
                true,
                TutorialAdvanceCondition.PREVIEW_VALID,
                true,
                true,
                List.of(
                    new TutorialCell(7, 6),
                    new TutorialCell(7, 7),
                    new TutorialCell(7, 8)),
                List.of(1),
                List.of(new TutorialGhostTile('C', 7, 6)),
                List.of(),
                List.of(TutorialActionKey.BOARD_EDIT),
                List.of(new TutorialExpectedPlacement('C', 7, 6)),
                true),
            new TutorialStepDefinition(
                4,
                8,
                "提交",
                "现在草稿已经合法。点击 Submit 提交这一步。",
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
                "后续落子",
                "后续落子必须连接已有字母。把 S 放到提示位置，并点击 Submit。",
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
                "Skip 与 Resign",
                "Skip 会跳过当前回合；Resign 会认输退出对局。\n教程里这两个按钮只用于介绍，不会真的执行。",
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
                "先点击 Rearrange 体验重排序 rack，然后把 D 和 G 放到提示位置，并点击 Submit。",
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
                "完成教程",
                "教程完成。你已经学会了基础操作。\n点击 Return Home 返回主页。",
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
