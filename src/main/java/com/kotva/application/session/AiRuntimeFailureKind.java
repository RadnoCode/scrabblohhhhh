package com.kotva.application.session;

/**
 * Lists the kinds of AI runtime failures.
 */
public enum AiRuntimeFailureKind {
    /**
     * AI setup failed after all retries.
     */
    INIT_RETRY_EXHAUSTED,
    /**
     * The AI move request failed.
     */
    MOVE_REQUEST_FAILURE,
    /**
     * The AI move was rejected by game rules.
     */
    INVALID_MOVE_REJECTED,
    /**
     * Too many invalid AI moves were rejected.
     */
    INVALID_MOVE_CIRCUIT_BROKEN
}
