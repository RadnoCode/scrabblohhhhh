package com.kotva.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.draft.TurnDraftActionMapper;
import com.kotva.application.preview.BoardHighlight;
import com.kotva.application.preview.HighlightType;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.preview.PreviewWord;
import com.kotva.application.session.GameSession;
import com.kotva.domain.RuleEngine;
import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Board;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.utils.CandidateWord;
import com.kotva.domain.utils.MoveValidator;
import com.kotva.domain.utils.ScoreCalculator;
import com.kotva.domain.utils.WordExtractor;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.WordType;

public class MovePreviewServiceImpl implements MovePreviewService
{
    private static final String BLANK_TILE_SELECTION_REQUIRED_MESSAGE =
        "Invalid placement. Please hover over the blank tile and choose a letter first.";

    private final DictionaryRepository dictionaryRepository;
    private final RuleEngine ruleEngine;

    public MovePreviewServiceImpl(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository =
        Objects.requireNonNull(
            dictionaryRepository, "dictionaryRepository cannot be null.");
        this.ruleEngine = new RuleEngine(this.dictionaryRepository);
    }

    @Override
    public PreviewResult preview(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        GameState gameState = session.getGameState();
        Player currentPlayer = gameState.requireCurrentActivePlayer();
        return preview(
                gameState,
                session.getConfig().getDictionaryType(),
                currentPlayer.getPlayerId(),
                session.getTurnDraft());
    }

    @Override
    public PreviewResult preview(
            GameState gameState,
            DictionaryType dictionaryType,
            String playerId,
            TurnDraft turnDraft) {
        Objects.requireNonNull(gameState, "gameState cannot be null.");
        Objects.requireNonNull(dictionaryType, "dictionaryType cannot be null.");
        Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");
        ensureDictionaryLoaded(dictionaryType);

        if (hasUnassignedBlankTile(gameState, turnDraft)) {
            return new PreviewResult(
                false,
                0,
                List.of(),
                buildHighlights(turnDraft, false),
                List.of(BLANK_TILE_SELECTION_REQUIRED_MESSAGE));
        }

        Player previewPlayer = resolvePreviewPlayer(gameState, playerId);
        PlayerAction action =
                TurnDraftActionMapper.toPlaceAction(previewPlayer.getPlayerId(), turnDraft);

        String validationMessage = validateSafely(gameState, action);

        boolean valid = validationMessage == null;

        List<CandidateWord> candidateWords = extractCandidateWords(gameState, action);
        int estimatedScore = valid ? ScoreCalculator.calculate(candidateWords, gameState, action) : 0;

        List<String> messages = valid
        ? List.of()
        : List.of(mapToUserFriendlyMessage(validationMessage));

        List<PreviewWord> words = buildPreviewWords(gameState, action, candidateWords);
        List<BoardHighlight> highlights = buildHighlights(turnDraft, valid);

        return new PreviewResult(valid, estimatedScore, words, highlights, messages);
    }

    private void ensureDictionaryLoaded(DictionaryType dictionaryType) {
        if (dictionaryRepository.getLoadedDictionaryType() != dictionaryType) {
            dictionaryRepository.loadDictionary(dictionaryType);
        }
    }

    private Player resolvePreviewPlayer(GameState gameState, String playerId) {
        if (playerId != null) {
            Player player = gameState.getPlayerById(playerId);
            if (player != null) {
                return player;
            }
        }
        return gameState.requireCurrentActivePlayer();
    }

