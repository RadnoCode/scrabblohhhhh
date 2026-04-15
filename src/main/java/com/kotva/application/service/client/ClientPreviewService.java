package com.kotva.application.service.client;

import com.kotva.application.draft.TurnDraft;
import com.kotva.application.preview.PreviewResult;
import com.kotva.application.service.MovePreviewService;
import com.kotva.application.service.MovePreviewServiceImpl;
import com.kotva.domain.model.GameState;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import java.util.Objects;

public class ClientPreviewService {
    private final ClientGameContext context;
    private final MovePreviewService movePreviewService;

    public ClientPreviewService(ClientGameContext context) {
        this(context, new DictionaryRepository());
    }

    public ClientPreviewService(ClientGameContext context, DictionaryRepository dictionaryRepository) {
        this(context, new MovePreviewServiceImpl(dictionaryRepository));
    }

    public ClientPreviewService(ClientGameContext context, MovePreviewService movePreviewService) {
        this.context = Objects.requireNonNull(context, "context cannot be null.");
        this.movePreviewService =
                Objects.requireNonNull(movePreviewService, "movePreviewService cannot be null.");
    }

    public PreviewResult refreshPreview(TurnDraft turnDraft) {
        Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");

        GameState previewState =
                ClientPreviewStateFactory.fromSnapshot(
                        context.getLatestSnapshot(),
                        context.getLocalPlayerId());
        PreviewResult previewResult =
                movePreviewService.preview(
                        previewState,
                        context.getDictionaryType(),
                        context.getLocalPlayerId(),
                        turnDraft);
        if (previewResult == null) {
            throw new IllegalStateException("movePreviewService returned null preview result.");
        }

        turnDraft.setPreviewResult(previewResult);
        return previewResult;
    }
}
