package com.kotva.application.service;

import java.util.List;
import java.util.Objects;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.draft.TurnDraftActionMapper;
import com.kotva.application.preview.BoardHighlight;
import com.kotva.application.preview.HighlightType;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.domain.RuleEngine;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.utils.CandidateWord;
import com.kotva.domain.utils.ScoreCalculator;
import com.kotva.domain.utils.WordExtractor;
import com.kotva.infrastructure.dictionary.DictionaryRepository;

/**
 * MovePreviewServiceImpl is responsible for providing a preview of the player's move based on the current turn draft.
 * It uses the RuleEngine to validate the move and calculate the estimated score, and formulates user-friendly messages for any rule violations.
 */
public class MovePreviewServiceImpl implements MovePreviewService
{
    private final RuleEngine ruleEngine;

    public MovePreviewServiceImpl(DictionaryRepository dictionaryRepository) {
        Objects.requireNonNull(dictionaryRepository, "dictionaryRepository cannot be null.");
        this.ruleEngine = new RuleEngine(dictionaryRepository);
    }

    @Override
    public PreviewResult preview(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        GameState gameState = session.getGameState();
        Player currentPlayer = gameState.requireCurrentActivePlayer();
        TurnDraft turnDraft = session.getTurnDraft();

        PlayerAction action =
            TurnDraftActionMapper.toPlaceAction(currentPlayer.getPlayerId(), turnDraft);

        String validationMessage = validateSafely(gameState, action);

        // according to the RuleEngine contract, validationMessage is null when the move is valid; if not valid, it contains a user-friendly message describing why
        boolean valid = validationMessage == null;

        int estimatedScore = valid ? calculateEstimatedScore(gameState, action) : 0;

        List<String> messages = valid
                ? List.of()
                : List.of(mapToUserFriendlyMessage(validationMessage));

        List<BoardHighlight> highlights = buildHighlights(turnDraft, valid);
        
        // todo: word list
        return new PreviewResult(valid, estimatedScore, List.of(), highlights, messages);
    }

    
    private String validateSafely(GameState gameState, PlayerAction action) {
        try {
            return ruleEngine.validateMove(gameState, action);
        } catch (RuntimeException ex) {
            if (ex.getMessage() == null || ex.getMessage().isBlank()) {
                return "Preview failed due to unexpected error.";
            }
            return ex.getMessage();
        }
    }

    private int calculateEstimatedScore(GameState gameState, PlayerAction action) {
        List<CandidateWord> words =
                WordExtractor.extract(action, gameState.getTileBag(), gameState.getBoard());
        return ScoreCalculator.calculate(words, gameState, action);
    }


    private String mapToUserFriendlyMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return "Invalid placement.";
        }
        
        if (rawMessage.startsWith("Invalid word:")) {
            String word = rawMessage.substring("Invalid word:".length()).trim();
            return word + " is not a valid word.";
        }

        return rawMessage;
    }

    private List<BoardHighlight> buildHighlights(
            TurnDraft turnDraft, 
            boolean valid
        ) {

        List<BoardHighlight> highlights = new java.util.ArrayList<>();
    
        // if turnDraft or placements are null, return empty highlights (but this should not happen in normal flow, as the UI should always provide a TurnDraft with placements when calling preview)
        if (turnDraft == null || turnDraft.getPlacements() == null) {
            return highlights;
        }

        for (DraftPlacement placement : turnDraft.getPlacements()) {
            if (placement == null || placement.getPosition() == null) {
                continue;
            }

            highlights.add(new BoardHighlight(
                    placement.getPosition(),
                    HighlightType.NEW_TILE));
    
            if (valid) {
                highlights.add(new BoardHighlight(
                        placement.getPosition(),
                        HighlightType.VALID_TILE));
            } else {
                highlights.add(new BoardHighlight(
                        placement.getPosition(),
                        HighlightType.INVALID_TILE
                        ));
            }
        }
    
        return highlights;
    }

}
