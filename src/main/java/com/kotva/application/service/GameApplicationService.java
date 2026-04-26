package com.kotva.application.service;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Position;

/**
 * Main application service for editing drafts and executing player actions.
 */
public interface GameApplicationService {
    /**
     * Assigns a letter to a blank tile.
     *
     * @param session game session
     * @param tileId blank tile id
     * @param assignedLetter selected letter
     */
    void assignLettertoBlank(GameSession session, String tileId, char assignedLetter);

    /**
     * Places a tile into the current draft.
     *
     * @param session game session
     * @param tileId tile id
     * @param position target board position
     * @return refreshed preview result
     */
    PreviewResult placeDraftTile(GameSession session, String tileId, Position position);

    /**
     * Moves a draft tile.
     *
     * @param session game session
     * @param tileId tile id
     * @param newPosition new board position
     * @return refreshed preview result
     */
    PreviewResult moveDraftTile(GameSession session, String tileId, Position newPosition);

    /**
     * Removes a tile from the current draft.
     *
     * @param session game session
     * @param tileId tile id
     * @return refreshed preview result
     */
    PreviewResult removeDraftTile(GameSession session, String tileId);

    /**
     * Recalls all draft tiles.
     *
     * @param session game session
     * @return refreshed preview result
     */
    PreviewResult recallAllDraftTiles(GameSession session);

    /**
     * Submits the current draft.
     *
     * @param session game session
     * @return action result
     */
    GameActionResult submitDraft(GameSession session);

    /**
     * Submits the current draft with a client action id.
     *
     * @param session game session
     * @param clientActionId client action id
     * @return action result
     */
    GameActionResult submitDraft(GameSession session, String clientActionId);

    /**
     * Passes the current turn.
     *
     * @param session game session
     * @return action result
     */
    GameActionResult passTurn(GameSession session);

    /**
     * Passes the current turn with a client action id.
     *
     * @param session game session
     * @param clientActionId client action id
     * @return action result
     */
    GameActionResult passTurn(GameSession session, String clientActionId);

    /**
     * Resigns the current player.
     *
     * @param session game session
     * @return action result
     */
    GameActionResult resign(GameSession session);

    /**
     * Resigns the current player with a client action id.
     *
     * @param session game session
     * @param clientActionId client action id
     * @return action result
     */
    GameActionResult resign(GameSession session, String clientActionId);

    /**
     * Executes a command received from a remote client.
     *
     * @param session game session
     * @param action remote player action
     * @return action result
     */
    GameActionResult executeRemoteCommand(GameSession session, PlayerAction action);

    /**
     * Executes a remote command with its client action id.
     *
     * @param session game session
     * @param action remote player action
     * @param clientActionId client action id
     * @return action result
     */
    GameActionResult executeRemoteCommand(
            GameSession session,
            PlayerAction action,
            String clientActionId);

    /**
     * Confirms hot-seat handoff between local players.
     *
     * @param session game session
     */
    void confirmHotSeatHandoff(GameSession session);

    /**
     * Advances the game clock and returns a snapshot.
     *
     * @param session game session
     * @param elapsedMillis elapsed time in milliseconds
     * @return updated session snapshot
     */
    GameSessionSnapshot tickClock(GameSession session, long elapsedMillis);

    /**
     * Builds the current session snapshot.
     *
     * @param session game session
     * @return session snapshot
     */
    GameSessionSnapshot getSessionSnapshot(GameSession session);

}
