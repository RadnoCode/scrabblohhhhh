/**
 * 包作用：应用层服务测试包，负责验证计时、开局与结算服务行为。
 * 包含类：ClockServiceImplTest、GameSetupServiceImplTest、SettlementServiceImplTest。
 */
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

/**
 * 类作用：测试计时服务实现的关键行为。
 * 包含方法：tickMovesFromMainTimeIntoByoYomiAndSnapshotReflectsChanges、tickTimeoutEliminatesCurrentPlayerAndEndsTwoPlayerGame、createTimedSession。
 * 继承/实现：无。
 * 引用类：GameEndReason 用于标识对局结束原因；GameSessionSnapshot 用于配合当前类完成与 GameSessionSnapshot 相关的处理；GameSession 用于承载当前会话、配置与对局状态；TimeControlConfig 用于提供时间控制参数；NewGameRequest 用于承接新对局请求参数；Player 用于访问玩家对象、分数、行动权或牌架；TurnDraft 用于保存当前回合草稿数据；DictionaryRepository 用于查询词典是否合法；GameMode 用于配合当前类完成与 GameMode 相关的处理；ClockPhase 用于区分计时阶段；DictionaryType 用于区分词典类型；SessionStatus 用于表示会话状态；Test 用于标记测试方法。
 */
public class ClockServiceImplTest {
    /**
     * 方法作用：测试方法：验证 tickMovesFromMainTimeIntoByoYomiAndSnapshotReflectsChanges 对应的业务场景。
     */
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

    /**
     * 方法作用：测试方法：验证 tickTimeoutEliminatesCurrentPlayerAndEndsTwoPlayerGame 对应的业务场景。
     */
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

    /**
     * 方法作用：创建Timed会话。
     */
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

    /**
     * 类作用：测试替身类，用于在测试中提供可控的词典行为。
     * 包含方法：loadDictionary、getDictionary、getLoadedDictionaryType、isAccepted。
     * 继承/实现：继承 DictionaryRepository。
     * 引用类：当前类未直接导入其他自定义类。
     */
    private static class StubDictionaryRepository extends DictionaryRepository {
        /**
         * 方法作用：加载词典。
         */
        @Override
        public void loadDictionary(DictionaryType dictionaryType) {
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
            return DictionaryType.AM;
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
