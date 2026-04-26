package com.kotva.application.session;

/**
 * Lists the main kinds of AI runtime failures.
 */
public enum AiRuntimeFailureKind {
    /** AI setup failed after retrying. */
    INIT_RETRY_EXHAUSTED,
    /** AI move request failed. */
    MOVE_REQUEST_FAILURE,
    /** AI produced a move that the game rejected. */
    INVALID_MOVE_REJECTED,
    /** AI produced too many invalid moves and was stopped. */
    INVALID_MOVE_CIRCUIT_BROKEN
}
