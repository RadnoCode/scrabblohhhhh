package com.kotva.presentation.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kotva.application.runtime.GameRuntime;
import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.Position;
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

    private static final class StubRuntime implements GameRuntime {
        private final boolean hasTimeControl;
        private final boolean requiresBackgroundRefresh;

        private StubRuntime(boolean hasTimeControl, boolean requiresBackgroundRefresh) {
            this.hasTimeControl = hasTimeControl;
            this.requiresBackgroundRefresh = requiresBackgroundRefresh;
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
            return false;
        }

        @Override
        public GameSession getSession() {
            return null;
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
            return null;
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
