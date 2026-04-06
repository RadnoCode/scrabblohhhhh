package com.kotva.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.session.GameSession;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.mode.PlayerController;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.SessionStatus;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class NonUiGameFlowTest {
    @Test
    public void gameCanRunEndToEndWithoutUiThroughControllersAndServices() {
        ClockService clockService = new ClockServiceImpl();
        StubDictionaryRepository dictionaryRepository = new StubDictionaryRepository();
        GameSetupService setupService =
                new GameSetupServiceImpl(dictionaryRepository, clockService, new Random(11L));
        GameApplicationService applicationService =
                new GameApplicationServiceImpl(clockService, dictionaryRepository);

        GameSession session =
                setupService.startNewGame(
                        new NewGameRequest(
                                GameMode.HOT_SEAT,
                                2,
                                List.of("Alice", "Bob"),
                                DictionaryType.AM,
                                null));

        PlayerController firstController =
                session.getGameState().requireCurrentActivePlayer().getController();
        TurnTransitionResult firstResult = firstController.passTurn(applicationService, session);

        assertTrue(firstResult.isSuccess());
        assertEquals(SessionStatus.IN_PROGRESS, session.getSessionStatus());
        assertNotNull(firstResult.getNextPlayerId());

        PlayerController secondController =
                session.getGameState().requireCurrentActivePlayer().getController();
        TurnTransitionResult secondResult = secondController.passTurn(applicationService, session);

        assertTrue(secondResult.isSuccess());
        assertTrue(secondResult.isGameEnded());
        assertEquals(SessionStatus.COMPLETED, session.getSessionStatus());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, session.getGameState().getGameEndReason());
        assertNotNull(secondResult.getSettlementResult());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, secondResult.getSettlementResult().getEndReason());
        assertEquals(2, session.getTurnCoordinator().getTurnNumber());
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
