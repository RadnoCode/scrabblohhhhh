package com.kotva.presentation.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.kotva.application.service.GameActionResult;
import com.kotva.application.runtime.GameRuntime;
import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.session.PlayerConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.mode.GameMode;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Test;

public class GameControllerTest {

    @Test
    public void calculateElapsedMillisReturnsZeroWhenClockBaselineIsUninitialized() {
        assertEquals(0L, GameController.calculateElapsedMillis(0L, 5_000_000_000L));
        assertEquals(0L, GameController.calculateElapsedMillis(-1L, 5_000_000_000L));
    }

    @Test
    public void calculateElapsedMillisReturnsZeroWhenCurrentTickDoesNotAdvance() {
        assertEquals(0L, GameController.calculateElapsedMillis(2_000_000L, 2_000_000L));
        assertEquals(0L, GameController.calculateElapsedMillis(3_000_000L, 2_000_000L));
    }

    @Test
    public void calculateElapsedMillisReturnsWholeMillisecondsForAdvancedClock() {
        assertEquals(2L, GameController.calculateElapsedMillis(1_000_000L, 3_999_999L));
        assertEquals(15L, GameController.calculateElapsedMillis(10_000_000L, 25_400_000L));
    }

    @Test
    public void shouldStartPollingWhenRuntimeHasTimeControl() {
        assertTrue(GameController.shouldStartPolling(new StubRuntime(true, false)));
    }

    @Test
    public void shouldStartPollingWhenRuntimeRequiresBackgroundRefresh() {
        assertTrue(GameController.shouldStartPolling(new StubRuntime(false, true)));
    }

    @Test
    public void shouldNotStartPollingWithoutTimeControlOrBackgroundRefresh() {
        assertFalse(GameController.shouldStartPolling(new StubRuntime(false, false)));
    }

    @Test
    public void resolveLatestActionResultFallsBackToSnapshotForClientRuntime() {
        GameActionResult latestActionResult =
            new GameActionResult(
                "action-1",
                "client-1",
                "p1",
                ActionType.PASS_TURN,
                true,
                "Turn passed.",
                0,
                "p2",
                false);
        GameSession session = createInProgressSession();
        session.setLatestActionResult(latestActionResult);
        GameSessionSnapshot snapshot = GameSessionSnapshotFactory.fromSession(session);

        assertSame(
            latestActionResult,
            GameController.resolveLatestActionResult(
                new StubRuntime(false, true, true, null, snapshot)));
    }

    private GameSession createInProgressSession() {
        Player first = new Player("p1", "Alice", PlayerType.LOCAL);
        Player second = new Player("p2", "Bob", PlayerType.LAN);
        GameState gameState = new GameState(List.of(first, second));
        GameConfig config =
            new GameConfig(
                GameMode.LAN_MULTIPLAYER,
                List.of(
                    new PlayerConfig("Alice", PlayerType.LOCAL),
                    new PlayerConfig("Bob", PlayerType.LAN)),
                DictionaryType.AM,
                null);
        GameSession session = new GameSession("session-1", config, gameState);
        session.setSessionStatus(SessionStatus.IN_PROGRESS);
        return session;
    }

    private static final class StubRuntime implements GameRuntime {
        private final boolean hasTimeControl;
        private final boolean requiresBackgroundRefresh;
        private final boolean hasSession;
        private final GameSession session;
        private final GameSessionSnapshot snapshot;

        private StubRuntime(boolean hasTimeControl, boolean requiresBackgroundRefresh) {
            this(hasTimeControl, requiresBackgroundRefresh, false, null, null);
        }

        private StubRuntime(
            boolean hasTimeControl,
            boolean requiresBackgroundRefresh,
            boolean hasSession,
            GameSession session,
            GameSessionSnapshot snapshot) {
            this.hasTimeControl = hasTimeControl;
            this.requiresBackgroundRefresh = requiresBackgroundRefresh;
            this.hasSession = hasSession;
            this.session = session;
            this.snapshot = snapshot;
        }

        @Override
        public void start(NewGameRequest request) {
        }

        @Override
        public boolean requiresBackgroundRefresh() {
            return requiresBackgroundRefresh;
        }

        @Override
        public boolean hasSession() {
            return hasSession;
        }

        @Override
        public GameSession getSession() {
            return session;
        }

        @Override
        public boolean hasTimeControl() {
            return hasTimeControl;
        }

        @Override
        public boolean isSessionInProgress() {
            return false;
        }

        @Override
        public GameSessionSnapshot getSessionSnapshot() {
            return snapshot;
        }

        @Override
        public GameSessionSnapshot tickClock(long elapsedMillis) {
            return null;
        }

        @Override
        public void placeDraftTile(String tileId, Position position) {
        }

        @Override
        public void moveDraftTile(String tileId, Position position) {
        }

        @Override
        public void removeDraftTile(String tileId) {
        }

        @Override
        public void recallAllDraftTiles() {
        }

        @Override
        public void submitDraft() {
        }

        @Override
        public void passTurn() {
        }

        @Override
        public void resign() {
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
        public void requestAutomatedTurnIfIdle(
            Consumer<AiSessionRuntime.TurnCompletion> completionConsumer) {
        }

        @Override
        public boolean matchesAutomatedTurn(AiSessionRuntime.TurnCompletion completion) {
            return false;
        }

        @Override
        public void applyAutomatedTurn(AiSessionRuntime.TurnCompletion completion) {
        }

        @Override
        public void cancelPendingAutomatedTurn() {
        }

        @Override
        public void disableAutomatedTurnSupport() {
        }

        @Override
        public void shutdown() {
        }
    }
}
