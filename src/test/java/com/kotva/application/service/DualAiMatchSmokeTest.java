package com.kotva.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.ai.AiMove;
import com.kotva.ai.AiMoveOptionSet;
import com.kotva.ai.QuackleNativeBridge;
import com.kotva.application.result.PlayerSettlement;
import com.kotva.application.result.SettlementResult;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.PlayerConfig;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.model.Player;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.mode.PlayerController;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Assume;
import org.junit.Test;

public class DualAiMatchSmokeTest {
    private static final int TOTAL_TILE_COUNT = 101;
    private static final int RACK_SIZE = 7;
    private static final int MAX_TURNS = 300;

    @Test
    public void dualEasyAiMatchConsumesBagCorrectlyAndProducesSettlement() throws Exception {
        QuackleNativeBridge bridge = new QuackleNativeBridge();
        Assume.assumeTrue(Files.isRegularFile(bridge.getLibraryPath()));
        Assume.assumeTrue(Files.isDirectory(bridge.getDataDirectory()));

        ClockService clockService = new ClockServiceImpl();
        DictionaryRepository dictionaryRepository = new DictionaryRepository();
        GameSetupService setupService =
            new GameSetupServiceImpl(dictionaryRepository, clockService, new Random(23L));
        GameApplicationService applicationService =
            new GameApplicationServiceImpl(clockService, dictionaryRepository);

        GameSession session = setupService.startNewGame(createDualAiConfig());
        assertEquals(SessionStatus.IN_PROGRESS, session.getSessionStatus());
        assertEquals(2, session.getGameState().getPlayers().size());
        for (Player player : session.getGameState().getPlayers()) {
            assertEquals(PlayerType.AI, player.getPlayerType());
            assertTrue(player.getController().supportsAutomatedTurn());
            assertEquals(RACK_SIZE, countRackTiles(player));
        }

        int initialBagSize = session.getGameState().getTileBag().size();
        assertEquals(TOTAL_TILE_COUNT - (RACK_SIZE * 2), initialBagSize);

        int previousBagSize = initialBagSize;
        int turnCount = 0;
        int placeCount = 0;
        int passCount = 0;
        List<Integer> bagTimeline = new ArrayList<>();
        bagTimeline.add(initialBagSize);

        try (AiTurnCoordinator aiTurnCoordinator =
            new AiTurnCoordinator(bridge, DictionaryType.AM, AiDifficulty.EASY)) {
            while (session.getSessionStatus() == SessionStatus.IN_PROGRESS && turnCount < MAX_TURNS) {
                Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
                AcceptedTurn acceptedTurn = applyAcceptedTurn(
                    aiTurnCoordinator,
                    applicationService,
                    session,
                    currentPlayer.getController());
                turnCount++;

                int bagAfter = session.getGameState().getTileBag().size();
                assertTrue("Bag size must never increase.", bagAfter <= previousBagSize);

                if (acceptedTurn.result().getActionType() == ActionType.PLACE_TILE) {
                    placeCount++;
                    int expectedBagSize =
                        previousBagSize - Math.min(previousBagSize, acceptedTurn.move().placements().size());
                    assertEquals(expectedBagSize, bagAfter);
                } else {
                    passCount++;
                    assertEquals(ActionType.PASS_TURN, acceptedTurn.result().getActionType());
                    assertEquals(previousBagSize, bagAfter);
                }

                previousBagSize = bagAfter;
                bagTimeline.add(bagAfter);
            }
        }

        assertTrue("Dual AI match did not finish within the turn budget.", turnCount < MAX_TURNS);
        assertEquals(SessionStatus.COMPLETED, session.getSessionStatus());
        assertTrue(session.getGameState().isGameOver());
        assertTrue("At least one placement is expected in a real AI match.", placeCount > 0);
        assertTrue("Bag should shrink after at least one placement.", previousBagSize < initialBagSize);

        SettlementResult settlement = session.getTurnCoordinator().getSettlementResult();
        assertNotNull(settlement);
        assertEquals(session.getGameState().getGameEndReason(), settlement.getEndReason());
        assertEquals(2, settlement.getRankings().size());
        assertFalse(settlement.getSummaryMessages().isEmpty());

        System.out.println(
            "Dual AI easy-vs-easy summary:"
            + " turns=" + turnCount
            + ", placeMoves=" + placeCount
            + ", passMoves=" + passCount
            + ", initialBag=" + initialBagSize
            + ", finalBag=" + previousBagSize
            + ", endReason=" + settlement.getEndReason()
            + ", rankings=" + formatRankings(settlement.getRankings())
            + ", bagTimeline=" + bagTimeline);
    }

    private static GameConfig createDualAiConfig() {
        return new GameConfig(
            GameMode.HUMAN_VS_AI,
            List.of(
                new PlayerConfig("Easy Bot A", PlayerType.AI),
                new PlayerConfig("Easy Bot B", PlayerType.AI)),
            DictionaryType.AM,
            null,
            AiDifficulty.EASY);
    }

    private static AcceptedTurn applyAcceptedTurn(
        AiTurnCoordinator aiTurnCoordinator,
        GameApplicationService applicationService,
        GameSession session,
        PlayerController controller) throws Exception {
        AiMoveOptionSet moveOptions = controller.requestAutomatedTurn(aiTurnCoordinator, session)
            .get(30, TimeUnit.SECONDS);

        assertNotNull(moveOptions);
        assertFalse(moveOptions.isEmpty());

        List<String> rejectionMessages = new ArrayList<>();
        for (AiMove candidate : moveOptions.moves()) {
            AiTurnAttemptResult attempt = controller.applyAutomatedTurn(
                aiTurnCoordinator,
                applicationService,
                session,
                candidate);
            if (attempt.accepted()) {
                GameActionResult latestActionResult = session.getLatestActionResult();
                assertNotNull(latestActionResult);
                assertTrue(latestActionResult.isSuccess());
                return new AcceptedTurn(candidate, latestActionResult);
            }

            rejectionMessages.add(attempt.rejectionCode() + ": " + attempt.rejectionReason());
        }

        throw new AssertionError("No AI move candidate was accepted. Rejections=" + rejectionMessages);
    }

    private static int countRackTiles(Player player) {
        int count = 0;
        for (var slot : player.getRack().getSlots()) {
            if (!slot.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static String formatRankings(List<PlayerSettlement> rankings) {
        List<String> formatted = new ArrayList<>();
        for (PlayerSettlement ranking : rankings) {
            formatted.add(
                ranking.getRank() + ":" + ranking.getPlayerName() + "=" + ranking.getFinalScore());
        }
        return formatted.toString();
    }

    private record AcceptedTurn(AiMove move, GameActionResult result) {
    }
}
