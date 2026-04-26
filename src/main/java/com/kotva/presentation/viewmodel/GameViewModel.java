package com.kotva.presentation.viewmodel;

import com.kotva.policy.BonusType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores data for the game screen.
 */
public class GameViewModel {
    private static final int DEFAULT_RACK_SLOT_COUNT = 7;

    private final String titleText;
    private final List<PlayerCardModel> playerCards;
    private final List<TileModel> rackTiles;
    private final List<BoardTileModel> boardTiles;
    private String stepTimerTitle;
    private String stepTimerText;
    private String totalTimerTitle;
    private String totalTimerText;
    private WordOutlineModel wordOutline;
    private boolean interactionLocked;
    private String aiErrorSummary;
    private String aiErrorDetails;
    private String transientMessageText;
    private long transientMessageVersion;
    private PreviewPanelModel previewPanel;
    private TutorialOverlayModel tutorialOverlay;
    private ActionPanelModel actionPanel;

    public GameViewModel(String titleText) {
        this.titleText = Objects.requireNonNull(titleText, "titleText cannot be null.");
        this.playerCards = new ArrayList<>();
        this.rackTiles = new ArrayList<>();
        this.boardTiles = new ArrayList<>();
        this.stepTimerTitle = "Step Time";
        this.stepTimerText = "--:--";
        this.totalTimerTitle = "Total Time";
        this.totalTimerText = "--:--";
        this.wordOutline = null;
        this.interactionLocked = false;
        this.aiErrorSummary = "";
        this.aiErrorDetails = "";
        this.transientMessageText = "";
        this.transientMessageVersion = 0L;
        this.previewPanel = PreviewPanelModel.hidden();
        this.tutorialOverlay = TutorialOverlayModel.hidden();
        this.actionPanel = ActionPanelModel.defaultState();
        resetPlaceholders();
    }

    public String getTitleText() {
        return titleText;
    }

    public List<PlayerCardModel> getPlayerCards() {
        return List.copyOf(playerCards);
    }

    public void setPlayerCards(List<PlayerCardModel> playerCards) {
        this.playerCards.clear();
        this.playerCards.addAll(Objects.requireNonNull(playerCards, "playerCards cannot be null."));
    }

    public List<TileModel> getRackTiles() {
        return List.copyOf(rackTiles);
    }

    public void setRackTiles(List<TileModel> rackTiles) {
        this.rackTiles.clear();
        this.rackTiles.addAll(Objects.requireNonNull(rackTiles, "rackTiles cannot be null."));
    }

    public List<BoardTileModel> getBoardTiles() {
        return List.copyOf(boardTiles);
    }

    public void setBoardTiles(List<BoardTileModel> boardTiles) {
        this.boardTiles.clear();
        this.boardTiles.addAll(Objects.requireNonNull(boardTiles, "boardTiles cannot be null."));
    }

    public String getStepTimerTitle() {
        return stepTimerTitle;
    }

    public void setStepTimerTitle(String stepTimerTitle) {
        this.stepTimerTitle = Objects.requireNonNull(stepTimerTitle, "stepTimerTitle cannot be null.");
    }

    public String getStepTimerText() {
        return stepTimerText;
    }

    public void setStepTimerText(String stepTimerText) {
        this.stepTimerText = Objects.requireNonNull(stepTimerText, "stepTimerText cannot be null.");
    }

    public String getTotalTimerTitle() {
        return totalTimerTitle;
    }

    public void setTotalTimerTitle(String totalTimerTitle) {
        this.totalTimerTitle = Objects.requireNonNull(totalTimerTitle, "totalTimerTitle cannot be null.");
    }

    public String getTotalTimerText() {
        return totalTimerText;
    }

    public void setTotalTimerText(String totalTimerText) {
        this.totalTimerText = Objects.requireNonNull(totalTimerText, "totalTimerText cannot be null.");
    }

    public WordOutlineModel getWordOutline() {
        return wordOutline;
    }

    public void setWordOutline(WordOutlineModel wordOutline) {
        this.wordOutline = wordOutline;
    }

    public boolean isInteractionLocked() {
        return interactionLocked;
    }

    public void setInteractionLocked(boolean interactionLocked) {
        this.interactionLocked = interactionLocked;
    }

    public String getAiErrorSummary() {
        return aiErrorSummary;
    }

    public void setAiErrorSummary(String aiErrorSummary) {
        this.aiErrorSummary = Objects.requireNonNull(aiErrorSummary, "aiErrorSummary cannot be null.");
    }

    public String getAiErrorDetails() {
        return aiErrorDetails;
    }

