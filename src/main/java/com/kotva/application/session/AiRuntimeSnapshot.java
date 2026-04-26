package com.kotva.application.session;

import java.io.Serializable;
import java.util.Objects;

/**
 * Stores UI-visible state for the AI runtime.
 *
 * @param fatal whether the AI failure is fatal
 * @param interactionLocked whether player interaction is locked
 * @param failureKind current failure kind
 * @param summary short status message
 * @param details detailed status message
 * @param consecutiveIllegalMoveCount number of rejected AI moves in a row
 * @param candidateCount number of candidate moves
 * @param attemptedCandidateCount number of tried candidate moves
 */
public record AiRuntimeSnapshot(
    boolean fatal,
    boolean interactionLocked,
    AiRuntimeFailureKind failureKind,
    String summary,
    String details,
    int consecutiveIllegalMoveCount,
    int candidateCount,
    int attemptedCandidateCount) implements Serializable {

    /**
     * Validates the AI runtime snapshot.
     */
    public AiRuntimeSnapshot {
        failureKind = Objects.requireNonNull(failureKind, "failureKind cannot be null.");
        summary = Objects.requireNonNull(summary, "summary cannot be null.");
        details = Objects.requireNonNull(details, "details cannot be null.");
    }
}
