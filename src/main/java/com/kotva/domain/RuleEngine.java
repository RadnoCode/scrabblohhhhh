package com.kotva.domain;

import java.util.ArrayList;
import java.util.List;
import com.kotva.application.PlayerAction;
import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.TileBag;
import com.kotva.domain.utils.CandidateWord;
import com.kotva.domain.utils.MoveValidator;
import com.kotva.domain.utils.WordExtractor;
import com.kotva.infrastructure.dictionary.DictionaryRepository;

/**
 * 规则引擎类
 * 负责统筹落子校验（物理规则）、单词提取和字典合法性校验。
 */
public class RuleEngine {

    // 字典库，用于校验提取出单词的合法性
    private final DictionaryRepository dictionaryRepository;

    // 通过构造方法注入已加载的字典库依赖
    public RuleEngine(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    /**
     * 校验玩家动作的合法性
     * * @param state 当前游戏状态
     * @param action 玩家执行的动作
     * @return 如果合法返回 null，如果违规返回具体的错误提示信息
     */
    public String validateMove(GameState state, PlayerAction action) {

        // 仅处理落子(PLACE_TILE)动作，跳过或认输等动作直接放行
        if (action.type() != com.kotva.policy.ActionType.PLACE_TILE) {
            return null;
        }

        Board board = state.getBoard();

        // 提取本次落子的所有目标坐标
        List<Position> placements = new ArrayList<>();
        if (action.draft() != null && action.draft().getPlacements() != null) {
            for (DraftPlacement dp : action.draft().getPlacements()) {
                placements.add(dp.getPosition());
            }
        }

        // --- 1. 基础物理规则校验 ---
        if (!MoveValidator.isStraightLine(placements)) {
            return "Letters shall be in a line";
        }
        if (!MoveValidator.isNotOverlapping(placements, board)) {
            return "Cannot place tiles on occupied squares";
        }

        // --- 2. 棋盘连接性及首回合校验 ---
        if (board.isEmpty()) {
            if (!MoveValidator.firstMove(placements)) {
                return "First word shall sit on the center";
            }
        } else {
            if (!MoveValidator.isConnected(placements, board)) {
                return "New word must connect to existing tiles";
            }
        }

        // --- 3. 提取新组成的所有候选单词 ---
        TurnDraft draft = action.draft();
        TileBag tilebag = state.getTileBag();
        List<CandidateWord> words = WordExtractor.extract(draft, tilebag, board);

        // --- 4. 字典合法性校验 ---
        for (CandidateWord candidate : words) {
            String wordString = candidate.getWord();

            // 若组成的单词不在字典中，立刻拦截并返回带有违规词汇的错误信息
            if (!dictionaryRepository.isAccepted(wordString)) {
                return "Invalid word: " + wordString;
            }
        }

        // 所有安检全部通过
        return null;
    }
}