    public void setAiErrorDetails(String aiErrorDetails) {
        this.aiErrorDetails = Objects.requireNonNull(aiErrorDetails, "aiErrorDetails cannot be null.");
    }

    public String getTransientMessageText() {
        return transientMessageText;
    }

    public long getTransientMessageVersion() {
        return transientMessageVersion;
    }

    public void pushTransientMessage(String transientMessageText) {
        this.transientMessageText =
        Objects.requireNonNull(transientMessageText, "transientMessageText cannot be null.");
        this.transientMessageVersion++;
    }

    public PreviewPanelModel getPreviewPanel() {
        return previewPanel;
    }

    public void setPreviewPanel(PreviewPanelModel previewPanel) {
        this.previewPanel = Objects.requireNonNull(previewPanel, "previewPanel cannot be null.");
    }

    public TutorialOverlayModel getTutorialOverlay() {
        return tutorialOverlay;
    }

    public void setTutorialOverlay(TutorialOverlayModel tutorialOverlay) {
        this.tutorialOverlay =
            Objects.requireNonNull(tutorialOverlay, "tutorialOverlay cannot be null.");
    }

    public ActionPanelModel getActionPanel() {
        return actionPanel;
    }

    public void setActionPanel(ActionPanelModel actionPanel) {
        this.actionPanel = Objects.requireNonNull(actionPanel, "actionPanel cannot be null.");
    }

    public void resetPlaceholders() {
        playerCards.clear();
        rackTiles.clear();
        boardTiles.clear();
        wordOutline = null;
        interactionLocked = false;
        aiErrorSummary = "";
        aiErrorDetails = "";
        transientMessageText = "";
        previewPanel = PreviewPanelModel.hidden();
        tutorialOverlay = TutorialOverlayModel.hidden();
        actionPanel = ActionPanelModel.defaultState();
        for (int index = 0; index < DEFAULT_RACK_SLOT_COUNT; index++) {
            rackTiles.add(TileModel.empty());
        }
    }

    public static final class PlayerCardModel {
        private final String playerName;
        private final String playerId;
        private final int score;
        private final String stepMarkText;
        private final boolean currentTurn;
        private final boolean active;

        public PlayerCardModel(
            String playerName,
            String playerId,
            int score,
            String stepMarkText,
            boolean currentTurn,
            boolean active) {
            this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
            this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
            this.score = score;
            this.stepMarkText = Objects.requireNonNull(stepMarkText, "stepMarkText cannot be null.");
            this.currentTurn = currentTurn;
            this.active = active;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getPlayerId() {
            return playerId;
        }

        public int getScore() {
            return score;
        }

        public String getStepMarkText() {
            return stepMarkText;
        }

        public boolean isCurrentTurn() {
            return currentTurn;
        }

        public boolean isActive() {
            return active;
        }
    }

    public static final class TileModel {
        private final String tileId;
        private final String letter;
        private final int score;
        private final boolean empty;
        private final boolean blank;
        private final String assignedLetter;
        private final boolean tutorialHighlighted;
        private final boolean tutorialDimmed;

        public TileModel(
            String tileId,
            String letter,
            int score,
            boolean empty,
            boolean blank,
            String assignedLetter,
            boolean tutorialHighlighted,
            boolean tutorialDimmed) {
            this.tileId = Objects.requireNonNull(tileId, "tileId cannot be null.");
            this.letter = Objects.requireNonNull(letter, "letter cannot be null.");
            this.score = score;
            this.empty = empty;
            this.blank = blank;
            this.assignedLetter = Objects.requireNonNull(
                assignedLetter,
                "assignedLetter cannot be null.");
            this.tutorialHighlighted = tutorialHighlighted;
            this.tutorialDimmed = tutorialDimmed;
        }

        public static TileModel empty() {
            return new TileModel("", "", 0, true, false, "", false, false);
        }

        public static TileModel empty(boolean tutorialHighlighted, boolean tutorialDimmed) {
            return new TileModel(
                "",
                "",
                0,
                true,
                false,
                "",
                tutorialHighlighted,
                tutorialDimmed);
        }

        public static TileModel filled(String tileId, String letter, int score) {
            return new TileModel(tileId, letter, score, false, false, "", false, false);
        }

        public static TileModel filled(
            String tileId,
            String letter,
            int score,
            boolean tutorialHighlighted,
            boolean tutorialDimmed) {
            return new TileModel(tileId, letter, score, false, false, "", tutorialHighlighted, tutorialDimmed);
        }

