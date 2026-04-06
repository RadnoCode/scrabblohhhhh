package com.kotva.application.service;

import com.kotva.application.draft.DraftManager;
import com.kotva.application.draft.TurnDraftActionMapper;
import com.kotva.application.draft.DraftManager;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.result.SettlementResult;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.session.PlayerClockSnapshot;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameApplicationServiceImpl implements GameApplicationService {
    private final ClockService clockService;
    private final DraftManager draftManager;
    private final MovePreviewService movePreviewService;
    private final DictionaryRepository dictionaryRepository;
    private final DraftManager draftManager;

    public GameApplicationServiceImpl(ClockService clockService) {
        this(clockService, new DraftManager(), null);
    }

    public GameApplicationServiceImpl(
            ClockService clockService,
            DraftManager draftManager,
            MovePreviewService movePreviewService) {
        this(clockService, new DictionaryRepository());
    }

    public GameApplicationServiceImpl(
            ClockService clockService, DictionaryRepository dictionaryRepository) {
        this.clockService = Objects.requireNonNull(clockService, "clockService cannot be null.");
        this.draftManager = Objects.requireNonNull(draftManager, "draftManager cannot be null.");
        this.movePreviewService = movePreviewService;
        this.dictionaryRepository =
                Objects.requireNonNull(dictionaryRepository, "dictionaryRepository cannot be null.");
        this.draftManager = new DraftManager();
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
    public PreviewResult recallAllDraftTiles(GameSession session) {
        ensureEditingAllowed(session);
        draftManager.recallAllTiles(session.getTurnDraft());
        return refreshPreview(session);
    }

    @Override
    public SubmitDraftResult submitDraft(GameSession session) {
        Player currentPlayer = requireCurrentPlayer(session);
        PlayerAction action =
                TurnDraftActionMapper.toPlaceAction(currentPlayer.getPlayerId(), session.getTurnDraft());
        ActionDispatchResult result = executeAction(session, action);
        return new SubmitDraftResult(
                result.success,
                result.message,
                result.awardedScore,
                result.nextPlayerId,
                result.gameEnded,
                result.settlementResult);
    }

    @Override
    public TurnTransitionResult passTurn(GameSession session) {
        Player currentPlayer = requireCurrentPlayer(session);
        ActionDispatchResult result =
                executeAction(session, PlayerAction.pass(currentPlayer.getPlayerId()));
        return new TurnTransitionResult(
                result.success,
                result.message,
                result.nextPlayerId,
                result.gameEnded,
                result.settlementResult);
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
        String validationMessage = validateDraft(session);
        PreviewResult previewResult;
        if (validationMessage != null) {
            previewResult = new PreviewResult(false, 0, List.of(), List.of(), List.of(validationMessage));
        } else {
            Player currentPlayer = requireCurrentPlayer(session);
            PlayerAction action =
                    TurnDraftActionMapper.toPlaceAction(
                            currentPlayer.getPlayerId(), session.getTurnDraft());
            int estimatedScore =
                    ScoreCalculator.calculate(
                            WordExtractor.extract(
                                    action,
                                    session.getGameState().getTileBag(),
                                    session.getGameState().getBoard()),
                            session.getGameState(),
                            action);
            previewResult = new PreviewResult(true, estimatedScore, List.of(), List.of(), List.of());
        }

        session.getTurnDraft().setPreviewResult(previewResult);
        return previewResult;
    }

    private String validateDraft(GameSession session) {
        ensureDictionaryLoaded(session);
        RuleEngine ruleEngine = new RuleEngine(dictionaryRepository);
        Player currentPlayer = requireCurrentPlayer(session);
        PlayerAction action =
                TurnDraftActionMapper.toPlaceAction(currentPlayer.getPlayerId(), session.getTurnDraft());
        return ruleEngine.validateMove(session.getGameState(), action);
    }

    private ActionDispatchResult executeAction(GameSession session, PlayerAction action) {
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(action, "action cannot be null.");
        ensureSessionInProgress(session);

        Player currentPlayer = requireCurrentPlayer(session);
        validateActionOwner(currentPlayer, action);

        return switch (action.type()) {
            case PLACE_TILE -> executePlace(session, currentPlayer, action);
            case PASS_TURN -> executePass(session, action);
            case LOSE -> executeLose(session, action);
        };
    }

    private ActionDispatchResult executePlace(
            GameSession session, Player currentPlayer, PlayerAction action) {
        ensureDictionaryLoaded(session);
        RuleEngine ruleEngine = new RuleEngine(dictionaryRepository);
        String validationMessage = ruleEngine.validateMove(session.getGameState(), action);
        if (validationMessage != null) {
            return ActionDispatchResult.failure(validationMessage, currentPlayer.getPlayerId());
        }

        List<CandidateWord> words =
                WordExtractor.extract(
                        action, session.getGameState().getTileBag(), session.getGameState().getBoard());
        int awardedScore = ScoreCalculator.calculate(words, session.getGameState(), action);

        ruleEngine.apply(session.getGameState(), action);
        currentPlayer.addScore(awardedScore);
        refillRack(currentPlayer, session.getGameState().getTileBag());
        session.resetTurnDraft();
        clockService.stopTurnClock(session);

        SettlementResult settlementResult = session.getTurnCoordinator().onActionApplied(action);
        return completeTransition(session, awardedScore, "Draft submitted.", settlementResult);
    }

    private ActionDispatchResult executePass(GameSession session, PlayerAction action) {
        RuleEngine ruleEngine = new RuleEngine(dictionaryRepository);
        ruleEngine.apply(session.getGameState(), action);
        session.resetTurnDraft();
        clockService.stopTurnClock(session);

        SettlementResult settlementResult = session.getTurnCoordinator().onActionApplied(action);
        return completeTransition(session, 0, "Turn passed.", settlementResult);
    }

    private ActionDispatchResult executeLose(GameSession session, PlayerAction action) {
        RuleEngine ruleEngine = new RuleEngine(dictionaryRepository);
        ruleEngine.apply(session.getGameState(), action);
        session.resetTurnDraft();
        clockService.stopTurnClock(session);

        SettlementResult settlementResult = session.getTurnCoordinator().onActionApplied(action);
        return completeTransition(session, 0, "Player lost the turn.", settlementResult);
    }

    private ActionDispatchResult completeTransition(
            GameSession session, int awardedScore, String message, SettlementResult settlementResult) {
        if (session.getTurnCoordinator().isGameEnded()) {
            session.setSessionStatus(SessionStatus.COMPLETED);
            return ActionDispatchResult.success(message, awardedScore, null, true, settlementResult);
        }

        clockService.startTurnClock(session);
        return ActionDispatchResult.success(
                message, awardedScore, requireCurrentPlayer(session).getPlayerId(), false, null);
    }

    private void handleTimeoutIfNeeded(GameSession session) {
        Player currentPlayer = session.getGameState().getCurrentPlayer();
        if (!currentPlayer.getActive()) {
            return;
        }

        if (currentPlayer.getClock().getPhase() == ClockPhase.TIMEOUT) {
            executeAction(session, PlayerAction.lose(currentPlayer.getPlayerId()));
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

    private static final class ActionDispatchResult {
        private final boolean success;
        private final String message;
        private final int awardedScore;
        private final String nextPlayerId;
        private final boolean gameEnded;
        private final SettlementResult settlementResult;

        private ActionDispatchResult(
                boolean success,
                String message,
                int awardedScore,
                String nextPlayerId,
                boolean gameEnded,
                SettlementResult settlementResult) {
            this.success = success;
            this.message = message;
            this.awardedScore = awardedScore;
            this.nextPlayerId = nextPlayerId;
            this.gameEnded = gameEnded;
            this.settlementResult = settlementResult;
        }

        private static ActionDispatchResult failure(String message, String nextPlayerId) {
            return new ActionDispatchResult(false, message, 0, nextPlayerId, false, null);
        }

        private static ActionDispatchResult success(
                String message,
                int awardedScore,
                String nextPlayerId,
                boolean gameEnded,
                SettlementResult settlementResult) {
            return new ActionDispatchResult(
                    true, message, awardedScore, nextPlayerId, gameEnded, settlementResult);
        }
    }
}
