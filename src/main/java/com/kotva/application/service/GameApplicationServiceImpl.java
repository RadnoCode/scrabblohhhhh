package com.kotva.application.service;

import com.kotva.application.draft.DraftManager;
import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraftActionMapper;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.domain.RuleEngine;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.domain.utils.CandidateWord;
import com.kotva.domain.utils.ScoreCalculator;
import com.kotva.domain.utils.WordExtractor;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Objects;

/**
 * Default application service for draft editing, action execution, and clock ticking.
 */
public class GameApplicationServiceImpl implements GameApplicationService {
    private static final String BLANK_TILE_SELECTION_REQUIRED_MESSAGE =
        "Invalid placement. Please hover over the blank tile and choose a letter first.";

    private final ClockService clockService;
    private final DraftManager draftManager;
    private final MovePreviewService movePreviewService;
    private final DictionaryRepository dictionaryRepository;

    /**
     * Creates the service with a default dictionary repository.
     *
     * @param clockService clock service
     */
    public GameApplicationServiceImpl(ClockService clockService) {
        this(clockService, new DictionaryRepository());
    }

    /**
     * Creates the service with explicit clock and dictionary services.
     *
     * @param clockService clock service
     * @param dictionaryRepository dictionary repository
     */
    public GameApplicationServiceImpl(
        ClockService clockService, DictionaryRepository dictionaryRepository) {
        this(clockService, dictionaryRepository, new DraftManager(), null);
    }

    /**
     * Creates the service with custom draft and preview services.
     *
     * @param clockService clock service
     * @param draftManager draft manager
     * @param movePreviewService preview service
     */
    public GameApplicationServiceImpl(
        ClockService clockService,
        DraftManager draftManager,
        MovePreviewService movePreviewService) {
        this(clockService, new DictionaryRepository(), draftManager, movePreviewService);
    }

    /**
     * Creates the full service implementation.
     *
     * @param clockService clock service
     * @param dictionaryRepository dictionary repository
     * @param draftManager draft manager
     * @param movePreviewService preview service, or {@code null} for default
     */
    private GameApplicationServiceImpl(
        ClockService clockService,
        DictionaryRepository dictionaryRepository,
        DraftManager draftManager,
        MovePreviewService movePreviewService) {
        this.clockService = Objects.requireNonNull(clockService, "clockService cannot be null.");
        this.dictionaryRepository = Objects.requireNonNull(dictionaryRepository,
            "dictionaryRepository cannot be null.");
        this.draftManager = Objects.requireNonNull(draftManager, "draftManager cannot be null.");
        this.movePreviewService = movePreviewService != null
        ? movePreviewService
        : new MovePreviewServiceImpl(this.dictionaryRepository);
    }

    /**
     * Places a tile in the current draft and refreshes the preview.
     *
     * @param session game session
     * @param tileId tile id
     * @param position board position
     * @return refreshed preview
     */
    @Override
    public PreviewResult placeDraftTile(GameSession session, String tileId, Position position) {
        ensureEditingAllowed(session);
        draftManager.placeTile(session.getTurnDraft(), tileId, position);
        return refreshPreview(session);
    }

    /**
     * Moves a tile already in the current draft.
     *
     * @param session game session
     * @param tileId tile id
     * @param newPosition new board position
     * @return refreshed preview
     */
    @Override
    public PreviewResult moveDraftTile(GameSession session, String tileId, Position newPosition) {
        ensureEditingAllowed(session);
        draftManager.moveTile(session.getTurnDraft(), tileId, newPosition);
        return refreshPreview(session);
    }

    /**
     * Removes a tile from the current draft.
     *
     * @param session game session
     * @param tileId tile id
     * @return refreshed preview
     */
    @Override
    public PreviewResult removeDraftTile(GameSession session, String tileId) {
        ensureEditingAllowed(session);
        draftManager.removeTile(session.getTurnDraft(), tileId);
        return refreshPreview(session);
    }

