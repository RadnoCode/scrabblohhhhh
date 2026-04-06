package com.kotva.application.service;

import java.util.List;
import java.util.Objects;

import com.kotva.application.PlayerAction;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.domain.RuleEngine;
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
        TurnDraft turnDraft = session.getTurnDraft();  //fetch current turn draft from session

        // creeate a PlayerAction based on the current turn draft for validation
        String playerId = currentPlayer.getPlayerId();
        TurnDraft draft = turnDraft;
        PlayerAction action = PlayerAction.place(playerId, draft);

        //get validation result from rule engine; if valid, also get estimated score and messages for frontend display
        String validationMessage = validateSafely(gameState, action);

        // according to the RuleEngine contract, validationMessage is null when the move is valid; if not valid, it contains a user-friendly message describing why
        boolean valid = validationMessage == null;

        // estimate the score for this move if it's valid; if not valid, estimated score is 0
        int estimatedScore = valid ? calculateEstimatedScore(gameState, turnDraft) : 0;

        // formulate messsage from raw validation message; if valid, messages list is empty; if not valid, messages list contains one user-friendly message
        List<String> messages = valid
                ? List.of()
                : List.of(mapToUserFriendlyMessage(validationMessage));

        // todo: words and highlights are not implemented yet, will be added in the next iteration; for now just return empty list
        return new PreviewResult(valid, estimatedScore, List.of(), List.of(), messages);
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

    private int calculateEstimatedScore(GameState gameState, TurnDraft turnDraft) {
        List<CandidateWord> words =
                WordExtractor.extract(turnDraft, gameState.getTileBag(), gameState.getBoard());
        return ScoreCalculator.calculate(words, gameState, turnDraft);
    }


    private String mapToUserFriendlyMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return "Invalid placement.";
        }

        // 示例：把 Invalid word: XXX 映射成需求里的文案风格
        if (rawMessage.startsWith("Invalid word:")) {
            String word = rawMessage.substring("Invalid word:".length()).trim();
            return word + " It is not a word";
        }

        // 其他消息先原样透传
        return rawMessage;
    }
}
