package com.kotva.application.service;

import com.kotva.application.draft.DraftManager;
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

public class GameApplicationServiceImpl implements GameApplicationService {
    private final ClockService clockService;
    private final DraftManager draftManager;
    private final MovePreviewService movePreviewService;
    private final DictionaryRepository dictionaryRepository;

    public GameApplicationServiceImpl(ClockService clockService) {
        this(clockService, new DictionaryRepository());
    }

    public GameApplicationServiceImpl(
        ClockService clockService, DictionaryRepository dictionaryRepository) {
        this(clockService, dictionaryRepository, new DraftManager(), null);
    }

    public GameApplicationServiceImpl(
        ClockService clockService,
        DraftManager draftManager,
        MovePreviewService movePreviewService) {
        this(clockService, new DictionaryRepository(), draftManager, movePreviewService);
    }

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

        @Override
    public PreviewResult placeDraftTile(GameSession session, String tileId, Position position) {
        ensureEditingAllowed(session);
        draftManager.placeTile(session.getTurnDraft(), tileId, position);
        return refreshPreview(session);
    }

        @Override
    public PreviewResult moveDraftTile(GameSession session, String tileId, Position newPosition) {
        ensureEditingAllowed(session);
        draftManager.moveTile(session.getTurnDraft(), tileId, newPosition);
        return refreshPreview(session);
    }

        @Override
    public PreviewResult removeDraftTile(GameSession session, String tileId) {
        ensureEditingAllowed(session);
        draftManager.removeTile(session.getTurnDraft(), tileId);
        return refreshPreview(session);
    }
        @Override
    public void assignLettertoBlank(GameSession session, String tileId, char assignedLetter) {
        ensureEditingAllowed(session);
        Tile tile =session.getGameState().getTileBag().getTileById(tileId);
        tile.setAssignedLetter(assignedLetter);
    }

        @Override
    public PreviewResult recallAllDraftTiles(GameSession session) {
        ensureEditingAllowed(session);
        draftManager.recallAllTiles(session.getTurnDraft());
        return refreshPreview(session);
    }

        @Override
    public GameActionResult submitDraft(GameSession session) {
        return submitDraft(session, null);
    }

        @Override
    public GameActionResult submitDraft(GameSession session, String clientActionId) {
        Player currentPlayer = requireCurrentPlayer(session);
        PlayerAction action =
        TurnDraftActionMapper.toPlaceAction(currentPlayer.getPlayerId(), session.getTurnDraft());
        return executeAction(session, action, clientActionId);
    }

        @Override
    public GameActionResult passTurn(GameSession session) {
        return passTurn(session, null);
    }

        @Override
    public GameActionResult passTurn(GameSession session, String clientActionId) {
        Player currentPlayer = requireCurrentPlayer(session);
        return executeAction(session, PlayerAction.pass(currentPlayer.getPlayerId()), clientActionId);
    }

        @Override
    public GameActionResult resign(GameSession session) {
        return resign(session, null);
    }

        @Override
    public GameActionResult resign(GameSession session, String clientActionId) {
        Player currentPlayer = requireCurrentPlayer(session);
        return executeAction(
            session,
            PlayerAction.lose(currentPlayer.getPlayerId()),
            clientActionId,
            "Player resigned.");
    }

        @Override
    public void confirmHotSeatHandoff(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
    }

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

        @Override
    public GameSessionSnapshot getSessionSnapshot(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        return GameSessionSnapshotFactory.fromSession(session);
    }

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

    private GameActionResult executeAction(
        GameSession session, PlayerAction action, String clientActionId) {
        return executeAction(session, action, clientActionId, "Player resigned.");
    }

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

    private GameActionResult executePlace(
        GameSession session,
        Player currentPlayer,
        PlayerAction action,
        String actionId,
        String clientActionId) {
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

    public GameActionResult executeRemoteCommand(GameSession session, PlayerAction action) {
        return executeRemoteCommand(session, action, null);
    }

    public GameActionResult executeRemoteCommand(
        GameSession session, PlayerAction action, String clientActionId) {
        return executeAction(session, action, clientActionId);
    }

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

    private void ensureEditingAllowed(GameSession session) {
        ensureSessionInProgress(session);
    }

    private void ensureSessionInProgress(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game session is not in progress.");
        }
    }

    private void ensureDictionaryLoaded(GameSession session) {
        if (dictionaryRepository.getLoadedDictionaryType() != session.getConfig().getDictionaryType()) {
            dictionaryRepository.loadDictionary(session.getConfig().getDictionaryType());
        }
    }

    private Player requireCurrentPlayer(GameSession session) {
        return session.getGameState().requireCurrentActivePlayer();
    }

    private void validateActionOwner(Player currentPlayer, PlayerAction action) {
        if (!Objects.equals(currentPlayer.getPlayerId(), action.playerId())) {
            throw new IllegalArgumentException(
                "Action playerId="
                + action.playerId()
                + " does not match current playerId="
                + currentPlayer.getPlayerId());
        }
    }

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