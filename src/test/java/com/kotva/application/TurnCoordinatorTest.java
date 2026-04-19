/**
 * 包作用：应用层测试包，负责验证动作协调与回合推进逻辑。
 * 包含类：TurnCoordinatorTest。
 */
package com.kotva.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.application.result.BoardSnapshot;
import com.kotva.application.result.SettlementResult;
import com.kotva.application.service.SettlementService;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.policy.PlayerType;
import java.util.List;
import org.junit.Test;

/**
 * 类作用：测试回合协调器的终局判断与轮次推进逻辑。
 * 包含方法：loseDoesNotEndGameWhenMultipleActivePlayersRemain、loseEndsGameWhenOnlyOneActivePlayerRemains、allPlayersPassingEndsGameAtRoundBoundary、emptyTileBagAndEmptyRackEndsGameAfterPlaceTileAction、createPlayer。
 * 继承/实现：无。
 * 引用类：TurnDraft 用于保存当前回合草稿数据；GameEndReason 用于标识对局结束原因；GameState 用于访问或更新当前对局状态；Player 用于访问玩家对象、分数、行动权或牌架；Tile 用于访问字牌字母、分值和空白牌状态；PlayerController 用于接收并产出玩家动作；PlayerType 用于区分玩家控制器类型；Test 用于标记测试方法。
 */
public class TurnCoordinatorTest {
    /**
     * 方法作用：测试方法：验证 loseDoesNotEndGameWhenMultipleActivePlayersRemain 对应的业务场景。
     */
    @Test
    public void loseDoesNotEndGameWhenMultipleActivePlayersRemain() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        Player third = createPlayer("p3", "Cleo");
        GameState gameState = new GameState(List.of(first, second, third));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);
        first.setActive(false);

        coordinator.onActionApplied(PlayerAction.lose("p1"));

        assertFalse(coordinator.isGameEnded());
        assertFalse(gameState.isGameOver());
        assertFalse(first.getActive());
        assertEquals("p2", gameState.requireCurrentActivePlayer().getPlayerId());
        assertEquals(1, coordinator.getTurnNumber());
        assertEquals(0, settlementService.callCount);
    }

    /**
     * 方法作用：测试方法：验证 loseEndsGameWhenOnlyOneActivePlayerRemains 对应的业务场景。
     */
    @Test
    public void loseEndsGameWhenOnlyOneActivePlayerRemains() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);
        first.setActive(false);

        SettlementResult settlementResult = coordinator.onActionApplied(PlayerAction.lose("p1"));

        assertTrue(coordinator.isGameEnded());
        assertTrue(gameState.isGameOver());
        assertEquals(GameEndReason.ONLY_ONE_PLAYER_REMAINING, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
        assertNotNull(settlementResult);
    }

    /**
     * 方法作用：测试方法：验证 allPlayersPassingEndsGameAtRoundBoundary 对应的业务场景。
     */
    @Test
    public void allPlayersPassingEndsGameAtRoundBoundary() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);

        coordinator.onActionApplied(PlayerAction.pass("p1"));
        assertFalse(coordinator.isGameEnded());
        assertEquals("p2", gameState.requireCurrentActivePlayer().getPlayerId());

        SettlementResult settlementResult = coordinator.onActionApplied(PlayerAction.pass("p2"));

        assertTrue(coordinator.isGameEnded());
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
        assertNotNull(settlementResult);
    }

    /**
     * 方法作用：测试方法：验证 emptyTileBagAndEmptyRackEndsGameAfterPlaceTileAction 对应的业务场景。
     */
    @Test
    public void emptyTileBagAndEmptyRackEndsGameAfterPlacedAction() {
        RecordingSettlementService settlementService = new RecordingSettlementService();
        Player first = createPlayer("p1", "Alice");
        Player second = createPlayer("p2", "Bob");
        GameState gameState = new GameState(List.of(first, second));
        TurnCoordinator coordinator = new TurnCoordinator(gameState, settlementService);

        while (!gameState.getTileBag().isEmpty()) {
            gameState.getTileBag().drawTile();
        }

        SettlementResult settlementResult = coordinator.onActionApplied(PlayerAction.place("p1", List.of()));

        assertTrue(coordinator.isGameEnded());
        assertEquals(
                GameEndReason.TILE_BAG_EMPTY_AND_PLAYER_FINISHED, gameState.getGameEndReason());
        assertEquals(1, settlementService.callCount);
        assertNotNull(settlementResult);
    }

    /**
     * 方法作用：创建一个测试用玩家对象。
     */
    private Player createPlayer(String playerId, String playerName) {
        return new Player(playerId, playerName, PlayerType.LOCAL);
    }

    /**
     * 类作用：测试替身类，用于记录结算服务调用结果。
     * 包含方法：settle。
     * 继承/实现：实现 SettlementService。
     * 引用类：当前类未直接导入其他自定义类。
     */
    private static class RecordingSettlementService implements SettlementService {
        private int callCount;

        /**
         * 方法作用：根据终局状态生成并返回结算结果。
         */
        @Override
        public SettlementResult settle(GameState gameState, GameEndReason endReason) {
            callCount++;
            return new SettlementResult(endReason, List.of(), List.of(), new BoardSnapshot(List.of()));
        }
    }
}
