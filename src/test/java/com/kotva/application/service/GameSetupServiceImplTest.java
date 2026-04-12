/**
 * 包作用：应用层服务测试包，负责验证计时、开局与结算服务行为。
 * 包含类：ClockServiceImplTest、GameSetupServiceImplTest、SettlementServiceImplTest。
 */
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

/**
 * 类作用：测试开局服务实现的配置校验与建局行为。
 * 包含方法：buildConfigNormalizesHotSeatPlayersAndTimeControl、buildConfigRejectsInvalidSetupInputs、startNewGameInitializesSessionDictionaryOrderAndTiles、startNewGameUsesStableRandomizedPlayerOrder、startNewGameLeavesUnlimitedGamesWithDisabledClocks、extractPlayerNames、countRackTiles。
 * 继承/实现：无。
 * 引用类：GameConfig 用于提供开局配置；GameSession 用于承载当前会话、配置与对局状态；TimeControlConfig 用于提供时间控制参数；NewGameRequest 用于承接新对局请求参数；Player 用于访问玩家对象、分数、行动权或牌架；RackSlot 用于配合当前类完成与 RackSlot 相关的处理；DictionaryRepository 用于查询词典是否合法；GameMode 用于配合当前类完成与 GameMode 相关的处理；ClockPhase 用于区分计时阶段；DictionaryType 用于区分词典类型；PlayerType 用于区分玩家控制器类型；SessionStatus 用于表示会话状态；Test 用于标记测试方法。
 */
public class GameSetupServiceImplTest {
    /**
     * 方法作用：测试方法：验证 buildConfigNormalizesHotSeatPlayersAndTimeControl 对应的业务场景。
     */
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

    /**
     * 方法作用：测试方法：验证 buildConfigRejectsInvalidSetupInputs 对应的业务场景。
     */
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

    /**
     * 方法作用：测试方法：验证 startNewGameInitializesSessionDictionaryOrderAndTiles 对应的业务场景。
     */
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

    /**
     * 方法作用：测试方法：验证 startNewGameUsesStableRandomizedPlayerOrder 对应的业务场景。
     */
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

    /**
     * 方法作用：测试方法：验证 startNewGameLeavesUnlimitedGamesWithDisabledClocks 对应的业务场景。
     */
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

    /**
     * 方法作用：提取会话中的玩家名称列表。
     */
    private List<String> extractPlayerNames(GameSession session) {
        return session.getGameState().getPlayers().stream()
                .map(Player::getPlayerName)
                .collect(Collectors.toList());
    }

    /**
     * 方法作用：统计玩家牌架中的字牌数量。
     */
    private int countRackTiles(Player player) {
        int tileCount = 0;
        for (RackSlot slot : player.getRack().getSlots()) {
            if (!slot.isEmpty()) {
                tileCount++;
            }
        }
        return tileCount;
    }

    /**
     * 类作用：测试替身类，用于在测试中提供可控的词典行为。
     * 包含方法：loadDictionary、getDictionary、getLoadedDictionaryType、isAccepted。
     * 继承/实现：继承 DictionaryRepository。
     * 引用类：当前类未直接导入其他自定义类。
     */
    private static class StubDictionaryRepository extends DictionaryRepository {
        private DictionaryType loadedDictionaryType;

        /**
         * 方法作用：加载词典。
         */
        @Override
        public void loadDictionary(DictionaryType dictionaryType) {
            this.loadedDictionaryType = dictionaryType;
        }

        /**
         * 方法作用：获取词典。
         */
        @Override
        public Set<String> getDictionary() {
            return Collections.singleton("BOOK");
        }

        /**
         * 方法作用：获取已加载词典类型。
         */
        @Override
        public DictionaryType getLoadedDictionaryType() {
            return loadedDictionaryType;
        }

        /**
         * 方法作用：判断是否Accepted。
         */
        @Override
        public boolean isAccepted(String word) {
            return "BOOK".equalsIgnoreCase(word);
        }
    }
}