        public static TileModel blank(
            String tileId,
            String displayLetter,
            int score,
            String assignedLetter) {
            return new TileModel(tileId, displayLetter, score, false, true, assignedLetter, false, false);
        }

        public static TileModel blank(
            String tileId,
            String displayLetter,
            int score,
            String assignedLetter,
            boolean tutorialHighlighted,
            boolean tutorialDimmed) {
            return new TileModel(
                tileId,
                displayLetter,
                score,
                false,
                true,
                assignedLetter,
                tutorialHighlighted,
                tutorialDimmed);
        }

        public String getTileId() {
            return tileId;
        }

        public String getLetter() {
            return letter;
        }

        public int getScore() {
            return score;
        }

        public boolean isEmpty() {
            return empty;
        }

        public boolean isBlank() {
            return blank;
        }

        public String getAssignedLetter() {
            return assignedLetter;
        }

        public boolean hasAssignedLetter() {
            return !assignedLetter.isBlank();
        }

        public boolean isTutorialHighlighted() {
            return tutorialHighlighted;
        }

        public boolean isTutorialDimmed() {
            return tutorialDimmed;
        }
    }

    public static final class BoardTileModel {
        private final BoardCoordinate coordinate;
        private final TileModel tile;
        private final BonusType bonusType;
        private final boolean draft;
        private final boolean previewValid;
        private final boolean previewInvalid;
        private final boolean mainWordHighlighted;
        private final boolean crossWordHighlighted;
        private final boolean mainWordPreviewValid;
        private final boolean mainWordPreviewInvalid;
        private final TileModel ghostTile;
        private final boolean tutorialHighlighted;
        private final boolean tutorialDimmed;

        public BoardTileModel(
            BoardCoordinate coordinate,
            TileModel tile,
            BonusType bonusType,
            boolean draft,
            boolean previewValid,
            boolean previewInvalid,
            boolean mainWordHighlighted,
            boolean crossWordHighlighted,
            boolean mainWordPreviewValid,
            boolean mainWordPreviewInvalid,
            TileModel ghostTile,
            boolean tutorialHighlighted,
            boolean tutorialDimmed) {
            this.coordinate = Objects.requireNonNull(coordinate, "coordinate cannot be null.");
            this.tile = Objects.requireNonNull(tile, "tile cannot be null.");
            this.bonusType = Objects.requireNonNull(bonusType, "bonusType cannot be null.");
            this.draft = draft;
            this.previewValid = previewValid;
            this.previewInvalid = previewInvalid;
            this.mainWordHighlighted = mainWordHighlighted;
            this.crossWordHighlighted = crossWordHighlighted;
            this.mainWordPreviewValid = mainWordPreviewValid;
            this.mainWordPreviewInvalid = mainWordPreviewInvalid;
            this.ghostTile = ghostTile;
            this.tutorialHighlighted = tutorialHighlighted;
            this.tutorialDimmed = tutorialDimmed;
        }

        public BoardCoordinate getCoordinate() {
            return coordinate;
        }

        public TileModel getTile() {
            return tile;
        }

        public BonusType getBonusType() {
            return bonusType;
        }

        public boolean isDraft() {
            return draft;
        }

        public boolean isPreviewValid() {
            return previewValid;
        }

        public boolean isPreviewInvalid() {
            return previewInvalid;
        }

        public boolean isMainWordHighlighted() {
            return mainWordHighlighted;
        }

        public boolean isCrossWordHighlighted() {
            return crossWordHighlighted;
        }

        public boolean isMainWordPreviewValid() {
            return mainWordPreviewValid;
        }

        public boolean isMainWordPreviewInvalid() {
            return mainWordPreviewInvalid;
        }

        public TileModel getGhostTile() {
            return ghostTile;
        }

        public boolean isTutorialHighlighted() {
            return tutorialHighlighted;
        }

        public boolean isTutorialDimmed() {
            return tutorialDimmed;
        }
    }

    public static final class WordOutlineModel {
        private final int startRow;
        private final int startCol;
        private final int endRow;
        private final int endCol;
        private final boolean valid;

        public WordOutlineModel(
            int startRow,
            int startCol,
            int endRow,
            int endCol,
            boolean valid) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
            this.valid = valid;
        }

        public int getStartRow() {
            return startRow;
        }

        public int getStartCol() {
            return startCol;
        }

        public int getEndRow() {
            return endRow;
        }

        public int getEndCol() {
            return endCol;
        }

        public boolean isValid() {
            return valid;
        }
    }

