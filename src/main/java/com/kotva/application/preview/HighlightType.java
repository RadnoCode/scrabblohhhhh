package com.kotva.application.preview;

/**
 * Describes how a preview tile should be highlighted on the board.
 */
public enum HighlightType {
    /** The tile is part of a valid preview move. */
    VALID_TILE,
    /** The tile is part of an invalid preview move. */
    INVALID_TILE,
    /** The tile is newly placed in the current draft. */
    NEW_TILE,
}
