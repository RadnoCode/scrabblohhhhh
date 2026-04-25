package com.kotva.runtime;

import com.kotva.application.service.GameActionResult;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.PlayerConfig;
import com.kotva.application.session.PlayerClockSnapshot;
import com.kotva.application.session.PreviewPositionSnapshot;
import com.kotva.application.session.TutorialGhostTileSnapshot;
import com.kotva.application.session.TutorialSnapshot;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.mode.GameMode;
import com.kotva.mode.PlayerController;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import com.kotva.tutorial.TutorialActionKey;
import com.kotva.tutorial.TutorialScenarioCatalog;
import com.kotva.tutorial.TutorialScenarioCatalog.TutorialAdvanceCondition;
import com.kotva.tutorial.TutorialScenarioCatalog.TutorialCell;
import com.kotva.tutorial.TutorialScenarioCatalog.TutorialExpectedPlacement;
import com.kotva.tutorial.TutorialScenarioCatalog.TutorialGhostTile;
import com.kotva.tutorial.TutorialScenarioCatalog.TutorialPlan;
import com.kotva.tutorial.TutorialScenarioCatalog.TutorialPlacedTile;
import com.kotva.tutorial.TutorialScenarioCatalog.TutorialScenarioDefinition;
import com.kotva.tutorial.TutorialScenarioCatalog.TutorialStepDefinition;
import com.kotva.tutorial.TutorialScriptId;
import com.kotva.tutorial.TutorialUiEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

final class TutorialGameRuntime implements GameRuntime {
    private static final String PLAYER_ID = "tutorial-player-1";
    private static final String PLAYER_NAME = "Player";

    private final TutorialPlan tutorialPlan;
    private final GameApplicationService gameApplicationService;

    private GameSession session;
    private int currentStepIndex;
    private boolean rearrangeUsed;
    private Map<String, Position> expectedPlacementByTileId;
    private Set<String> lockedPresetDraftTileIds;

    TutorialGameRuntime(TutorialScriptId scriptId, GameApplicationService gameApplicationService) {
        this.tutorialPlan = TutorialScenarioCatalog.getPlan(scriptId);
        this.gameApplicationService = Objects.requireNonNull(
            gameApplicationService,
            "gameApplicationService cannot be null.");
        this.expectedPlacementByTileId = new HashMap<>();
        this.lockedPresetDraftTileIds = new HashSet<>();
    }

    @Override
    public void start(NewGameRequest request) {
        currentStepIndex = 0;
        enterStep(currentStepIndex);
    }

    @Override
    public boolean hasSession() {
        return session != null;
    }

    @Override
    public GameSession getSession() {
        return session;
    }

    @Override
    public boolean hasTimeControl() {
        return false;
    }

    @Override
    public boolean isSessionInProgress() {
        return session != null && session.getSessionStatus() == SessionStatus.IN_PROGRESS;
    }

    @Override
    public GameSessionSnapshot getSessionSnapshot() {
        return buildSnapshot();
    }

    @Override
    public GameSessionSnapshot tickClock(long elapsedMillis) {
        return buildSnapshot();
    }

    @Override
    public void placeDraftTile(String tileId, Position position) {
        if (!isBoardEditEnabled()) {
            reject(ActionType.PLACE_TILE, "You cannot place draft tiles during this tutorial step.");
            return;
        }
        if (!isAllowedPlacement(tileId, position)) {
            reject(ActionType.PLACE_TILE, "Please drag the tile to the highlighted position.");
            return;
        }

        gameApplicationService.placeDraftTile(requireSession(), tileId, position);
        maybeAdvanceOnPreviewValid();
    }

    @Override
    public void moveDraftTile(String tileId, Position position) {
        if (!isBoardEditEnabled()) {
            reject(ActionType.PLACE_TILE, "You cannot move draft tiles during this tutorial step.");
            return;
        }
        if (lockedPresetDraftTileIds.contains(tileId)) {
            reject(ActionType.PLACE_TILE, "This preset draft tile cannot be moved.");
            return;
        }
        if (!isAllowedPlacement(tileId, position)) {
            reject(ActionType.PLACE_TILE, "Please drag the tile to the highlighted position.");
            return;
        }

        gameApplicationService.moveDraftTile(requireSession(), tileId, position);
        maybeAdvanceOnPreviewValid();
    }

