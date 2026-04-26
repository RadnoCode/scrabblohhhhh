package com.kotva.application.service;

import com.kotva.ai.AiMove;
import java.util.Objects;

/**
 * Describes whether applying an AI move succeeded.
 *
 * @param move AI move that was attempted
 * @param accepted whether the move was accepted
 * @param rejectionCode short rejection code
 * @param rejectionReason readable rejection reason
 * @param error exception raised while applying the move
 * @param awardedScore score earned by the move
 * @param nextPlayerId player id that should act next
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
     * Validates the attempt result.
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
     * @param move accepted AI move
     * @param awardedScore score earned by the move
     * @param nextPlayerId player id that should act next
     * @return accepted attempt result
     */
    public static AiTurnAttemptResult accepted(AiMove move, int awardedScore, String nextPlayerId) {
        return new AiTurnAttemptResult(move, true, null, null, null, awardedScore, nextPlayerId);
    }

    /**
     * Creates a rejected AI move result.
     *
     * @param move rejected AI move
     * @param rejectionCode short rejection code
     * @param rejectionReason readable rejection reason
     * @param error exception raised while applying the move
     * @return rejected attempt result
     */
    public static AiTurnAttemptResult rejected(
        AiMove move, String rejectionCode, String rejectionReason, Throwable error) {
        return new AiTurnAttemptResult(move, false, rejectionCode, rejectionReason, error, 0, null);
    }
}
