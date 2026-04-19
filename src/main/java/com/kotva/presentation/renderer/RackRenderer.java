package com.kotva.presentation.renderer;

import com.kotva.presentation.component.RackView;
import com.kotva.presentation.interaction.GameDraftState;
import java.util.Objects;

public class RackRenderer {
    private final RackView rackView;
    private final GameDraftState draftState;
    private final PreviewRenderer previewRenderer;

    public RackRenderer(RackView rackView, GameDraftState draftState, PreviewRenderer previewRenderer) {
        this.rackView = Objects.requireNonNull(rackView, "rackView cannot be null.");
        this.draftState = Objects.requireNonNull(draftState, "draftState cannot be null.");
        this.previewRenderer = Objects.requireNonNull(previewRenderer, "previewRenderer cannot be null.");
    }

    public void render() {
        rackView.setTiles(draftState.getRenderedRackTiles(previewRenderer.getSuppressedRackIndex()));
    }
}