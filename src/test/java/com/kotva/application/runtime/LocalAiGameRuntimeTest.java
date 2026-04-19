package com.kotva.application.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.kotva.ai.AiMove;
import com.kotva.ai.AiMoveOptionSet;
import com.kotva.application.service.AiRuntimeBootstrapper;
import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.service.AiTurnAttemptResult;
import com.kotva.application.service.AiTurnRuntime;
import com.kotva.application.service.ClockService;
import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.service.GameSetupServiceImpl;
import com.kotva.application.session.AiRuntimeFailureKind;
import com.kotva.application.session.AiRuntimeSnapshot;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.SessionStatus;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.Test;

public class LocalAiGameRuntimeTest {
    @Test
    public void aiInitializationRetriesOnceAndSucceeds() {
        StubAiTurnRuntime aiTurnRuntime = new StubAiTurnRuntime();
        AtomicInteger bootstrapperAttempts = new AtomicInteger();
        LocalAiGameRuntime runtime = createRuntime(() -> {
            int attempt = bootstrapperAttempts.incrementAndGet();
            if (attempt == 1) {
                return new FailingBootstrapper("native load failed");
            }
            return new StubBootstrapper(aiTurnRuntime);
        });

        runtime.start(createAiRequest());

        assertEquals(2, bootstrapperAttempts.get());
        assertTrue(runtime.hasSession());
        assertTrue(runtime.isSessionInProgress());
        assertTrue(runtime.hasAutomatedTurnSupport());
        assertNull(runtime.getSessionSnapshot().getAiRuntimeSnapshot());
    }

    @Test
    public void aiInitializationFailureEndsMatchInFrozenState() {
        AtomicInteger bootstrapperAttempts = new AtomicInteger();
        LocalAiGameRuntime runtime = createRuntime(() -> {
            bootstrapperAttempts.incrementAndGet();
            return new FailingBootstrapper("missing native library");
        });

        runtime.start(createAiRequest());

        assertEquals(2, bootstrapperAttempts.get());
        assertTrue(runtime.hasSession());
        assertFalse(runtime.isSessionInProgress());
        assertFalse(runtime.hasAutomatedTurnSupport());
        assertEquals(SessionStatus.COMPLETED, runtime.getSession().getSessionStatus());
        assertEquals(
                GameEndReason.AI_RUNTIME_FAILURE,
                runtime.getSession().getGameState().getGameEndReason());

        AiRuntimeSnapshot snapshot = runtime.getSessionSnapshot().getAiRuntimeSnapshot();
        assertNotNull(snapshot);
        assertTrue(snapshot.fatal());
        assertTrue(snapshot.interactionLocked());
        assertEquals(AiRuntimeFailureKind.INIT_RETRY_EXHAUSTED, snapshot.failureKind());
    }

    @Test
    public void aiCandidateFallbackUsesNextOptionAndResetsIllegalCounterOnSuccess() {
        StubAiTurnRuntime aiTurnRuntime = new StubAiTurnRuntime();
        AiMove firstMove = passMove(1);
        AiMove secondMove = passMove(2);
        aiTurnRuntime.queueResult(AiTurnAttemptResult.rejected(
                firstMove,
                "SUBMIT_REJECTED",
                "First AI move is illegal.",
                null));
        aiTurnRuntime.queueResult(AiTurnAttemptResult.accepted(secondMove, 12, "p1"));
        aiTurnRuntime.queueResult(AiTurnAttemptResult.rejected(
                firstMove,
                "SUBMIT_REJECTED",
                "Illegal move after reset.",
                null));

        LocalAiGameRuntime runtime = createRuntime(() -> new StubBootstrapper(aiTurnRuntime));
        runtime.start(createAiRequest());
        runtime.passTurn();

        runtime.applyAutomatedTurn(completionForCurrentTurn(
                runtime,
                new AiMoveOptionSet(List.of(firstMove, secondMove))));

        assertEquals(List.of(firstMove, secondMove), aiTurnRuntime.getAppliedMoves());
        assertNull(runtime.getSessionSnapshot().getAiRuntimeSnapshot());

        runtime.applyAutomatedTurn(completionForCurrentTurn(runtime, AiMoveOptionSet.ofSingle(firstMove)));

        AiRuntimeSnapshot retrySnapshot = runtime.getSessionSnapshot().getAiRuntimeSnapshot();
        assertNotNull(retrySnapshot);
        assertFalse(retrySnapshot.fatal());
        assertEquals(1, retrySnapshot.consecutiveIllegalMoveCount());
        assertEquals(1, retrySnapshot.candidateCount());
        assertEquals(1, retrySnapshot.attemptedCandidateCount());
    }

