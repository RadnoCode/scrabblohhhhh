package com.kotva.presentation.renderer;

import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.interaction.GameDraftState;
import com.kotva.presentation.viewmodel.BoardCoordinate;
import java.util.Objects;

/**
 * BoardRenderer applies the merged committed-board and local-draft state to the
 * reusable BoardView.
 */
public class BoardRenderer {
    private final BoardView boardView;
    private final GameDraftState draftState;
    private final PreviewRenderer previewRenderer;

    public BoardRenderer(BoardView boardView, GameDraftState draftState, PreviewRenderer previewRenderer) {
        this.boardView = Objects.requireNonNull(boardView, "boardView cannot be null.");
        this.draftState = Objects.requireNonNull(draftState, "draftState cannot be null.");
        this.previewRenderer = Objects.requireNonNull(previewRenderer, "previewRenderer cannot be null.");
    }

    public void render() {
        BoardCoordinate suppressedCoordinate = previewRenderer.getSuppressedBoardCoordinate();
        boardView.setTiles(draftState.getRenderedBoardTiles(suppressedCoordinate));
    }
}
