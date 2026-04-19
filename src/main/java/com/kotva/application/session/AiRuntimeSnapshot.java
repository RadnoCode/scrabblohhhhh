package com.kotva.application.session;

import java.util.Objects;

public record AiRuntimeSnapshot(
    boolean fatal,
    boolean interactionLocked,
    AiRuntimeFailureKind failureKind,
    String summary,
    String details,
    int consecutiveIllegalMoveCount,
    int candidateCount,
    int attemptedCandidateCount) {

    public AiRuntimeSnapshot {
        failureKind = Objects.requireNonNull(failureKind, "failureKind cannot be null.");
        summary = Objects.requireNonNull(summary, "summary cannot be null.");
        details = Objects.requireNonNull(details, "details cannot be null.");
    }
}