    /**
     * Assigns a chosen letter to a blank tile.
     *
     * @param session game session
     * @param tileId blank tile id
     * @param assignedLetter selected letter
     */
    @Override
    public void assignLettertoBlank(GameSession session, String tileId, char assignedLetter) {
        ensureEditingAllowed(session);
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        Tile tile = session.getGameState().getTileBag().getTileById(tileId);
        if (tile == null) {
            throw new IllegalArgumentException("Unknown tileId: " + tileId);
        }
        if (!tile.isBlank()) {
            throw new IllegalArgumentException("Tile is not a blank tile: " + tileId);
        }

        char normalizedLetter = Character.toUpperCase(assignedLetter);
        if (normalizedLetter < 'A' || normalizedLetter > 'Z') {
            throw new IllegalArgumentException("Assigned letter must be between A and Z.");
        }

        tile.setAssignedLetter(normalizedLetter);
        session.getTurnDraft().getAssignedLettersByTileId().put(tileId, normalizedLetter);
        DraftPlacement placement = draftManager.findPlacementByTileId(session.getTurnDraft(), tileId);
        if (placement != null) {
            placement.setAssignedLetter(normalizedLetter);
        }
        if (session.getTurnDraft().getPlacements().isEmpty()) {
            session.getTurnDraft().setPreviewResult(null);
            return;
        }
        refreshPreview(session);
    }

    /**
     * Recalls all tiles from the current draft.
     *
     * @param session game session
     * @return refreshed preview
     */
    @Override
    public PreviewResult recallAllDraftTiles(GameSession session) {
        ensureEditingAllowed(session);
        draftManager.recallAllTiles(session.getTurnDraft());
        return refreshPreview(session);
    }

    /**
     * Submits the current draft.
     *
     * @param session game session
     * @return action result
     */
    @Override
    public GameActionResult submitDraft(GameSession session) {
        return submitDraft(session, null);
    }

    /**
     * Submits the current draft with a client action id.
     *
     * @param session game session
     * @param clientActionId client action id
     * @return action result
     */
    @Override
    public GameActionResult submitDraft(GameSession session, String clientActionId) {
        Player currentPlayer = requireCurrentPlayer(session);
        PlayerAction action =
        TurnDraftActionMapper.toPlaceAction(currentPlayer.getPlayerId(), session.getTurnDraft());
        return executeAction(session, action, clientActionId);
    }

    /**
     * Passes the current player's turn.
     *
     * @param session game session
     * @return action result
     */
    @Override
    public GameActionResult passTurn(GameSession session) {
        return passTurn(session, null);
    }

    /**
     * Passes the current player's turn with a client action id.
     *
     * @param session game session
     * @param clientActionId client action id
     * @return action result
     */
    @Override
    public GameActionResult passTurn(GameSession session, String clientActionId) {
        Player currentPlayer = requireCurrentPlayer(session);
        return executeAction(session, PlayerAction.pass(currentPlayer.getPlayerId()), clientActionId);
    }

    /**
     * Resigns the current player.
     *
     * @param session game session
     * @return action result
     */
    @Override
    public GameActionResult resign(GameSession session) {
        return resign(session, null);
    }

    /**
     * Resigns the current player with a client action id.
     *
     * @param session game session
     * @param clientActionId client action id
     * @return action result
     */
    @Override
    public GameActionResult resign(GameSession session, String clientActionId) {
        Player currentPlayer = requireCurrentPlayer(session);
        return executeAction(
            session,
            PlayerAction.lose(currentPlayer.getPlayerId()),
            clientActionId,
            "Player resigned.");
    }

    /**
     * Confirms a local hot-seat handoff.
     *
     * @param session game session
     */
    @Override
    public void confirmHotSeatHandoff(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
    }