    @Override
    public void removeDraftTile(String tileId) {
        if (!isBoardEditEnabled()) {
            reject(ActionType.PLACE_TILE, "You cannot remove draft tiles during this tutorial step.");
            return;
        }
        if (lockedPresetDraftTileIds.contains(tileId)) {
            reject(ActionType.PLACE_TILE, "This preset draft tile cannot be removed.");
            return;
        }

        gameApplicationService.removeDraftTile(requireSession(), tileId);
    }

    @Override
    public void recallAllDraftTiles() {
        reject(ActionType.PLACE_TILE, "You cannot recall all draft tiles during this tutorial step.");
    }

    @Override
    public void submitDraft() {
        submitDraft(null);
    }

    @Override
    public void submitDraft(String clientActionId) {
        TutorialStepDefinition step = getCurrentStep();
        if (!resolveEnabledActions(step).contains(TutorialActionKey.SUBMIT)) {
            reject(ActionType.PLACE_TILE, "Please complete the current tutorial requirement first.");
            return;
        }

        GameActionResult result = gameApplicationService.submitDraft(requireSession(), clientActionId);
        if (!result.isSuccess()) {
            return;
        }

        if (step.advanceCondition() == TutorialAdvanceCondition.SUBMIT) {
            advanceToNextStep();
        }
    }

    @Override
    public void passTurn() {
        passTurn(null);
    }

    @Override
    public void passTurn(String clientActionId) {
        reject(ActionType.PASS_TURN, "This stage only introduces Skip, it will not actually execute the skip.");
    }

    @Override
    public void resign() {
        resign(null);
    }

    @Override
    public void resign(String clientActionId) {
        reject(ActionType.LOSE, "This stage only introduces Resign, it will not actually exit the tutorial.");
    }

    @Override
    public boolean isTutorialRuntime() {
        return true;
    }

    @Override
    public void advanceTutorialInstruction() {
        if (getCurrentStep().advanceCondition() == TutorialAdvanceCondition.TAP) {
            advanceToNextStep();
        }
    }

    @Override
    public boolean shouldReturnHomeAfterTutorial() {
        return getCurrentStep().advanceCondition() == TutorialAdvanceCondition.RETURN_HOME;
    }

    @Override
    public void recordTutorialEvent(TutorialUiEvent event) {
        if (event == TutorialUiEvent.REARRANGE_USED && getCurrentStep().stepNumber() == 7) {
            rearrangeUsed = true;
        }
    }

    @Override
    public boolean hasAutomatedTurnSupport() {
        return false;
    }

    @Override
    public boolean isCurrentTurnAutomated() {
        return false;
    }

    @Override
    public void requestAutomatedTurnIfIdle(Consumer<com.kotva.application.service.AiSessionRuntime.TurnCompletion> completionConsumer) {
    }

    @Override
    public boolean matchesAutomatedTurn(com.kotva.application.service.AiSessionRuntime.TurnCompletion completion) {
        return false;
    }

    @Override
    public void applyAutomatedTurn(com.kotva.application.service.AiSessionRuntime.TurnCompletion completion) {
        throw new IllegalStateException("Tutorial runtime does not support automated turns.");
    }

    @Override
    public void cancelPendingAutomatedTurn() {
    }

    @Override
    public void disableAutomatedTurnSupport() {
    }

    @Override
    public void shutdown() {
        session = null;
        expectedPlacementByTileId = new HashMap<>();
        lockedPresetDraftTileIds = new HashSet<>();
        rearrangeUsed = false;
    }

    private void maybeAdvanceOnPreviewValid() {
        TutorialStepDefinition step = getCurrentStep();
        if (step.advanceCondition() != TutorialAdvanceCondition.PREVIEW_VALID) {
            return;
        }

        if (!hasAllExpectedPlacements(step, requireSession())) {
            return;
        }

        if (requireSession().getTurnDraft().getPreviewResult() != null
            && requireSession().getTurnDraft().getPreviewResult().isValid()) {
            advanceToNextStep();
        }
    }

