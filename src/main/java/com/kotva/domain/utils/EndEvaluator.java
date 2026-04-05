package com.kotva.domain.utils;

import com.kotva.application.result.GameEndReason;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;


/**
 * 结束判定器（裁判员）
 * 负责在每次玩家动作后，检查是否满足游戏结束的条件。
 */
public final class EndEvaluator {

    private static final int TARGET_SCORE = 100;

    private EndEvaluator() {
        // 工具类，私有化构造方法
    }

    /**
     * 评估当前游戏状态是否应该结束
     *
     * @param state 当前游戏状态
     * @return 如果满足结束条件，返回对应的 GameEndReason；如果未结束，返回 null
     */
    public static GameEndReason evaluate(GameState state) {

        // 1. 检查是否有人达到目标分数（最常见的情况优先判断）
        for (Player p : state.getPlayers()) {
            if (p.getScore() >= TARGET_SCORE) {
                return GameEndReason.TARGET_SCORE_REACHED;
            }
        }

        // 2. 检查是否只剩下一名活跃玩家（如其他玩家认输或超时）
        if (state.getPlayers().size() > 1 && state.getActivePlayerCount() <= 1) {
            return GameEndReason.ONLY_ONE_PLAYER_REMAINING;
        }

        // 3. 检查棋盘是否全满
        if (state.getBoard().isFull()) {
            return GameEndReason.BOARD_FULL;
        }

        // 4. 检查是否全员连续跳过
        if (state.getPlayers().size() > 1 && state.getConsecutivePasses() >= state.getActivePlayerCount()) {
            return GameEndReason.ALL_PLAYERS_PASSED;
        }

        // 所有条件均未满足，游戏继续
        return null;
    }
}