    /**
     * Advances the clock and returns the current snapshot.
     *
     * @param session game session
     * @param elapsedMillis elapsed time in milliseconds
     * @return session snapshot
     */
    @Override
    public GameSessionSnapshot tickClock(GameSession session, long elapsedMillis) {
        Objects.requireNonNull(session, "session cannot be null.");
        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            return getSessionSnapshot(session);
        }

        clockService.tick(session, elapsedMillis);
        handleTimeoutIfNeeded(session);
        return getSessionSnapshot(session);
    }

    /**
     * Builds a snapshot of the current session.
     *
     * @param session game session
     * @return session snapshot
     */
    @Override
    public GameSessionSnapshot getSessionSnapshot(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        return GameSessionSnapshotFactory.fromSession(session);
    }

    /**
     * Refreshes and stores the preview result for the current draft.
     *
     * @param session game session
     * @return refreshed preview result
     */
    private PreviewResult refreshPreview(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        ensureDictionaryLoaded(session);

        PreviewResult previewResult = movePreviewService.preview(session);
        if (previewResult == null) {
            throw new IllegalStateException("movePreviewService returned null preview result.");
        }

        session.getTurnDraft().setPreviewResult(previewResult);
        return previewResult;
    }

    /**
     * Executes an action with the default lose message.
     *
     * @param session game session
     * @param action action to execute
     * @param clientActionId client action id
     * @return action result
     */
    private GameActionResult executeAction(
        GameSession session, PlayerAction action, String clientActionId) {
        return executeAction(session, action, clientActionId, "Player resigned.");
    }

    /**
     * Executes a player action and stores the latest result.
     *
     * @param session game session
     * @param action action to execute
     * @param clientActionId client action id
     * @param loseMessage message used for lose actions
     * @return action result
     */
    private GameActionResult executeAction(
        GameSession session,
        PlayerAction action,
        String clientActionId,
        String loseMessage) {
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(action, "action cannot be null.");
        ensureSessionInProgress(session);

        Player currentPlayer = requireCurrentPlayer(session);
        validateActionOwner(currentPlayer, action);
        String actionId = session.issueActionId();

        GameActionResult result =
        switch (action.type()) {
        case PLACE_TILE ->
            executePlace(session, currentPlayer, action, actionId, clientActionId);
        case PASS_TURN ->
            executePass(session, currentPlayer, action, actionId, clientActionId);
        case LOSE ->
            executeLose(
                session,
                currentPlayer,
                action,
                actionId,
                clientActionId,
                loseMessage);
        };
        session.setLatestActionResult(result);
        return result;
    }

    /**
     * Executes a tile-placement action.
     *
     * @param session game session
     * @param currentPlayer current player
     * @param action placement action
     * @param actionId generated action id
     * @param clientActionId client action id
     * @return action result
     */
    private GameActionResult executePlace(
        GameSession session,
        Player currentPlayer,
        PlayerAction action,
        String actionId,
        String clientActionId) {
        if (hasUnassignedBlankTile(session, action)) {
            return failureResult(
                actionId,
                clientActionId,
                action,
                BLANK_TILE_SELECTION_REQUIRED_MESSAGE,
                currentPlayer.getPlayerId());
        }
        ensureDictionaryLoaded(session);
        RuleEngine ruleEngine = new RuleEngine(dictionaryRepository);
        String validationMessage = ruleEngine.validateMove(session.getGameState(), action);
        if (validationMessage != null) {
            return failureResult(
                actionId,
                clientActionId,
                action,
                validationMessage,
                currentPlayer.getPlayerId());
        }

        List<CandidateWord> words = WordExtractor.extract(
            action, session.getGameState().getTileBag(), session.getGameState().getBoard());
        int awardedScore = ScoreCalculator.calculate(words, session.getGameState(), action);
        ruleEngine.apply(session.getGameState(), action);
        currentPlayer.addScore(awardedScore);
        refillRack(currentPlayer, session.getGameState().getTileBag());
        clearRackBlankAssignments(currentPlayer);
        session.resetTurnDraft();
        clockService.stopTurnClock(session);

        session.getTurnCoordinator().onActionApplied(action);
        return completeTransition(
            session, actionId, clientActionId, action, awardedScore, "Draft submitted.");
    }

    /**
     * Executes a pass action.
     *
     * @param session game session
     * @param currentPlayer current player
     * @param action pass action
     * @param actionId generated action id
     * @param clientActionId client action id
     * @return action result
     */
    private GameActionResult executePass(
        GameSession session,
        Player currentPlayer,
        PlayerAction action,
        String actionId,
        String clientActionId) {
        RuleEngine ruleEngine = new RuleEngine(dictionaryRepository);
        ruleEngine.apply(session.getGameState(), action);
        clearRackBlankAssignments(currentPlayer);
        session.resetTurnDraft();
        clockService.stopTurnClock(session);

        session.getTurnCoordinator().onActionApplied(action);
        return completeTransition(session, actionId, clientActionId, action, 0, "Turn passed.");
    }

    /**
     * Executes a lose or resign action.
     *
     * @param session game session
     * @param currentPlayer current player
     * @param action lose action
     * @param actionId generated action id
     * @param clientActionId client action id
     * @param message result message
     * @return action result
     */
    private GameActionResult executeLose(
        GameSession session,
        Player currentPlayer,
        PlayerAction action,
        String actionId,
        String clientActionId,
        String message) {
        RuleEngine ruleEngine = new RuleEngine(dictionaryRepository);
        ruleEngine.apply(session.getGameState(), action);
        clearRackBlankAssignments(currentPlayer);
        session.resetTurnDraft();
        clockService.stopTurnClock(session);

        session.getTurnCoordinator().onActionApplied(action);
        return completeTransition(
            session,
            actionId,
            clientActionId,
            action,
            0,
            message);
    }

    /**
     * Executes a command received from a remote client.
     *
     * @param session game session
     * @param action remote action
     * @return action result
     */
    public GameActionResult executeRemoteCommand(GameSession session, PlayerAction action) {
        return executeRemoteCommand(session, action, null);
    }

    /**
     * Executes a command received from a remote client with its client id.
     *
     * @param session game session
     * @param action remote action
     * @param clientActionId client action id
     * @return action result
     */
    public GameActionResult executeRemoteCommand(
        GameSession session, PlayerAction action, String clientActionId) {
        return executeAction(session, action, clientActionId);
    }

    /**
     * Completes turn transition after an action has been applied.
     *
     * @param session game session
     * @param actionId generated action id
     * @param clientActionId client action id
     * @param action executed action
     * @param awardedScore awarded score
     * @param message result message
     * @return action result
     */
    private GameActionResult completeTransition(
        GameSession session,
        String actionId,
        String clientActionId,
        PlayerAction action,
        int awardedScore,
        String message) {
        if (session.getTurnCoordinator().isGameEnded()) {
            session.setSessionStatus(SessionStatus.COMPLETED);
            return successResult(
                actionId,
                clientActionId,
                action,
                message,
                awardedScore,
                null,
                true);
        }

        clockService.startTurnClock(session);
        return successResult(
            actionId,
            clientActionId,
            action,
            message,
            awardedScore,
            requireCurrentPlayer(session).getPlayerId(),
            false);
    }

    /**
     * Converts a timeout clock phase into a lose action.
     *
     * @param session game session
     */
    private void handleTimeoutIfNeeded(GameSession session) {
        Player currentPlayer = session.getGameState().getCurrentPlayer();
        if (!currentPlayer.getActive()) {
            return;
        }

        if (currentPlayer.getClock().getPhase() == ClockPhase.TIMEOUT) {
            executeAction(
                session,
                PlayerAction.lose(currentPlayer.getPlayerId()),
                null,
                "Player timed out.");
        }
    }

    /**
     * Ensures draft editing is currently allowed.
     *
     * @param session game session
     */
    private void ensureEditingAllowed(GameSession session) {
        ensureSessionInProgress(session);
    }

    /**
     * Ensures the session is in progress.
     *
     * @param session game session
     */
    private void ensureSessionInProgress(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game session is not in progress.");
        }
    }

    /**
     * Loads the session dictionary if it is not already loaded.
     *
     * @param session game session
     */
    private void ensureDictionaryLoaded(GameSession session) {
        if (dictionaryRepository.getLoadedDictionaryType() != session.getConfig().getDictionaryType()) {
            dictionaryRepository.loadDictionary(session.getConfig().getDictionaryType());
        }
    }

    /**
     * Gets the current active player.
     *
     * @param session game session
     * @return current player
     */
    private Player requireCurrentPlayer(GameSession session) {
        return session.getGameState().requireCurrentActivePlayer();
    }

    /**
     * Checks that an action belongs to the current player.
     *
     * @param currentPlayer current player
     * @param action action to validate
     */
    private void validateActionOwner(Player currentPlayer, PlayerAction action) {
        if (!Objects.equals(currentPlayer.getPlayerId(), action.playerId())) {
            throw new IllegalArgumentException(
                "Action playerId="
                + action.playerId()
                + " does not match current playerId="
                + currentPlayer.getPlayerId());
        }
    }

    /**
     * Refills empty rack slots from the tile bag.
     *
     * @param player player whose rack is refilled
     * @param tileBag tile bag to draw from
     */
    private void refillRack(Player player, TileBag tileBag) {
        for (RackSlot slot : player.getRack().getSlots()) {
            if (!slot.isEmpty() || tileBag.isEmpty()) {
                continue;
            }

            Tile drawnTile = tileBag.drawTile();
            if (drawnTile == null) {
                return;
            }
            player.getRack().setTileAt(slot.getIndex(), drawnTile);
        }
    }

    /**
     * Clears assigned letters from blank tiles left in the rack.
     *
     * @param player player whose rack is cleaned
     */
    private void clearRackBlankAssignments(Player player) {
        for (RackSlot slot : player.getRack().getSlots()) {
            if (slot.isEmpty()) {
                continue;
            }

            Tile tile = slot.getTile();
            if (tile.isBlank()) {
                tile.clearAssignedLetter();
            }
        }
    }

    /**
     * Checks whether a submitted action contains an unassigned blank tile.
     *
     * @param session game session
     * @param action action to inspect
     * @return {@code true} if a blank tile has no assigned letter
     */
    private boolean hasUnassignedBlankTile(GameSession session, PlayerAction action) {
        for (var placement : action.placements()) {
            Tile tile = session.getGameState().getTileBag().getTileById(placement.tileId());
            if (tile != null
                && tile.isBlank()
                && tile.getAssignedLetter() == null
                && placement.assignedLetter() == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a failed action result.
     *
     * @param actionId generated action id
     * @param clientActionId client action id
     * @param action attempted action
     * @param message failure message
     * @param nextPlayerId next player id
     * @return failed result
     */
    private static GameActionResult failureResult(
        String actionId,
        String clientActionId,
        PlayerAction action,
        String message,
        String nextPlayerId) {
        return new GameActionResult(
            actionId,
            clientActionId,
            action.playerId(),
            action.type(),
            false,
            message,
            0,
            nextPlayerId,
            false);
    }

    /**
     * Builds a successful action result.
     *
     * @param actionId generated action id
     * @param clientActionId client action id
     * @param action executed action
     * @param message success message
     * @param awardedScore awarded score
     * @param nextPlayerId next player id
     * @param gameEnded whether the game ended
     * @return successful result
     */
    private static GameActionResult successResult(
        String actionId,
        String clientActionId,
        PlayerAction action,
        String message,
        int awardedScore,
        String nextPlayerId,
        boolean gameEnded) {
        return new GameActionResult(
            actionId,
            clientActionId,
            action.playerId(),
            action.type(),
            true,
            message,
            awardedScore,
            nextPlayerId,
            gameEnded);
    }
}