    private boolean hasAllExpectedPlacements(TutorialStepDefinition step, GameSession currentSession) {
        if (step.expectedPlacements().isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Position> entry : expectedPlacementByTileId.entrySet()) {
            boolean matched = currentSession.getTurnDraft().getPlacements().stream()
                .anyMatch(placement ->
                    Objects.equals(placement.getTileId(), entry.getKey())
                    && samePosition(placement.getPosition(), entry.getValue()));
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private boolean isBoardEditEnabled() {
        TutorialStepDefinition step = getCurrentStep();
        return resolveEnabledActions(step).contains(TutorialActionKey.BOARD_EDIT);
    }

    private boolean isAllowedPlacement(String tileId, Position position) {
        Position expectedPosition = expectedPlacementByTileId.get(tileId);
        return expectedPosition != null && samePosition(expectedPosition, position);
    }

    private void enterStep(int stepIndex) {
        TutorialStepDefinition step = tutorialPlan.steps().get(stepIndex);
        if (step.resetScenarioOnEnter() || session == null) {
            buildScenarioSession(step);
        } else {
            expectedPlacementByTileId = resolveExpectedPlacementMap(step, requireSession());
            lockedPresetDraftTileIds = resolveLockedPresetDraftTileIds(requireSession());
        }
        if (step.stepNumber() != 7) {
            rearrangeUsed = false;
        }
    }

    private void advanceToNextStep() {
        if (currentStepIndex >= tutorialPlan.steps().size() - 1) {
            return;
        }
        currentStepIndex++;
        enterStep(currentStepIndex);
    }

    private void buildScenarioSession(TutorialStepDefinition step) {
        TutorialScenarioDefinition scenario = tutorialPlan.scenarios().get(step.scenarioKey());
        Player player = new Player(PLAYER_ID, PLAYER_NAME, PlayerType.LOCAL);
        player.setController(PlayerController.create(PLAYER_ID, PlayerType.LOCAL));
        player.setClock(PlayerClock.disabled());

        com.kotva.domain.model.GameState gameState =
            new com.kotva.domain.model.GameState(List.of(player));
        GameConfig config = new GameConfig(
            GameMode.HOT_SEAT,
            List.of(new PlayerConfig(PLAYER_NAME, PlayerType.LOCAL)),
            DictionaryType.AM,
            null);
        session = new GameSession(
            "tutorial-" + UUID.randomUUID(),
            config,
            gameState);
        session.setSessionStatus(SessionStatus.IN_PROGRESS);

        TileBag tileBag = gameState.getTileBag();
        for (TutorialPlacedTile committedTile : scenario.committedBoardTiles()) {
            Tile tile = tileBag.takeTileByLetter(committedTile.letter());
            gameState.getBoard()
                .getCell(new Position(committedTile.row(), committedTile.col()))
                .setPlacedTile(tile);
        }
        if (!scenario.committedBoardTiles().isEmpty()) {
            gameState.markFirstMoveMade();
        }

        Map<Character, Tile> rackTilesByLetter = new HashMap<>();
        for (int index = 0; index < scenario.rackLetters().size(); index++) {
            char rackLetter = scenario.rackLetters().get(index);
            Tile tile = tileBag.takeTileByLetter(rackLetter);
            player.getRack().setTileAt(index, tile);
            rackTilesByLetter.put(Character.toUpperCase(rackLetter), tile);
        }

        for (TutorialPlacedTile presetDraftTile : scenario.presetDraftTiles()) {
            Tile tile = rackTilesByLetter.get(Character.toUpperCase(presetDraftTile.letter()));
            gameApplicationService.placeDraftTile(
                session,
                tile.getTileID(),
                new Position(presetDraftTile.row(), presetDraftTile.col()));
        }

        expectedPlacementByTileId = resolveExpectedPlacementMap(step, session);
        lockedPresetDraftTileIds = step.lockPresetDraftTiles()
            ? resolveLockedPresetDraftTileIds(session)
            : new HashSet<>();
        rearrangeUsed = false;
    }

    private Map<String, Position> resolveExpectedPlacementMap(
        TutorialStepDefinition step,
        GameSession currentSession) {
        Map<String, Position> placements = new HashMap<>();
        if (step.expectedPlacements().isEmpty()) {
            return placements;
        }

        Player currentPlayer = currentSession.getGameState().requireCurrentActivePlayer();
        Set<String> alreadyLocked = resolveLockedPresetDraftTileIds(currentSession);
        for (TutorialExpectedPlacement expectedPlacement : step.expectedPlacements()) {
            Tile tile = findRackTileByLetter(
                currentPlayer,
                expectedPlacement.letter(),
                placements.keySet(),
                alreadyLocked);
            placements.put(
                tile.getTileID(),
                new Position(expectedPlacement.row(), expectedPlacement.col()));
        }
        return placements;
    }

    private Set<String> resolveLockedPresetDraftTileIds(GameSession currentSession) {
        Set<String> lockedIds = new HashSet<>();
        currentSession.getTurnDraft().getPlacements().forEach(placement -> lockedIds.add(placement.getTileId()));
        return lockedIds;
    }

    private Tile findRackTileByLetter(
        Player player,
        char letter,
        Set<String> alreadyAssigned,
        Set<String> excludedTileIds) {
        char normalized = Character.toUpperCase(letter);
        for (com.kotva.domain.model.RackSlot slot : player.getRack().getSlots()) {
            Tile tile = slot.getTile();
            if (tile == null) {
                continue;
            }
            if (alreadyAssigned.contains(tile.getTileID()) || excludedTileIds.contains(tile.getTileID())) {
                continue;
            }
            if (Character.toUpperCase(tile.getLetter()) == normalized) {
                return tile;
            }
        }
        throw new IllegalStateException("Expected rack tile " + normalized + " was not found.");
    }

    private void reject(ActionType actionType, String message) {
        GameSession currentSession = requireSession();
        currentSession.setLatestActionResult(
            new GameActionResult(
                currentSession.issueActionId(),
                null,
                PLAYER_ID,
                actionType,
                false,
                message,
                0,
                PLAYER_ID,
                false));
    }

    private GameSessionSnapshot buildSnapshot() {
        return gameApplicationService.getSessionSnapshot(requireSession()).withTutorial(buildTutorialSnapshot());
    }

    private TutorialSnapshot buildTutorialSnapshot() {
        TutorialStepDefinition step = getCurrentStep();
        List<TutorialActionKey> enabledActions = resolveEnabledActions(step);
        List<TutorialActionKey> highlightedActions = resolveHighlightedActions(step);
        List<PreviewPositionSnapshot> highlightedBoardPositions = new ArrayList<>();
        for (TutorialCell cell : step.highlightedBoardCells()) {
            highlightedBoardPositions.add(new PreviewPositionSnapshot(cell.row(), cell.col()));
        }

        List<Integer> highlightedRackSlots = step.highlightedRackSlots();
        if (step.stepNumber() == 7 && !rearrangeUsed) {
            highlightedRackSlots = List.of();
        }

        List<TutorialGhostTileSnapshot> ghostTiles = new ArrayList<>();
        for (TutorialGhostTile ghostTile : step.ghostTiles()) {
            ghostTiles.add(
                new TutorialGhostTileSnapshot(
                    ghostTile.row(),
                    ghostTile.col(),
                    String.valueOf(ghostTile.letter()),
                    ghostTile.score()));
        }

        return new TutorialSnapshot(
            tutorialPlan.scriptId(),
            step.stepNumber(),
            step.stepCount(),
            step.title(),
            step.body(),
            step.advanceCondition() == TutorialAdvanceCondition.TAP,
            step.advanceCondition() != TutorialAdvanceCondition.RETURN_HOME,
            step.advanceCondition() == TutorialAdvanceCondition.RETURN_HOME,
            step.dimNonTargetBoardCells(),
            step.dimNonTargetRackSlots(),
            highlightedBoardPositions,
            highlightedRackSlots,
            ghostTiles,
            highlightedActions,
            enabledActions);
    }

    private List<TutorialActionKey> resolveEnabledActions(TutorialStepDefinition step) {
        if (step.stepNumber() == 7 && !rearrangeUsed) {
            return List.of(TutorialActionKey.REARRANGE);
        }
        return step.enabledActions();
    }

    private List<TutorialActionKey> resolveHighlightedActions(TutorialStepDefinition step) {
        if (step.stepNumber() == 7 && rearrangeUsed) {
            return List.of(TutorialActionKey.SUBMIT);
        }
        return step.highlightedActions();
    }

    private TutorialStepDefinition getCurrentStep() {
        return tutorialPlan.steps().get(currentStepIndex);
    }

    private GameSession requireSession() {
        return Objects.requireNonNull(session, "session cannot be null.");
    }

    private boolean samePosition(Position left, Position right) {
        return left != null
            && right != null
            && left.getRow() == right.getRow()
            && left.getCol() == right.getCol();
    }
}
