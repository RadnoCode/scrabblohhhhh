package com.kotva.application.service;

import com.kotva.ai.AiMove;
import java.util.Objects;

/**
 * Result of trying to apply one AI move.
 *
 * @param move AI move that was attempted
 * @param accepted whether the move was accepted
 * @param rejectionCode code for a rejected move
 * @param rejectionReason readable reason for rejection
 * @param error error raised while applying the move
 * @param awardedScore score awarded if accepted
 * @param nextPlayerId next player id if accepted
 */
public record AiTurnAttemptResult(
    AiMove move,
    boolean accepted,
    String rejectionCode,
    String rejectionReason,
    Throwable error,
    int awardedScore,
    String nextPlayerId) {

    /**
     * Validates rejection information.
     */
    public AiTurnAttemptResult {
        move = Objects.requireNonNull(move, "move cannot be null.");
        if (accepted) {
            rejectionCode = null;
            rejectionReason = null;
        } else {
            rejectionCode =
            Objects.requireNonNull(rejectionCode, "rejectionCode cannot be null for rejected attempts.");
            rejectionReason = Objects.requireNonNull(
                rejectionReason, "rejectionReason cannot be null for rejected attempts.");
        }
    }

    /**
     * Creates an accepted AI move result.
     *
     * @param move accepted move
     * @param awardedScore score awarded
     * @param nextPlayerId next player id
     * @return accepted result
     */
    public static AiTurnAttemptResult accepted(AiMove move, int awardedScore, String nextPlayerId) {
        return new AiTurnAttemptResult(move, true, null, null, null, awardedScore, nextPlayerId);
    }

    /**
     * Creates a rejected AI move result.
     *
     * @param move rejected move
     * @param rejectionCode rejection code
     * @param rejectionReason rejection reason
     * @param error related error, or {@code null}
     * @return rejected result
     */
    public static AiTurnAttemptResult rejected(
        AiMove move, String rejectionCode, String rejectionReason, Throwable error) {
        return new AiTurnAttemptResult(move, false, rejectionCode, rejectionReason, error, 0, null);
    }
}
