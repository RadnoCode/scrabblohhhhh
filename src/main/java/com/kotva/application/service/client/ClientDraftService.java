package com.kotva.application.service.client;

import com.kotva.application.draft.DraftManager;
import com.kotva.application.draft.DraftPlacement;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.draft.TurnDraftActionMapper;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Position;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Objects;

public class ClientDraftService {
    private final ClientGameContext context;
    private final DraftManager draftManager;
    private final ClientPreviewService previewService;
    private TurnDraft turnDraft;

    public ClientDraftService(ClientGameContext context) {
        this(context, new DraftManager(), new ClientPreviewService(context));
    }

    public ClientDraftService(ClientGameContext context, ClientPreviewService previewService) {
        this(context, new DraftManager(), previewService);
    }

    public ClientDraftService(
            ClientGameContext context, DraftManager draftManager, ClientPreviewService previewService) {
        this.context = Objects.requireNonNull(context, "context cannot be null.");
        this.draftManager = Objects.requireNonNull(draftManager, "draftManager cannot be null.");
        this.previewService =
                Objects.requireNonNull(previewService, "previewService cannot be null.");
        this.turnDraft = new TurnDraft();
    }

    public PreviewResult placeDraftTile(String tileId, Position position) {
        ensureEditingAllowed();
        draftManager.placeTile(turnDraft, tileId, position);
        return refreshPreview();
    }

    public PreviewResult moveDraftTile(String tileId, Position newPosition) {
        ensureEditingAllowed();
        draftManager.moveTile(turnDraft, tileId, newPosition);
        return refreshPreview();
    }

    public PreviewResult removeDraftTile(String tileId) {
        ensureEditingAllowed();
        draftManager.removeTile(turnDraft, tileId);
        return refreshPreview();
    }

    public PreviewResult recallAllDraftTiles() {
        ensureEditingAllowed();
        draftManager.recallAllTiles(turnDraft);
        return refreshPreview();
    }

    public PreviewResult refreshPreview() {
        ensureEditingAllowed();
        return previewService.refreshPreview(turnDraft);
    }

    public PlayerAction buildSubmitAction() {
        return TurnDraftActionMapper.toPlaceAction(context.getLocalPlayerId(), turnDraft);
    }

    public GameSessionSnapshot getUiSnapshot() {
        return GameSessionSnapshotFactory.withLocalDraft(context.getLatestSnapshot(), turnDraft);
    }

    public void updateSnapshot(GameSessionSnapshot snapshot) {
        context.updateSnapshot(snapshot);
    }

    public List<DraftPlacement> getPlacements() {
        return List.copyOf(turnDraft.getPlacements());
    }

    public void resetDraft() {
        this.turnDraft = new TurnDraft();
    }

    private void ensureEditingAllowed() {
        if (context.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game session is not in progress.");
        }
        if (!context.isLocalPlayerTurn()) {
            throw new IllegalStateException("It is not this client player's turn.");
        }
    }
}
