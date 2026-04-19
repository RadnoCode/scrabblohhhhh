package com.kotva.application.session;

import com.kotva.tutorial.TutorialActionKey;
import com.kotva.tutorial.TutorialScriptId;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

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

    public TutorialScriptId getScriptId() {
        return scriptId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public int getStepCount() {
        return stepCount;
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

    public boolean isDimNonTargetBoardCells() {
        return dimNonTargetBoardCells;
    }

    public boolean isDimNonTargetRackSlots() {
        return dimNonTargetRackSlots;
    }

    public List<PreviewPositionSnapshot> getHighlightedBoardPositions() {
        return highlightedBoardPositions;
    }

    public List<Integer> getHighlightedRackSlots() {
        return highlightedRackSlots;
    }

    public List<TutorialGhostTileSnapshot> getGhostTiles() {
        return ghostTiles;
    }

    public List<TutorialActionKey> getHighlightedActions() {
        return highlightedActions;
    }

    public List<TutorialActionKey> getEnabledActions() {
        return enabledActions;
    }
}
