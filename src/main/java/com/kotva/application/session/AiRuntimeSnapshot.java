package com.kotva.application.session;

import java.io.Serializable;
import java.util.Objects;

/**
 * Snapshot of the current AI runtime status for the UI.
 *
 * @param fatal whether the AI problem cannot be recovered
 * @param interactionLocked whether player interaction should be blocked
 * @param failureKind type of AI failure
 * @param summary short status message
 * @param details detailed status message
 * @param consecutiveIllegalMoveCount illegal AI moves in a row
 * @param candidateCount number of AI candidates received
 * @param attemptedCandidateCount number of AI candidates already tried
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
     * Validates required text fields and failure kind.
     */
    public AiRuntimeSnapshot {
        failureKind = Objects.requireNonNull(failureKind, "failureKind cannot be null.");
        summary = Objects.requireNonNull(summary, "summary cannot be null.");
        details = Objects.requireNonNull(details, "details cannot be null.");
    }
}
