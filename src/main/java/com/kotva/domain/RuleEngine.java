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
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.Rack;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.utils.CandidateWord;
import com.kotva.domain.utils.MoveValidator;
import com.kotva.domain.utils.WordExtractor;
import com.kotva.infrastructure.dictionary.DictionaryRepository;

/**
 * 规则引擎类
 * 负责统筹落子校验（物理规则）、字典合法性校验以及落子生效动作。
 */
public class RuleEngine {

    private final DictionaryRepository dictionaryRepository;

    public RuleEngine(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    /**
     * 校验玩家动作的合法性
     *
     * @param state  当前游戏状态
     * @param action 玩家执行的动作
     * @return 如果合法返回 null，如果违规返回具体的错误提示信息
     */
    public String validateMove(GameState state, PlayerAction action) {

        if (action.type() != com.kotva.policy.ActionType.PLACE_TILE) {
            return null;
        }

        if (action.draft() == null || action.draft().getPlacements() == null || action.draft().getPlacements().isEmpty()) {
            return "No tiles placed";
        }

        Board board = state.getBoard();
        List<Position> placements = new ArrayList<>();

        for (DraftPlacement dp : action.draft().getPlacements()) {
            placements.add(dp.getPosition());
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
            if (!dictionaryRepository.isAccepted(wordString)) {
                return "Invalid word: " + wordString;
            }
        }

        return null;
    }
    /**
     * 盖章生效：根据玩家的具体动作（落子/跳过/认输），正式变更游戏状态
     *
     * @param state  当前游戏状态
     * @param action 玩家执行的动作
     */
    public void apply(GameState state, PlayerAction action) {

        // 1. 先拿到当前正在操作的玩家
        com.kotva.domain.model.Player currentPlayer = state.getCurrentPlayer();

        // 2. 根据玩家选择的动作类型，执行不同的状态变更
        switch (action.type()) {

            case PLACE_TILE:
                // --- 动作 A：正常落子 ---
                Board board = state.getBoard();
                TileBag tileBag = state.getTileBag();
                Rack rack = currentPlayer.getRack();

                for (DraftPlacement dp : action.draft().getPlacements()) {
                    String tileId = dp.getTileId();
                    Position pos = dp.getPosition();

                    // 焊死在棋盘上
                    Tile realTile = tileBag.getTileById(tileId);
                    Cell cell = board.getCell(pos);
                    cell.setPlacedTile(realTile);

                    // 从手牌架扣除
                    for (RackSlot slot : rack.getSlots()) {
                        if (!slot.isEmpty() && slot.getTile().getTileID().equals(tileId)) {
                            rack.setTileAt(slot.getIndex(), null);
                            break;
                        }
                    }
                }
                //  关键点：只要有人正常落子，全场的“连续跳过计数器”必须清零！
                state.resetPasses();
                break;

            case PASS_TURN:
                // --- 动作 B：跳过当前回合 ---
                // 只需要给跳过计数器 + 1
                state.incrementPass();
                break;

            case LOSE:
                // --- 动作 C：玩家认输或超时失败 ---
                 currentPlayer.setActive(false);
                break;
        }
    }

}