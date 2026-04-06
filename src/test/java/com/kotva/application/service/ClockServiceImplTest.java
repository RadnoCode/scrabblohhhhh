package com.kotva.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.TimeControlConfig;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.Player;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.application.draft.TurnDraft;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.SessionStatus;

public class ClockServiceImplTest {
    @Test
    public void tickMovesFromMainTimeIntoByoYomiAndSnapshotReflectsChanges() {
        ClockServiceImpl clockService = new ClockServiceImpl();
        GameApplicationServiceImpl applicationService = new GameApplicationServiceImpl(clockService);
        GameSession session = createTimedSession(clockService, 1_000L, 300L);
        Player firstPlayer = session.getGameState().getCurrentPlayer();

        GameSessionSnapshot enteredByoYomi = applicationService.tickClock(session, 1_000L);
        assertEquals(0L, firstPlayer.getClock().getMainTimeRemainingMillis());
        assertEquals(ClockPhase.BYO_YOMI, firstPlayer.getClock().getPhase());
        assertEquals(300L, firstPlayer.getClock().getByoYomiRemainingMillis());
        assertEquals(ClockPhase.BYO_YOMI, enteredByoYomi.getCurrentPlayerClockPhase());
        assertEquals(300L, enteredByoYomi.getCurrentPlayerByoYomiRemainingMillis());

        GameSessionSnapshot tickingSnapshot = applicationService.tickClock(session, 100L);
        assertEquals(200L, firstPlayer.getClock().getByoYomiRemainingMillis());
        assertEquals(200L, tickingSnapshot.getCurrentPlayerByoYomiRemainingMillis());
        assertEquals(firstPlayer.getPlayerId(), tickingSnapshot.getCurrentPlayerId());

        clockService.stopTurnClock(session);
        session.getGameState().advanceToNextActivePlayer();
        clockService.startTurnClock(session);
        session.getGameState().advanceToNextActivePlayer();
        clockService.startTurnClock(session);

        assertEquals(ClockPhase.BYO_YOMI, firstPlayer.getClock().getPhase());
        assertEquals(300L, firstPlayer.getClock().getByoYomiRemainingMillis());
    }

    @Test
    public void tickTimeoutEliminatesCurrentPlayerAndEndsTwoPlayerGame() {
        ClockServiceImpl clockService = new ClockServiceImpl();
        GameApplicationServiceImpl applicationService = new GameApplicationServiceImpl(clockService);
        GameSession session = createTimedSession(clockService, 100L, 50L);
        Player timedOutPlayer = session.getGameState().getCurrentPlayer();
        TurnDraft originalDraft = session.getTurnDraft();

        GameSessionSnapshot snapshot = applicationService.tickClock(session, 200L);

        assertFalse(timedOutPlayer.getActive());
        assertEquals(ClockPhase.TIMEOUT, timedOutPlayer.getClock().getPhase());
        assertTrue(session.getGameState().isGameOver());
        assertEquals(GameEndReason.ONLY_ONE_PLAYER_REMAINING, session.getGameState().getGameEndReason());
        assertEquals(SessionStatus.COMPLETED, session.getSessionStatus());
        assertEquals(SessionStatus.COMPLETED, snapshot.getSessionStatus());
        assertNotSame(originalDraft, session.getTurnDraft());
    }

    private GameSession createTimedSession(
            ClockServiceImpl clockService, long mainTimeMillis, long byoYomiMillisPerTurn) {
        GameSetupServiceImpl service =
                new GameSetupServiceImpl(
                        new StubDictionaryRepository(),
                        clockService,
                        new Random(5L));

        return service.startNewGame(
                        new NewGameRequest(
                                GameMode.HOT_SEAT,
                                2,
                                List.of("Alice", "Bob"),
                                DictionaryType.AM,
                                new TimeControlConfig(mainTimeMillis, byoYomiMillisPerTurn)));
    }

    private static class StubDictionaryRepository extends DictionaryRepository {
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
