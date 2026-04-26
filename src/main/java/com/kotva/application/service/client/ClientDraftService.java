package com.kotva.application.service.client;

import com.kotva.application.draft.DraftManager;
import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.draft.TurnDraftActionMapper;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.application.session.RackTileSnapshot;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Position;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Objects;

/**
 * Manages the local client's editable draft before sending it to the host.
 */
public class ClientDraftService {
    private final ClientGameContext context;
    private final DraftManager draftManager;
    private final ClientPreviewService previewService;
    private TurnDraft turnDraft;

    /**
     * Creates a client draft service with default helpers.
     *
     * @param context client game context
     */
    public ClientDraftService(ClientGameContext context) {
        this(context, new DraftManager(), new ClientPreviewService(context));
    }

    /**
     * Creates a client draft service with a custom preview service.
     *
     * @param context client game context
     * @param previewService preview service
     */
    public ClientDraftService(ClientGameContext context, ClientPreviewService previewService) {
        this(context, new DraftManager(), previewService);
    }

    /**
     * Creates a client draft service.
     *
     * @param context client game context
     * @param draftManager draft manager
     * @param previewService preview service
     */
    public ClientDraftService(
            ClientGameContext context, DraftManager draftManager, ClientPreviewService previewService) {
        this.context = Objects.requireNonNull(context, "context cannot be null.");
        this.draftManager = Objects.requireNonNull(draftManager, "draftManager cannot be null.");
        this.previewService =
                Objects.requireNonNull(previewService, "previewService cannot be null.");
        this.turnDraft = new TurnDraft();
    }

    /**
     * Places a tile in the local draft.
     *
     * @param tileId tile id
     * @param position board position
     * @return refreshed preview
     */
    public PreviewResult placeDraftTile(String tileId, Position position) {
        ensureEditingAllowed();
        draftManager.placeTile(turnDraft, tileId, position);
        return refreshPreview();
    }

    /**
     * Moves a tile in the local draft.
     *
     * @param tileId tile id
     * @param newPosition new board position
     * @return refreshed preview
     */
    public PreviewResult moveDraftTile(String tileId, Position newPosition) {
        ensureEditingAllowed();
        draftManager.moveTile(turnDraft, tileId, newPosition);
        return refreshPreview();
    }

    /**
     * Removes a tile from the local draft.
     *
     * @param tileId tile id
     * @return refreshed preview
     */
    public PreviewResult removeDraftTile(String tileId) {
        ensureEditingAllowed();
        draftManager.removeTile(turnDraft, tileId);
        return refreshPreview();
    }

    /**
     * Assigns a letter to a blank tile in the local draft.
     *
     * @param tileId blank tile id
     * @param assignedLetter selected letter
     */
    public void assignBlankTileLetter(String tileId, char assignedLetter) {
        ensureEditingAllowed();
        Objects.requireNonNull(tileId, "tileId cannot be null.");
        char normalizedLetter = Character.toUpperCase(assignedLetter);
        if (normalizedLetter < 'A' || normalizedLetter > 'Z') {
            throw new IllegalArgumentException("Assigned letter must be between A and Z.");
        }

        RackTileSnapshot rackTileSnapshot = context.getLatestSnapshot().getVisibleRackTiles().stream()
            .filter(visibleRackTile -> tileId.equals(visibleRackTile.getTileId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown tileId: " + tileId));
        if (!rackTileSnapshot.isBlank()) {
            throw new IllegalArgumentException("Tile is not a blank tile: " + tileId);
        }

        turnDraft.getAssignedLettersByTileId().put(tileId, normalizedLetter);
        DraftPlacement draftPlacement = draftManager.findPlacementByTileId(turnDraft, tileId);
        if (draftPlacement != null) {
            draftPlacement.setAssignedLetter(normalizedLetter);
            refreshPreview();
            return;
        }
        turnDraft.setPreviewResult(null);
    }

    /**
     * Recalls all local draft tiles.
     *
     * @return refreshed preview
     */
    public PreviewResult recallAllDraftTiles() {
        ensureEditingAllowed();
        draftManager.recallAllTiles(turnDraft);
        return refreshPreview();
    }

    /**
     * Refreshes preview for the current local draft.
     *
     * @return preview result
     */
    public PreviewResult refreshPreview() {
        ensureEditingAllowed();
        return previewService.refreshPreview(turnDraft);
    }

    /**
     * Builds the player action that should be sent to the host.
     *
     * @return submit action
     */
    public PlayerAction buildSubmitAction() {
        return TurnDraftActionMapper.toPlaceAction(context.getLocalPlayerId(), turnDraft);
    }

    /**
     * Builds a UI snapshot with local draft data applied.
     *
     * @return UI snapshot
     */
    public GameSessionSnapshot getUiSnapshot() {
        return GameSessionSnapshotFactory.withLocalDraft(context.getLatestSnapshot(), turnDraft);
    }

    /**
     * Updates the latest host snapshot.
     *
     * @param snapshot host snapshot
     */
    public void updateSnapshot(GameSessionSnapshot snapshot) {
        context.updateSnapshot(snapshot);
    }

    /**
     * Gets current local draft placements.
     *
     * @return draft placements
     */
    public List<DraftPlacement> getPlacements() {
        return List.copyOf(turnDraft.getPlacements());
    }

    /**
     * Clears the local draft.
     */
    public void resetDraft() {
        this.turnDraft = new TurnDraft();
    }

    /**
     * Ensures the local player can edit the draft now.
     */
    private void ensureEditingAllowed() {
        if (context.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game session is not in progress.");
        }
        if (!context.isLocalPlayerTurn()) {
            throw new IllegalStateException("It is not this client player's turn.");
        }
    }
}
