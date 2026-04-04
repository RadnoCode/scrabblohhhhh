package com.kotva.domain;

import java.util.ArrayList;
import java.util.List;
import com.kotva.application.PlayerAction;
import com.kotva.application.draft.DraftPlacement;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Position;
import com.kotva.domain.utils.MoveValidator;

public class RuleEngine {

    // 👈 参数换成了 PlayerAction
    public String validateMove(GameState state, PlayerAction action) {

        // 🚨 前置拦截：法官只管“落子”动作，如果玩家是点“跳过”或“认输”，直接放行
        if (action.type() != com.kotva.policy.ActionType.PLACE_TILE) {
            return null;
        }

        Board board = state.getBoard();

        // 💡 从队友的 record 里把坐标提取出来
        // 队友把新牌都放在了 draft (草稿) 里
        List<Position> placements = new ArrayList<>();
        if (action.draft() != null && action.draft().getPlacements() != null) {
            for (DraftPlacement dp : action.draft().getPlacements()) {
                placements.add(dp.getPosition());
            }
        }

        // --- 1. 基础物理安检 ---
        if (!MoveValidator.isStraightLine(placements)) {
            return "Letters shall be in a line";
        }
        if (!MoveValidator.isNotOverlapping(placements, board)) {
            return "Cannot place tiles on occupied squares";
        }

        // --- 2. 阶段性安检 ---
        if (board.isEmpty()) {
            if (!MoveValidator.firstMove(placements)) {
                return "First word shall sit on the center";
            }
        } else {
            if (!MoveValidator.isConnected(placements, board)) {
                return "New word must connect to existing tiles";
            }
        }

        return null;
    }
}
//未完待续，这上面的还没看懂