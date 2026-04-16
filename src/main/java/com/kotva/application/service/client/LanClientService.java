package com.kotva.application.service.client;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.ClientRuntimeSnapshot;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Position;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.LanClientTransport;
import com.kotva.infrastructure.network.LanCommandResultMessage;
import com.kotva.infrastructure.network.LanInboundMessage;
import com.kotva.infrastructure.network.LanSnapshotMessage;
import com.kotva.infrastructure.network.RemoteCommandResult;
import com.kotva.mode.PlayerController;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Objects;

public class LanClientService {
    private final ClientGameContext context;
    private final ClientDraftService draftService;
    private final LanClientTransport transport;
    private final PlayerController playerController;

    private String pendingCommandId;
    private String statusSummary;
    private String statusDetails;

    public LanClientService(ClientGameContext context, LanClientTransport transport) {
        this(
                context,
                new ClientDraftService(context),
                transport,
                PlayerController.create(context.getLocalPlayerId(), PlayerType.LAN));
    }

    public LanClientService(
            ClientGameContext context,
            ClientDraftService draftService,
            LanClientTransport transport,
            PlayerController playerController) {
        this.context = Objects.requireNonNull(context, "context cannot be null.");
        this.draftService = Objects.requireNonNull(draftService, "draftService cannot be null.");
        this.transport = Objects.requireNonNull(transport, "transport cannot be null.");
        this.playerController =
                Objects.requireNonNull(playerController, "playerController cannot be null.");
        this.pendingCommandId = null;
        this.statusSummary = "";
        this.statusDetails = "";
    }

    public PreviewResult placeDraftTile(String tileId, Position position) {
        ensureInteractiveEditingAllowed();
        clearStatusMessage();
        return draftService.placeDraftTile(tileId, position);
    }

    public PreviewResult moveDraftTile(String tileId, Position position) {
        ensureInteractiveEditingAllowed();
        clearStatusMessage();
        return draftService.moveDraftTile(tileId, position);
    }

    public PreviewResult removeDraftTile(String tileId) {
        ensureInteractiveEditingAllowed();
        clearStatusMessage();
        return draftService.removeDraftTile(tileId);
    }

    public PreviewResult recallAllDraftTiles() {
        ensureInteractiveEditingAllowed();
        clearStatusMessage();
        return draftService.recallAllDraftTiles();
    }

    public void submitDraft() {
        ensureInteractiveEditingAllowed();
        dispatchCommand(draftService.buildSubmitAction(), "Waiting for host confirmation.");
    }

    public void passTurn() {
        ensureInteractiveEditingAllowed();
        dispatchCommand(PlayerAction.pass(context.getLocalPlayerId()), "Waiting for host confirmation.");
    }

    public GameSessionSnapshot tickClock(long elapsedMillis) {
        List<LanInboundMessage> inboundMessages = transport.drainInboundMessages();
        for (LanInboundMessage inboundMessage : inboundMessages) {
            if (inboundMessage instanceof LanSnapshotMessage snapshotMessage) {
                applyAuthoritativeSnapshot(snapshotMessage.snapshot(), true);
            } else if (inboundMessage instanceof LanCommandResultMessage resultMessage) {
                applyCommandResult(resultMessage.result());
            }
        }
        context.advanceLocalClock(elapsedMillis);
        return getUiSnapshot();
    }

    public GameSessionSnapshot getUiSnapshot() {
        GameSessionSnapshot snapshot = draftService.getUiSnapshot();
        return GameSessionSnapshotFactory.withClientRuntimeSnapshot(snapshot, buildRuntimeSnapshot());
    }

    public void shutdown() {
        transport.close();
    }

    private void dispatchCommand(PlayerAction action, String summary) {
        CommandEnvelope commandEnvelope =
                playerController.buildLanCommand(
                        context.getSessionId(),
                        context.getTurnNumber(),
                        Objects.requireNonNull(action, "action cannot be null."));
        transport.sendCommand(commandEnvelope);
        pendingCommandId = commandEnvelope.getCommandId();
        statusSummary = Objects.requireNonNull(summary, "summary cannot be null.");
        statusDetails = "commandId=" + pendingCommandId;
    }

    private void applyCommandResult(RemoteCommandResult result) {
        Objects.requireNonNull(result, "result cannot be null.");
        boolean matchesPending =
                pendingCommandId != null && Objects.equals(pendingCommandId, result.commandId());
        if (matchesPending) {
            pendingCommandId = null;
        }

        if (result.snapshot() != null) {
            boolean preserveDraft = !result.success();
            applyAuthoritativeSnapshot(result.snapshot(), preserveDraft);
        }

        if (!result.success()) {
            statusSummary = "Host rejected action.";
            statusDetails =
                    (result.message() == null || result.message().isBlank())
                            ? "commandId=" + result.commandId()
                            : result.message() + System.lineSeparator() + "commandId=" + result.commandId();
        } else if (matchesPending) {
            clearStatusMessage();
        }
    }

    private void applyAuthoritativeSnapshot(
            GameSessionSnapshot snapshot, boolean preserveDraftWhenSameEditableTurn) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        boolean keepDraft =
                preserveDraftWhenSameEditableTurn && isSameEditableTurn(snapshot);
        context.updateSnapshot(snapshot);
        if (!keepDraft) {
            draftService.resetDraft();
        }
    }

    private boolean isSameEditableTurn(GameSessionSnapshot snapshot) {
        return snapshot.getSessionStatus() == SessionStatus.IN_PROGRESS
                && context.getSessionStatus() == SessionStatus.IN_PROGRESS
                && Objects.equals(snapshot.getCurrentPlayerId(), context.getLocalPlayerId())
                && Objects.equals(context.getLatestSnapshot().getCurrentPlayerId(), context.getLocalPlayerId())
                && snapshot.getTurnNumber() == context.getTurnNumber();
    }

    private ClientRuntimeSnapshot buildRuntimeSnapshot() {
        return new ClientRuntimeSnapshot(
                pendingCommandId != null,
                pendingCommandId,
                statusSummary,
                statusDetails);
    }

    private void ensureInteractiveEditingAllowed() {
        if (pendingCommandId != null) {
            throw new IllegalStateException("A client command is already pending host confirmation.");
        }
        if (context.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game session is not in progress.");
        }
        if (!context.isLocalPlayerTurn()) {
            throw new IllegalStateException("It is not this client player's turn.");
        }
    }

    private void clearStatusMessage() {
        statusSummary = "";
        statusDetails = "";
    }
}
