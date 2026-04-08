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

public class RuleEngine {

    private final DictionaryRepository dictionaryRepository;

    public RuleEngine(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    public String validateMove(GameState state, PlayerAction action) {

        if (action.type() != com.kotva.policy.ActionType.PLACE_TILE) {
            return null;
        }
        if(action.draft() == null && action.draft().getPlacements() == null){
            return "No tiles placed";
        }
        Board board = state.getBoard();

        List<Position> placements = new ArrayList<>();

            for (DraftPlacement dp : action.draft().getPlacements()) {
                placements.add(dp.getPosition());
            }


        if (!MoveValidator.isStraightLine(placements)) {
            return "Letters shall be in a line";
        }
        if (!MoveValidator.isNotOverlapping(placements, board)) {
            return "Cannot place tiles on occupied squares";
        }

        if (board.isEmpty()) {
            if (!MoveValidator.firstMove(placements)) {
                return "First word shall sit on the center";
            }
        } else {
            if (!MoveValidator.isConnected(placements, board)) {
                return "New word must connect to existing tiles";
            }
        }

        TurnDraft draft = action.draft();
        TileBag tilebag = state.getTileBag();
        List<CandidateWord> words = WordExtractor.extract(draft, tilebag, board);

        for (CandidateWord candidate : words) {
            String wordString = candidate.getWord();

            if (!dictionaryRepository.isAccepted(wordString)) {
                return "Invalid word: " + wordString;
            }
        }

        return null;
    }
}