    @Test
    public void repeatedIllegalAiMovesTripCircuitBreaker() {
        StubAiTurnRuntime aiTurnRuntime = new StubAiTurnRuntime();
        AiMove move = passMove(1);
        aiTurnRuntime.queueResult(AiTurnAttemptResult.rejected(
                move,
                "SUBMIT_REJECTED",
                "illegal move #1",
                null));
        aiTurnRuntime.queueResult(AiTurnAttemptResult.rejected(
                move,
                "SUBMIT_REJECTED",
                "illegal move #2",
                null));
        aiTurnRuntime.queueResult(AiTurnAttemptResult.rejected(
                move,
                "SUBMIT_REJECTED",
                "illegal move #3",
                null));

        LocalAiGameRuntime runtime = createRuntime(() -> new StubBootstrapper(aiTurnRuntime));
        runtime.start(createAiRequest());
        runtime.passTurn();

        runtime.applyAutomatedTurn(completionForCurrentTurn(runtime, AiMoveOptionSet.ofSingle(move)));
        assertEquals(1, runtime.getSessionSnapshot().getAiRuntimeSnapshot().consecutiveIllegalMoveCount());
        assertTrue(runtime.isSessionInProgress());

        runtime.applyAutomatedTurn(completionForCurrentTurn(runtime, AiMoveOptionSet.ofSingle(move)));
        assertEquals(2, runtime.getSessionSnapshot().getAiRuntimeSnapshot().consecutiveIllegalMoveCount());
        assertTrue(runtime.isSessionInProgress());

        runtime.applyAutomatedTurn(completionForCurrentTurn(runtime, AiMoveOptionSet.ofSingle(move)));

        assertEquals(SessionStatus.COMPLETED, runtime.getSession().getSessionStatus());
        assertEquals(
                GameEndReason.AI_RUNTIME_FAILURE,
                runtime.getSession().getGameState().getGameEndReason());

        AiRuntimeSnapshot snapshot = runtime.getSessionSnapshot().getAiRuntimeSnapshot();
        assertNotNull(snapshot);
        assertTrue(snapshot.fatal());
        assertTrue(snapshot.interactionLocked());
        assertEquals(AiRuntimeFailureKind.INVALID_MOVE_CIRCUIT_BROKEN, snapshot.failureKind());
        assertEquals(3, snapshot.consecutiveIllegalMoveCount());
    }

    private static LocalAiGameRuntime createRuntime(Supplier<AiRuntimeBootstrapper> bootstrapperSupplier) {
        ClockService clockService = new ClockServiceImpl();
        DictionaryRepository dictionaryRepository = new StubDictionaryRepository();
        GameSetupService gameSetupService =
                new GameSetupServiceImpl(dictionaryRepository, clockService, new Random(19L));
        GameApplicationService gameApplicationService =
                new GameApplicationServiceImpl(clockService, dictionaryRepository);
        return new LocalAiGameRuntime(gameSetupService, gameApplicationService, bootstrapperSupplier);
    }

