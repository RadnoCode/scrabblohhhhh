package com.kotva.presentation.interaction;

import com.kotva.domain.model.Position;

/**
 * Lists game actions that the UI can request.
 */
public interface GameActionPort {

    /**
     * Checks if the game UI is locked.
     *
     * @return true if the UI is locked
     */
    boolean isInteractionLocked();

    /**
     * Places a draft tile on the board.
     *
     * @param tileId tile id
     * @param position board position
     */
    void onDraftTilePlaced(String tileId, Position position);

    /**
     * Moves a draft tile.
     *
     * @param tileId tile id
     * @param position board position
     */
    void onDraftTileMoved(String tileId, Position position);

    /**
     * Removes a draft tile.
     *
     * @param tileId tile id
     */
    void onDraftTileRemoved(String tileId);

    /**
     * Sets the letter for a blank tile.
     *
     * @param tileId tile id
     * @param assignedLetter chosen letter
     */
    void onBlankTileLetterAssigned(String tileId, char assignedLetter);

    /**
     * Recalls all draft tiles.
     */
    void onRecallAllDraftTilesRequested();

    /**
     * Submits the draft move.
     */
    void onSubmitDraftRequested();

    /**
     * Skips the turn.
     */
    void onSkipTurnRequested();

    /**
     * Rearranges the rack.
     */
    void onRearrangeRequested();

    /**
     * Resigns the game.
     */
    void onResignRequested();

    /**
     * Opens the debug rack editor.
     */
    void onDebugRackEditRequested();

    /**
     * Moves to the next tutorial step.
     */
    void onTutorialAdvanceRequested();

    /**
     * Exits the tutorial.
     */
    void onTutorialExitRequested();

    /**
     * Returns to the home screen from the tutorial.
     */
    void onTutorialReturnHomeRequested();
}
