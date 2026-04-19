package com.kotva.application.service;

import com.kotva.ai.AiMove;
import java.util.Objects;

public record AiTurnAttemptResult(
    AiMove move,
    boolean accepted,
    String rejectionCode,
    String rejectionReason,
    Throwable error,
    int awardedScore,
    String nextPlayerId) {

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

    public static AiTurnAttemptResult accepted(AiMove move, int awardedScore, String nextPlayerId) {
        return new AiTurnAttemptResult(move, true, null, null, null, awardedScore, nextPlayerId);
    }

    public static AiTurnAttemptResult rejected(
        AiMove move, String rejectionCode, String rejectionReason, Throwable error) {
        return new AiTurnAttemptResult(move, false, rejectionCode, rejectionReason, error, 0, null);
    }
}