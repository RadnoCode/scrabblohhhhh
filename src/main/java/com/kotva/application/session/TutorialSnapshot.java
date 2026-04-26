package com.kotva.application.session;

import com.kotva.tutorial.TutorialActionKey;
import com.kotva.tutorial.TutorialScriptId;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Snapshot of the current tutorial step for the UI.
 */
public final class TutorialSnapshot implements Serializable {
    private final TutorialScriptId scriptId;
    private final int stepNumber;
    private final int stepCount;
    private final String title;
    private final String body;
    private final boolean tapToContinue;
    private final boolean showExitButton;
    private final boolean showReturnHomeButton;
    private final boolean dimNonTargetBoardCells;
    private final boolean dimNonTargetRackSlots;
    private final List<PreviewPositionSnapshot> highlightedBoardPositions;
    private final List<Integer> highlightedRackSlots;
    private final List<TutorialGhostTileSnapshot> ghostTiles;
    private final List<TutorialActionKey> highlightedActions;
    private final List<TutorialActionKey> enabledActions;

    /**
     * Creates a tutorial snapshot.
     *
     * @param scriptId tutorial script id
     * @param stepNumber current step number
     * @param stepCount total step count
     * @param title tutorial title
     * @param body tutorial body text
     * @param tapToContinue whether tapping can continue the tutorial
     * @param showExitButton whether the exit button is shown
     * @param showReturnHomeButton whether the return-home button is shown
     * @param dimNonTargetBoardCells whether non-target board cells are dimmed
     * @param dimNonTargetRackSlots whether non-target rack slots are dimmed
     * @param highlightedBoardPositions board positions to highlight
     * @param highlightedRackSlots rack slots to highlight
     * @param ghostTiles tutorial ghost tiles to show
     * @param highlightedActions actions to highlight
     * @param enabledActions actions allowed at this step
     */
    public TutorialSnapshot(
        TutorialScriptId scriptId,
        int stepNumber,
        int stepCount,
        String title,
        String body,
        boolean tapToContinue,
        boolean showExitButton,
        boolean showReturnHomeButton,
        boolean dimNonTargetBoardCells,
        boolean dimNonTargetRackSlots,
        List<PreviewPositionSnapshot> highlightedBoardPositions,
        List<Integer> highlightedRackSlots,
        List<TutorialGhostTileSnapshot> ghostTiles,
        List<TutorialActionKey> highlightedActions,
        List<TutorialActionKey> enabledActions) {
        this.scriptId = Objects.requireNonNull(scriptId, "scriptId cannot be null.");
        this.stepNumber = stepNumber;
        this.stepCount = stepCount;
        this.title = Objects.requireNonNull(title, "title cannot be null.");
        this.body = Objects.requireNonNull(body, "body cannot be null.");
        this.tapToContinue = tapToContinue;
        this.showExitButton = showExitButton;
        this.showReturnHomeButton = showReturnHomeButton;
        this.dimNonTargetBoardCells = dimNonTargetBoardCells;
        this.dimNonTargetRackSlots = dimNonTargetRackSlots;
        this.highlightedBoardPositions = List.copyOf(
            Objects.requireNonNull(
                highlightedBoardPositions,
                "highlightedBoardPositions cannot be null."));
        this.highlightedRackSlots = List.copyOf(
            Objects.requireNonNull(highlightedRackSlots, "highlightedRackSlots cannot be null."));
        this.ghostTiles = List.copyOf(Objects.requireNonNull(ghostTiles, "ghostTiles cannot be null."));
        this.highlightedActions = List.copyOf(
            Objects.requireNonNull(highlightedActions, "highlightedActions cannot be null."));
        this.enabledActions = List.copyOf(
            Objects.requireNonNull(enabledActions, "enabledActions cannot be null."));
    }

    /**
     * Gets the tutorial script id.
     *
     * @return script id
     */
    public TutorialScriptId getScriptId() {
        return scriptId;
    }

    /**
     * Gets the current step number.
     *
     * @return step number
     */
    public int getStepNumber() {
        return stepNumber;
    }

    /**
     * Gets the total step count.
     *
     * @return step count
     */
    public int getStepCount() {
        return stepCount;
    }

    /**
     * Gets the title text.
     *
     * @return title text
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the body text.
     *
     * @return body text
     */
    public String getBody() {
        return body;
    }

    /**
     * Checks whether tapping can continue the tutorial.
     *
     * @return {@code true} if tap-to-continue is enabled
     */
    public boolean isTapToContinue() {
        return tapToContinue;
    }

    /**
     * Checks whether the exit button is shown.
     *
     * @return {@code true} if shown
     */
    public boolean isShowExitButton() {
        return showExitButton;
    }

    /**
     * Checks whether the return-home button is shown.
     *
     * @return {@code true} if shown
     */
    public boolean isShowReturnHomeButton() {
        return showReturnHomeButton;
    }

    /**
     * Checks whether non-target board cells are dimmed.
     *
     * @return {@code true} if dimmed
     */
    public boolean isDimNonTargetBoardCells() {
        return dimNonTargetBoardCells;
    }

    /**
     * Checks whether non-target rack slots are dimmed.
     *
     * @return {@code true} if dimmed
     */
    public boolean isDimNonTargetRackSlots() {
        return dimNonTargetRackSlots;
    }

    /**
     * Gets highlighted board positions.
     *
     * @return highlighted board positions
     */
    public List<PreviewPositionSnapshot> getHighlightedBoardPositions() {
        return highlightedBoardPositions;
    }

    /**
     * Gets highlighted rack slots.
     *
     * @return highlighted rack slot indexes
     */
    public List<Integer> getHighlightedRackSlots() {
        return highlightedRackSlots;
    }

    /**
     * Gets ghost tiles shown by the tutorial.
     *
     * @return ghost tile list
     */
    public List<TutorialGhostTileSnapshot> getGhostTiles() {
        return ghostTiles;
    }

    /**
     * Gets highlighted action buttons.
     *
     * @return highlighted actions
     */
    public List<TutorialActionKey> getHighlightedActions() {
        return highlightedActions;
    }

    /**
     * Gets actions enabled for this step.
     *
     * @return enabled actions
     */
    public List<TutorialActionKey> getEnabledActions() {
        return enabledActions;
    }
}
