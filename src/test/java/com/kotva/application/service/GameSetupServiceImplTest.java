
package com.kotva.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.TimeControlConfig;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.RackSlot;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class GameSetupServiceImplTest {

        @Test
    public void buildConfigNormalizesHotSeatPlayersAndTimeControl() {
        GameSetupServiceImpl service =
        new GameSetupServiceImpl(new StubDictionaryRepository(), new ClockServiceImpl(), new Random(7L));

        GameConfig config =
        service.buildConfig(
            new NewGameRequest(
            GameMode.HOT_SEAT,
            3,
            List.of(" Alice ", "Bob", "Carol"),
            DictionaryType.AM,
            new TimeControlConfig(60_000L, 10_000L)));

        assertEquals(GameMode.HOT_SEAT, config.getGameMode());
        assertEquals(3, config.getPlayerCount());
        assertEquals("Alice", config.getPlayers().get(0).getPlayerName());
        assertTrue(
            config.getPlayers().stream()
            .allMatch(playerConfig -> playerConfig.getPlayerType() == PlayerType.LOCAL));
        assertEquals(60_000L, config.getTimeControlConfig().getMainTimeMillis());
        assertEquals(10_000L, config.getTimeControlConfig().getByoYomiMillisPerTurn());
    }

        @Test
    public void buildConfigRejectsInvalidSetupInputs() {
        GameSetupServiceImpl service =
        new GameSetupServiceImpl(new StubDictionaryRepository(), new ClockServiceImpl(), new Random(7L));

        assertThrows(
            IllegalArgumentException.class,
            () ->
            service.buildConfig(
            new NewGameRequest(
            GameMode.HOT_SEAT,
            1,
            List.of("Alice"),
            DictionaryType.AM,
            null)));

        assertThrows(
            IllegalArgumentException.class,
            () ->
            service.buildConfig(
            new NewGameRequest(
            GameMode.HOT_SEAT,
            2,
            List.of("Alice"),
            DictionaryType.AM,
            null)));

        assertThrows(
            IllegalArgumentException.class,
            () ->
            service.buildConfig(
            new NewGameRequest(
            GameMode.HOT_SEAT,
            2,
            List.of("Alice", " "),
            DictionaryType.AM,
            null)));

        assertThrows(
            IllegalArgumentException.class,
            () ->
            service.buildConfig(
            new NewGameRequest(
            GameMode.HOT_SEAT,
            2,
            List.of("Alice", "alice"),
            DictionaryType.AM,
            null)));

        assertThrows(
            IllegalArgumentException.class,
            () ->
            service.buildConfig(
            new NewGameRequest(
            GameMode.HUMAN_VS_AI,
            2,
            List.of("Alice", "Bob"),
            DictionaryType.AM,
            null)));

        GameConfig localAiConfig =
        service.buildConfig(
            new NewGameRequest(
            GameMode.HUMAN_VS_AI,
            2,
            List.of("Alice", "Bob"),
            DictionaryType.AM,
            null,
            AiDifficulty.HARD));
        assertEquals(GameMode.HUMAN_VS_AI, localAiConfig.getGameMode());
        assertEquals(PlayerType.LOCAL, localAiConfig.getPlayers().get(0).getPlayerType());
        assertEquals(PlayerType.AI, localAiConfig.getPlayers().get(1).getPlayerType());
        assertEquals(AiDifficulty.HARD, localAiConfig.getAiDifficulty());
    }

        @Test
    public void startNewGameInitializesSessionDictionaryOrderAndTiles() {
        StubDictionaryRepository dictionaryRepository = new StubDictionaryRepository();
        ClockService clockService = new ClockServiceImpl();
        GameSetupServiceImpl service =
        new GameSetupServiceImpl(dictionaryRepository, clockService, new Random(7L));

        GameSession session =
        service.startNewGame(
            new NewGameRequest(
            GameMode.HOT_SEAT,
            4,
            List.of("Alice", "Bob", "Carol", "Dave"),
            DictionaryType.BR,
            new TimeControlConfig(90_000L, 15_000L)));

        assertEquals(SessionStatus.IN_PROGRESS, session.getSessionStatus());
        assertTrue(session.getTurnDraft().getPlacements().isEmpty());
        assertEquals(DictionaryType.BR, dictionaryRepository.getLoadedDictionaryType());
        assertEquals(
            session.getGameState().getPlayers().get(0).getPlayerId(),
            session.getGameState().getCurrentPlayer().getPlayerId());
        assertEquals(ClockPhase.MAIN_TIME, session.getGameState().getCurrentPlayer().getClock().getPhase());

        for (Player player : session.getGameState().getPlayers()) {
            assertEquals(7, countRackTiles(player));
        }
    }

        @Test
    public void startNewGameUsesStableRandomizedPlayerOrder() {
        NewGameRequest request =
        new NewGameRequest(
            GameMode.HOT_SEAT,
            4,
            List.of("Alice", "Bob", "Carol", "Dave"),
            DictionaryType.AM,
            null);

        GameSetupServiceImpl firstService =
        new GameSetupServiceImpl(new StubDictionaryRepository(), new ClockServiceImpl(), new Random(11L));
        GameSetupServiceImpl secondService =
        new GameSetupServiceImpl(new StubDictionaryRepository(), new ClockServiceImpl(), new Random(11L));

        List<String> firstOrder = extractPlayerNames(firstService.startNewGame(request));
        List<String> secondOrder = extractPlayerNames(secondService.startNewGame(request));

        assertEquals(firstOrder, secondOrder);
        assertNotEquals(request.getPlayerNames(), firstOrder);
    }

        @Test
    public void startNewGameLeavesUnlimitedGamesWithDisabledClocks() {
        GameSetupServiceImpl service =
        new GameSetupServiceImpl(new StubDictionaryRepository(), new ClockServiceImpl(), new Random(7L));

        GameSession session =
        service.startNewGame(
            new NewGameRequest(
            GameMode.HOT_SEAT,
            2,
            List.of("Alice", "Bob"),
            DictionaryType.AM,
            null));

        assertTrue(
            session.getGameState().getPlayers().stream()
            .allMatch(player -> player.getClock().getPhase() == ClockPhase.DISABLED));
    }

    private List<String> extractPlayerNames(GameSession session) {
        return session.getGameState().getPlayers().stream()
            .map(Player::getPlayerName)
            .collect(Collectors.toList());
    }

    private int countRackTiles(Player player) {
        int tileCount = 0;
        for (RackSlot slot : player.getRack().getSlots()) {
            if (!slot.isEmpty()) {
                tileCount++;
            }
        }
        return tileCount;
    }

    private static class StubDictionaryRepository extends DictionaryRepository {
        private DictionaryType loadedDictionaryType;

            @Override
        public void loadDictionary(DictionaryType dictionaryType) {
            this.loadedDictionaryType = dictionaryType;
        }

            @Override
        public Set<String> getDictionary() {
            return Collections.singleton("BOOK");
        }

            @Override
        public DictionaryType getLoadedDictionaryType() {
            return loadedDictionaryType;
        }

            @Override
        public boolean isAccepted(String word) {
            return "BOOK".equalsIgnoreCase(word);
        }
    }
}