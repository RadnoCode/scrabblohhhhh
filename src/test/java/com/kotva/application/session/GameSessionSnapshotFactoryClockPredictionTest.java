package com.kotva.application.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameSetupServiceImpl;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.DictionaryType;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class GameSessionSnapshotFactoryClockPredictionTest {
    @Test
    public void withReceivedTimestampStoresLocalReceiptTime() {
        GameSessionSnapshot snapshot = createTimedSnapshot(1_000L, 300L);

        GameSessionSnapshot receivedSnapshot =
                GameSessionSnapshotFactory.withReceivedTimestamp(
                        snapshot,
                        snapshot.getSnapshotSentAtEpochMillis() + 120L);

        assertEquals(
                snapshot.getSnapshotSentAtEpochMillis() + 120L,
                receivedSnapshot.getSnapshotReceivedAtEpochMillis());
        assertEquals(
                snapshot.getSnapshotSentAtEpochMillis(),
                receivedSnapshot.getSnapshotSentAtEpochMillis());
    }

    @Test
    public void withLocalClockPredictionAppliesTransportDelayAndLocalElapsed() {
        GameSessionSnapshot snapshot = createTimedSnapshot(1_000L, 300L);
        GameSessionSnapshot receivedSnapshot =
                GameSessionSnapshotFactory.withReceivedTimestamp(
                        snapshot,
                        snapshot.getSnapshotSentAtEpochMillis() + 120L);

        GameSessionSnapshot predictedSnapshot =
                GameSessionSnapshotFactory.withLocalClockPrediction(receivedSnapshot, 80L);

        assertEquals(800L, predictedSnapshot.getCurrentPlayerMainTimeRemainingMillis());
        assertEquals(300L, predictedSnapshot.getCurrentPlayerByoYomiRemainingMillis());
        assertEquals(ClockPhase.MAIN_TIME, predictedSnapshot.getCurrentPlayerClockPhase());
        assertEquals(
                800L,
                predictedSnapshot.getPlayerClockSnapshots().stream()
                        .filter(clockSnapshot ->
                                clockSnapshot.getPlayerId().equals(predictedSnapshot.getCurrentPlayerId()))
                        .findFirst()
                        .orElseThrow()
                        .getMainTimeRemainingMillis());
    }

    @Test
    public void withLocalClockPredictionTransitionsIntoByoYomiAfterMainTimeExpires() {
        GameSessionSnapshot snapshot = createTimedSnapshot(1_000L, 300L);
        GameSessionSnapshot receivedSnapshot =
                GameSessionSnapshotFactory.withReceivedTimestamp(
                        snapshot,
                        snapshot.getSnapshotSentAtEpochMillis() + 1_100L);

        GameSessionSnapshot predictedSnapshot =
                GameSessionSnapshotFactory.withLocalClockPrediction(receivedSnapshot, 100L);

        assertEquals(0L, predictedSnapshot.getCurrentPlayerMainTimeRemainingMillis());
        assertEquals(100L, predictedSnapshot.getCurrentPlayerByoYomiRemainingMillis());
        assertEquals(ClockPhase.BYO_YOMI, predictedSnapshot.getCurrentPlayerClockPhase());
        assertTrue(
                predictedSnapshot.getPlayers().stream()
                        .filter(GamePlayerSnapshot::isCurrentTurn)
                        .findFirst()
                        .orElseThrow()
                        .getClockSnapshot()
                        .getPhase() == ClockPhase.BYO_YOMI);
    }

    private static GameSessionSnapshot createTimedSnapshot(
            long mainTimeMillis,
            long byoYomiMillisPerTurn) {
        GameSetupServiceImpl gameSetupService =
                new GameSetupServiceImpl(
                        new StubDictionaryRepository(),
                        new ClockServiceImpl(),
                        new Random(5L));
        GameSession session =
                gameSetupService.startNewGame(
                        new NewGameRequest(
                                GameMode.HOT_SEAT,
                                2,
                                List.of("Alice", "Bob"),
                                DictionaryType.AM,
                                new TimeControlConfig(mainTimeMillis, byoYomiMillisPerTurn)));
        return GameSessionSnapshotFactory.fromSession(session);
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
