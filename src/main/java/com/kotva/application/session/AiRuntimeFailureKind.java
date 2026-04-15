package com.kotva.application.session;

public enum AiRuntimeFailureKind {
    INIT_RETRY_EXHAUSTED,
    MOVE_REQUEST_FAILURE,
    INVALID_MOVE_REJECTED,
    INVALID_MOVE_CIRCUIT_BROKEN
}