    private boolean hasUnassignedBlankTile(GameState gameState, TurnDraft turnDraft) {
        for (DraftPlacement placement : turnDraft.getPlacements()) {
            if (placement == null) {
                continue;
            }
            var tile = gameState.getTileBag().getTileById(placement.getTileId());
            if (tile != null
                && tile.isBlank()
                && tile.getAssignedLetter() == null
                && placement.getAssignedLetter() == null) {
                return true;
            }
        }
        return false;
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

    private List<CandidateWord> extractCandidateWords(GameState gameState, PlayerAction action) {
        return WordExtractor.extract(action, gameState.getTileBag(), gameState.getBoard());
    }

    private List<PreviewWord> buildPreviewWords(
        GameState gameState, PlayerAction action, List<CandidateWord> candidateWords) {
        if (!canBuildPreviewWords(gameState, action) || candidateWords.isEmpty()) {
            return List.of();
        }

        CandidateWord mainWord = resolveMainWord(candidateWords, action);
        List<PreviewWord> words = new ArrayList<>();
        if (mainWord != null) {
            words.add(buildPreviewWord(mainWord, WordType.MAIN_WORD, gameState, action));
        }

        for (CandidateWord candidateWord : candidateWords) {
            if (isSameWord(candidateWord, mainWord)) {
                continue;
            }
            words.add(buildPreviewWord(candidateWord, WordType.CROSS_WORD, gameState, action));
        }
        return words;
    }

    private boolean canBuildPreviewWords(GameState gameState, PlayerAction action) {
        if (action.placements().isEmpty()) {
            return false;
        }

        List<Position> placements = new ArrayList<>(action.placements().size());
        for (ActionPlacement placement : action.placements()) {
            placements.add(placement.position());
        }

        Board board = gameState.getBoard();
        return MoveValidator.isStraightLine(placements)
        && MoveValidator.isNotOverlapping(placements, board)
        && MoveValidator.isContiguous(placements, board);
    }

    private CandidateWord resolveMainWord(List<CandidateWord> candidateWords, PlayerAction action) {
        if (candidateWords.isEmpty()) {
            return null;
        }
        if (action.placements().size() == 1) {
            return resolveSingleTileMainWord(candidateWords);
        }

        ActionPlacement firstPlacement = action.placements().get(0);
        boolean horizontal = true;
        int minRow = firstPlacement.position().getRow();
        int maxRow = minRow;
        int minCol = firstPlacement.position().getCol();
        int maxCol = minCol;
        for (ActionPlacement placement : action.placements()) {
            Position position = placement.position();
            if (position.getRow() != firstPlacement.position().getRow()) {
                horizontal = false;
            }
            minRow = Math.min(minRow, position.getRow());
            maxRow = Math.max(maxRow, position.getRow());
            minCol = Math.min(minCol, position.getCol());
            maxCol = Math.max(maxCol, position.getCol());
        }

        CandidateWord selectedMainWord = null;
        for (CandidateWord candidateWord : candidateWords) {
            if (horizontal) {
                if (candidateWord.getStartPosition().getRow() != firstPlacement.position().getRow()
                    || candidateWord.getEndPosition().getRow() != firstPlacement.position().getRow()) {
                    continue;
                }
                if (candidateWord.getStartPosition().getCol() > minCol
                    || candidateWord.getEndPosition().getCol() < maxCol) {
                    continue;
                }
            } else {
                if (candidateWord.getStartPosition().getCol() != firstPlacement.position().getCol()
                    || candidateWord.getEndPosition().getCol() != firstPlacement.position().getCol()) {
                    continue;
                }
                if (candidateWord.getStartPosition().getRow() > minRow
                    || candidateWord.getEndPosition().getRow() < maxRow) {
                    continue;
                }
            }

            if (selectedMainWord == null || wordLength(candidateWord) > wordLength(selectedMainWord)) {
                selectedMainWord = candidateWord;
            }
        }
        return selectedMainWord != null ? selectedMainWord : candidateWords.get(0);
    }

    private CandidateWord resolveSingleTileMainWord(List<CandidateWord> candidateWords) {
        if (candidateWords.size() == 1) {
            return candidateWords.get(0);
        }

        CandidateWord horizontalWord = null;
        CandidateWord verticalWord = null;
        for (CandidateWord candidateWord : candidateWords) {
            if (candidateWord.getStartPosition().getRow() == candidateWord.getEndPosition().getRow()) {
                if (horizontalWord == null || wordLength(candidateWord) > wordLength(horizontalWord)) {
                    horizontalWord = candidateWord;
                }
            } else if (candidateWord.getStartPosition().getCol()
                == candidateWord.getEndPosition().getCol()) {
                if (verticalWord == null || wordLength(candidateWord) > wordLength(verticalWord)) {
                    verticalWord = candidateWord;
                }
            }
        }

        if (horizontalWord == null) {
            return verticalWord != null ? verticalWord : candidateWords.get(0);
        }
        if (verticalWord == null) {
            return horizontalWord;
        }
        return wordLength(horizontalWord) >= wordLength(verticalWord) ? horizontalWord : verticalWord;
    }

    private PreviewWord buildPreviewWord(
        CandidateWord candidateWord,
        WordType wordType,
        GameState gameState,
        PlayerAction action) {
        String word = candidateWord.getWord();
        boolean valid = word.length() < 2 || dictionaryRepository.isAccepted(word);
        int scoreContribution = ScoreCalculator.calculateWordScore(candidateWord, gameState, action);
        return new PreviewWord(
            word,
            valid,
            scoreContribution,
            buildCoveredPositions(candidateWord),
            wordType);
    }

    private List<Position> buildCoveredPositions(CandidateWord candidateWord) {
        List<Position> coveredPositions = new ArrayList<>();
        int startRow = candidateWord.getStartPosition().getRow();
        int endRow = candidateWord.getEndPosition().getRow();
        int startCol = candidateWord.getStartPosition().getCol();
        int endCol = candidateWord.getEndPosition().getCol();

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                coveredPositions.add(new Position(row, col));
            }
        }
        return coveredPositions;
    }

    private boolean isSameWord(CandidateWord candidateWord, CandidateWord otherWord) {
        return otherWord != null && candidateWord.equals(otherWord);
    }

    private int wordLength(CandidateWord candidateWord) {
        if (candidateWord.getStartPosition().getRow() == candidateWord.getEndPosition().getRow()) {
            return candidateWord.getEndPosition().getCol() - candidateWord.getStartPosition().getCol() + 1;
        }
        return candidateWord.getEndPosition().getRow() - candidateWord.getStartPosition().getRow() + 1;
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