    private static NewGameRequest createAiRequest() {
        return new NewGameRequest(
                GameMode.HUMAN_VS_AI,
                2,
                List.of("Player", "Bot"),
                DictionaryType.AM,
                null,
                AiDifficulty.EASY);
    }

    private static AiMove passMove(int score) {
        return new AiMove(AiMove.Action.PASS, List.of(), score, 0.0, 0.0);
    }

    private static AiSessionRuntime.TurnCompletion completionForCurrentTurn(
            LocalAiGameRuntime runtime, AiMoveOptionSet moveOptions) {
        String sessionId = runtime.getSession().getSessionId();
        String currentPlayerId =
                runtime.getSession().getGameState().requireCurrentActivePlayer().getPlayerId();
        return new AiSessionRuntime.TurnCompletion(sessionId, currentPlayerId, 0L, moveOptions, null);
    }

    private static final class StubBootstrapper implements AiRuntimeBootstrapper {
        private final StubAiTurnRuntime aiTurnRuntime;

        private StubBootstrapper(StubAiTurnRuntime aiTurnRuntime) {
            this.aiTurnRuntime = aiTurnRuntime;
        }

        @Override
        public AiTurnRuntime create(com.kotva.application.session.GameConfig gameConfig) {
            return aiTurnRuntime;
        }

        @Override
        public String getLibraryPath() {
            return "/stub/native/library";
        }

        @Override
        public String getDataDirectory() {
            return "/stub/data";
        }

        @Override
        public String getPlatform() {
            return "stub-os";
        }
    }

    private static final class FailingBootstrapper implements AiRuntimeBootstrapper {
        private final String message;

        private FailingBootstrapper(String message) {
            this.message = message;
        }

        @Override
        public AiTurnRuntime create(com.kotva.application.session.GameConfig gameConfig) {
            throw new IllegalStateException(message);
        }

        @Override
        public String getLibraryPath() {
            return "/missing/library";
        }

        @Override
        public String getDataDirectory() {
            return "/missing/data";
        }
    }

    private static final class StubAiTurnRuntime implements AiTurnRuntime {
        private final Queue<AiTurnAttemptResult> queuedResults = new ArrayDeque<>();
        private final List<AiMove> appliedMoves = new ArrayList<>();

        @Override
        public void requestTurnIfIdle(
                com.kotva.application.session.GameSession session,
                com.kotva.mode.PlayerController controller,
                java.util.function.Consumer<AiSessionRuntime.TurnCompletion> completionConsumer) {
        }

        @Override
        public void cancelPending() {
        }

        @Override
        public boolean matchesCurrentTurn(
                AiSessionRuntime.TurnCompletion completion,
                com.kotva.application.session.GameSession session,
                com.kotva.domain.model.Player currentPlayer,
                com.kotva.mode.PlayerController controller) {
            return true;
        }

        @Override
        public AiTurnAttemptResult applyMove(
                com.kotva.mode.PlayerController controller,
                GameApplicationService gameApplicationService,
                com.kotva.application.session.GameSession session,
                AiMove move) {
            appliedMoves.add(move);
            if (queuedResults.isEmpty()) {
                throw new AssertionError("No queued AI result for move " + move);
            }
            return queuedResults.remove();
        }

        @Override
        public void close() {
        }

        private void queueResult(AiTurnAttemptResult result) {
            queuedResults.add(result);
        }

        private List<AiMove> getAppliedMoves() {
            return Collections.unmodifiableList(appliedMoves);
        }
    }

    private static final class StubDictionaryRepository extends DictionaryRepository {
        @Override
        public void loadDictionary(DictionaryType dictionaryType) {
        }

        @Override
        public Set<String> getDictionary() {
            return Collections.singleton("BOOK");
        }

        @Override
        public DictionaryType getLoadedDictionaryType() {
            return DictionaryType.AM;
        }

        @Override
        public boolean isAccepted(String word) {
            return "BOOK".equalsIgnoreCase(word);
        }
    }
}
