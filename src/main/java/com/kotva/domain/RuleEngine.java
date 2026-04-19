package com.kotva.domain;

import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.Cell;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Rack;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.domain.utils.CandidateWord;
import com.kotva.domain.utils.MoveValidator;
import com.kotva.domain.utils.WordExtractor;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import java.util.ArrayList;
import java.util.List;

public class RuleEngine {
    private final DictionaryRepository dictionaryRepository;

    public RuleEngine(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    public String validateMove(GameState state, PlayerAction action) {
        if (action.type() != ActionType.PLACE_TILE) {
            return null;
        }

        if (action.placements().isEmpty()) {
            return "No tiles placed";
        }

        Board board = state.getBoard();
        List<Position> placements = new ArrayList<>();
        for (ActionPlacement placement : action.placements()) {
            placements.add(placement.position());
        }

        if (!MoveValidator.isStraightLine(placements)) {
            return "Letters shall be in a line";
        }
        if (!MoveValidator.isNotOverlapping(placements, board)) {
            return "Cannot place tiles on occupied squares";
        }
        if (!MoveValidator.isContiguous(placements, board)) {
            return "Letters shall be contiguous";
        }

        if (board.isEmpty()) {
            if (!MoveValidator.firstMove(placements)) {
                return "First word shall sit on the center";
            }
        } else if (!MoveValidator.isConnected(placements, board)) {
            return "New word must connect to existing tiles";
        }

        TileBag tilebag = state.getTileBag();
        List<CandidateWord> words = WordExtractor.extract(action, tilebag, board);
        for (CandidateWord candidate : words) {
            if (!dictionaryRepository.isAccepted(candidate.getWord())) {
                return "Invalid word: " + candidate.getWord();
            }
        }

        return null;
    }

    public void apply(GameState state, PlayerAction action) {
        com.kotva.domain.model.Player currentPlayer = state.getCurrentPlayer();

        switch (action.type()) {
            case PLACE_TILE:
                Board board = state.getBoard();
                TileBag tileBag = state.getTileBag();
                Rack rack = currentPlayer.getRack();

                for (ActionPlacement placement : action.placements()) {
                    String tileId = placement.tileId();
                    Position pos = placement.position();

                    Tile realTile = tileBag.getTileById(tileId);
                    Cell cell = board.getCell(pos);
                    cell.setPlacedTile(realTile);
                    if (realTile != null && realTile.isBlank()) {
                        realTile.markFixed();
                    }

                    for (RackSlot slot : rack.getSlots()) {
                        if (!slot.isEmpty() && slot.getTile().getTileID().equals(tileId)) {
                            rack.setTileAt(slot.getIndex(), null);
                            break;
                        }
                    }
                }
                break;

            case PASS_TURN:
                break;

            case LOSE:
                currentPlayer.setActive(false);
                break;
        }
    }
}