    public static final class PreviewPanelModel {
        private final boolean visible;
        private final boolean valid;
        private final String statusText;
        private final String scoreText;
        private final String mainWordText;
        private final List<String> messages;

        public PreviewPanelModel(
            boolean visible,
            boolean valid,
            String statusText,
            String scoreText,
            String mainWordText,
            List<String> messages) {
            this.visible = visible;
            this.valid = valid;
            this.statusText = Objects.requireNonNull(statusText, "statusText cannot be null.");
            this.scoreText = Objects.requireNonNull(scoreText, "scoreText cannot be null.");
            this.mainWordText = Objects.requireNonNull(mainWordText, "mainWordText cannot be null.");
            this.messages = List.copyOf(Objects.requireNonNull(messages, "messages cannot be null."));
        }

        public static PreviewPanelModel hidden() {
            return new PreviewPanelModel(false, false, "", "", "", List.of());
        }

        public boolean isVisible() {
            return visible;
        }

        public boolean isValid() {
            return valid;
        }

        public String getStatusText() {
            return statusText;
        }

        public String getScoreText() {
            return scoreText;
        }

        public String getMainWordText() {
            return mainWordText;
        }

        public List<String> getMessages() {
            return messages;
        }
    }

    public static final class TutorialOverlayModel {
        private final boolean visible;
        private final String progressText;
        private final String title;
        private final String body;
        private final boolean tapToContinue;
        private final boolean showExitButton;
        private final boolean showReturnHomeButton;

        public TutorialOverlayModel(
            boolean visible,
            String progressText,
            String title,
            String body,
            boolean tapToContinue,
            boolean showExitButton,
            boolean showReturnHomeButton) {
            this.visible = visible;
            this.progressText = Objects.requireNonNull(progressText, "progressText cannot be null.");
            this.title = Objects.requireNonNull(title, "title cannot be null.");
            this.body = Objects.requireNonNull(body, "body cannot be null.");
            this.tapToContinue = tapToContinue;
            this.showExitButton = showExitButton;
            this.showReturnHomeButton = showReturnHomeButton;
        }

        public static TutorialOverlayModel hidden() {
            return new TutorialOverlayModel(false, "", "", "", false, false, false);
        }

        public boolean isVisible() {
            return visible;
        }

        public String getProgressText() {
            return progressText;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }

        public boolean isTapToContinue() {
            return tapToContinue;
        }

        public boolean isShowExitButton() {
            return showExitButton;
        }

        public boolean isShowReturnHomeButton() {
            return showReturnHomeButton;
        }
    }

    public static final class ActionPanelModel {
        private final ActionButtonModel skipButton;
        private final ActionButtonModel rearrangeButton;
        private final ActionButtonModel recallButton;
        private final ActionButtonModel resignButton;
        private final ActionButtonModel submitButton;

        public ActionPanelModel(
            ActionButtonModel skipButton,
            ActionButtonModel rearrangeButton,
            ActionButtonModel recallButton,
            ActionButtonModel resignButton,
            ActionButtonModel submitButton) {
            this.skipButton = Objects.requireNonNull(skipButton, "skipButton cannot be null.");
            this.rearrangeButton = Objects.requireNonNull(
                rearrangeButton,
                "rearrangeButton cannot be null.");
            this.recallButton = Objects.requireNonNull(recallButton, "recallButton cannot be null.");
            this.resignButton = Objects.requireNonNull(resignButton, "resignButton cannot be null.");
            this.submitButton = Objects.requireNonNull(submitButton, "submitButton cannot be null.");
        }

        public static ActionPanelModel defaultState() {
            ActionButtonModel button = ActionButtonModel.enabled();
            return new ActionPanelModel(button, button, button, button, button);
        }

        public ActionButtonModel getSkipButton() {
            return skipButton;
        }

        public ActionButtonModel getRearrangeButton() {
            return rearrangeButton;
        }

        public ActionButtonModel getRecallButton() {
            return recallButton;
        }

        public ActionButtonModel getResignButton() {
            return resignButton;
        }

        public ActionButtonModel getSubmitButton() {
            return submitButton;
        }
    }

    public static final class ActionButtonModel {
        private final boolean enabled;
        private final boolean highlighted;

        public ActionButtonModel(boolean enabled, boolean highlighted) {
            this.enabled = enabled;
            this.highlighted = highlighted;
        }

        public static ActionButtonModel enabled() {
            return new ActionButtonModel(true, false);
        }

        public static ActionButtonModel of(boolean enabled, boolean highlighted) {
            return new ActionButtonModel(enabled, highlighted);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isHighlighted() {
            return highlighted;
        }
    }
}
