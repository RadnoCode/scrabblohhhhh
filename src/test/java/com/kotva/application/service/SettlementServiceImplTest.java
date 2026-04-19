/**
 * 包作用：应用层服务测试包，负责验证计时、开局与结算服务行为。
 * 包含类：ClockServiceImplTest、GameSetupServiceImplTest、SettlementServiceImplTest。
 */
package com.kotva.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.kotva.application.result.BoardCellSnapshot;
import com.kotva.application.result.PlayerSettlement;
import com.kotva.application.result.SettlementResult;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Tile;
import com.kotva.mode.PlayerController;
import com.kotva.policy.BonusType;
import com.kotva.policy.PlayerType;
import java.util.List;
import org.junit.Test;

/**
 * 类作用：测试结算服务实现的结果生成行为。
 * 包含方法：settlementIncludesRankingNamesReasonAndBoardSnapshot、createPlayer、assertFalseOrMissingBlank。
 * 继承/实现：无。
 * 引用类：BoardCellSnapshot 用于封装单格快照；GameEndReason 用于标识对局结束原因；PlayerSettlement 用于封装玩家结算结果；GameState 用于访问或更新当前对局状态；Player 用于访问玩家对象、分数、行动权或牌架；Tile 用于访问字牌字母、分值和空白牌状态；PlayerController 用于接收并产出玩家动作；BonusType 用于识别奖励格类型；PlayerType 用于区分玩家控制器类型；Test 用于标记测试方法。
 */
public class SettlementServiceImplTest {
    /**
     * 方法作用：测试方法：验证 settlementIncludesRankingNamesReasonAndBoardSnapshot 对应的业务场景。
     */
    @Test
    public void settlementIncludesRankingNamesReasonAndBoardSnapshot() {
        Player alice = createPlayer("p1", "Alice");
        Player bob = createPlayer("p2", "Bob");
        Player carol = createPlayer("p3", "Carol");
        alice.addScore(20);
        bob.addScore(20);
        carol.addScore(15);

        GameState gameState = new GameState(List.of(alice, bob, carol));
        gameState.getBoard()
                .getCell(new com.kotva.domain.model.Position(7, 7))
                .setPlacedTile(new Tile("tile-1", 'A', 1, false));

        RecordingNavigationPort navigationPort = new RecordingNavigationPort();
        SettlementServiceImpl settlementService = new SettlementServiceImpl(navigationPort);

        SettlementResult result =
                settlementService.settle(gameState, GameEndReason.ALL_PLAYERS_PASSED);

        assertSame(result, navigationPort.lastResult);
        assertEquals(GameEndReason.ALL_PLAYERS_PASSED, result.getEndReason());
        assertEquals(3, result.getRankings().size());
        assertEquals("Alice", result.getRankings().get(0).getPlayerName());
        assertEquals("Bob", result.getRankings().get(1).getPlayerName());
        assertEquals(1, result.getRankings().get(0).getRank());
        assertEquals(1, result.getRankings().get(1).getRank());
        assertEquals(3, result.getRankings().get(2).getRank());
        assertEquals(225, result.getBoardSnapshot().getCells().size());
        BoardCellSnapshot centerCell =
                result.getBoardSnapshot().getCells().stream()
                        .filter(cell -> cell.getRow() == 7 && cell.getCol() == 7)
                        .findFirst()
                        .orElseThrow();
        assertEquals(Character.valueOf('A'), centerCell.getLetter());
        assertEquals(BonusType.DOUBLE_WORD, centerCell.getBonusType());
        assertFalseOrMissingBlank(centerCell.isBlank());
        assertNotNull(result.getSummaryMessages());
        assertTrue(
                result.getSummaryMessages().stream()
                        .anyMatch(message -> message.contains("Shared first place")));
    }

    /**
     * 方法作用：创建一个测试用玩家对象。
     */
    private Player createPlayer(String playerId, String playerName) {
        Player player = new Player(playerId, playerName, PlayerType.LOCAL);
        player.setController(new PlayerController(playerId, PlayerType.LOCAL));
        return player;
    }

    /**
     * 方法作用：校验空白牌标记是否符合预期。
     */
    private void assertFalseOrMissingBlank(boolean blank) {
        assertTrue(!blank);
    }

    /**
     * 类作用：测试替身类，用于记录结算展示调用结果。
     * 包含方法：showSettlement。
     * 继承/实现：实现 SettlementNavigationPort。
     * 引用类：当前类未直接导入其他自定义类。
     */
    private static class RecordingNavigationPort implements SettlementNavigationPort {
        private SettlementResult lastResult;

        /**
         * 方法作用：展示或记录结算结果。
         */
        @Override
        public void showSettlement(SettlementResult result) {
            this.lastResult = result;
        }
    }
}
