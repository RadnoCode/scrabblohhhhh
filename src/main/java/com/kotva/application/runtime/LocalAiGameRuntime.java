package com.kotva.application.runtime;

import com.kotva.ai.AiMove;
import com.kotva.application.service.AiRuntimeBootstrapper;
import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.service.AiTurnAttemptResult;
import com.kotva.application.service.AiTurnRuntime;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.session.AiRuntimeFailureKind;
import com.kotva.application.session.AiRuntimeSnapshot;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import com.kotva.policy.SessionStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class LocalAiGameRuntime extends AbstractLocalGameRuntime {
    private static final int MAX_INIT_ATTEMPTS = 2;
    private static final int INVALID_MOVE_FUSE_THRESHOLD = 3;

    private final Supplier<AiRuntimeBootstrapper> aiRuntimeBootstrapperSupplier;

    private AiTurnRuntime aiTurnRuntime;
    private AiRuntimeSnapshot aiRuntimeSnapshot;
    private int consecutiveIllegalMoveCount;

    LocalAiGameRuntime(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService,
        Supplier<AiRuntimeBootstrapper> aiRuntimeBootstrapperSupplier) {
        super(gameSetupService, gameApplicationService);
        this.aiRuntimeBootstrapperSupplier = Objects.requireNonNull(
            aiRuntimeBootstrapperSupplier,
            "aiRuntimeBootstrapperSupplier cannot be null.");
    }

        @Override
    protected void afterSessionStarted() {
        clearAiState();

        RuntimeException lastFailure = null;
        AiRuntimeBootstrapper lastBootstrapper = null;
        List<String> attemptMessages = new ArrayList<>();
        for (int attempt = 1; attempt <= MAX_INIT_ATTEMPTS; attempt++) {
            lastBootstrapper = requireBootstrapper();
            try {
                aiTurnRuntime = Objects.requireNonNull(
                    lastBootstrapper.create(requireSession().getConfig()),
                    "AI bootstrapper returned null runtime.");
                return;
            } catch (RuntimeException exception) {
                lastFailure = exception;
                shutdownAiTurnRuntime();
                String attemptMessage =
                "AI initialization attempt "
                + attempt
                + "/"
                + MAX_INIT_ATTEMPTS
                + " failed: "
                + exception.getMessage();
                attemptMessages.add(attemptMessage);
                System.err.println(attemptMessage);
                exception.printStackTrace(System.err);
            }
        }

        markFatalFailure(
            AiRuntimeFailureKind.INIT_RETRY_EXHAUSTED,
            "AI initialization failed after retry.",
            buildInitializationFailureDetails(lastBootstrapper, attemptMessages),
            lastFailure,
            0,
            0);
    }

        @Override
    protected GameSessionSnapshot decorateSnapshot(GameSessionSnapshot snapshot) {
        return com.kotva.application.session.GameSessionSnapshotFactory.fromSession(
            requireSession(),
            aiRuntimeSnapshot);
    }

        @Override
    public boolean hasAutomatedTurnSupport() {
        return aiTurnRuntime != null;
    }

        @Override
    public boolean isCurrentTurnAutomated() {
        if (!hasAutomatedTurnSupport() || !isSessionInProgress()) {
            return false;
        }
        return requireCurrentPlayerController().supportsAutomatedTurn();
    }

        @Override
    public void requestAutomatedTurnIfIdle(
        Consumer<AiSessionRuntime.TurnCompletion> completionConsumer) {
        if (!isCurrentTurnAutomated()) {
            return;
        }
        aiTurnRuntime.requestTurnIfIdle(
            requireSession(),
            requireCurrentPlayerController(),
            Objects.requireNonNull(completionConsumer, "completionConsumer cannot be null."));
    }

        @Override
    public boolean matchesAutomatedTurn(AiSessionRuntime.TurnCompletion completion) {
        if (!isCurrentTurnAutomated()) {
            return false;
        }

        Player currentPlayer = requireSession().getGameState().requireCurrentActivePlayer();
        return aiTurnRuntime.matchesCurrentTurn(
            Objects.requireNonNull(completion, "completion cannot be null."),
            requireSession(),
            currentPlayer,
            requireCurrentPlayerController());
    }

        @Override
    public void applyAutomatedTurn(AiSessionRuntime.TurnCompletion completion) {
        Objects.requireNonNull(completion, "completion cannot be null.");
        if (completion.error() != null) {
            markFatalFailure(
                AiRuntimeFailureKind.MOVE_REQUEST_FAILURE,
                "AI move request failed.",
                buildMoveRequestFailureDetails(completion.error()),
                completion.error(),
                0,
                0);
            return;
        }
        if (aiTurnRuntime == null) {
            throw new IllegalStateException("Automated turn support is not available.");
        }
        if (completion.moveOptions() == null || completion.moveOptions().isEmpty()) {
            markFatalFailure(
                AiRuntimeFailureKind.MOVE_REQUEST_FAILURE,
                "AI returned no move candidates.",
                "The AI response contained an empty candidate set.",
                null,
                0,
                0);
            return;
        }

        int candidateCount = completion.moveOptions().size();
        int attemptedCandidateCount = 0;
        PlayerController controller = requireCurrentPlayerController();
        GameSession session = requireSession();
        String lastRejectedDetails = null;

        for (AiMove candidate : completion.moveOptions().moves()) {
            attemptedCandidateCount++;
            AiTurnAttemptResult result =
            aiTurnRuntime.applyMove(
                controller,
                gameApplicationService,
                session,
                candidate);
            if (result.accepted()) {
                consecutiveIllegalMoveCount = 0;
                aiRuntimeSnapshot = null;
                return;
            }

            consecutiveIllegalMoveCount++;
            String details = buildInvalidMoveDetails(result, candidateCount, attemptedCandidateCount);
            lastRejectedDetails = details;
            logRejectedMove(result, details);
            if (consecutiveIllegalMoveCount >= INVALID_MOVE_FUSE_THRESHOLD) {
                markFatalFailure(
                    AiRuntimeFailureKind.INVALID_MOVE_CIRCUIT_BROKEN,
                    "AI move circuit breaker opened after repeated invalid candidates.",
                    details,
                    result.error(),
                    candidateCount,
                    attemptedCandidateCount);
                return;
            }
        }

        aiRuntimeSnapshot = new AiRuntimeSnapshot(
            false,
            false,
            AiRuntimeFailureKind.INVALID_MOVE_REJECTED,
            summarize(
            AiRuntimeFailureKind.INVALID_MOVE_REJECTED,
            "AI move candidate was rejected. Waiting for another AI attempt."),
            buildRetryingDetails(lastRejectedDetails, candidateCount, attemptedCandidateCount),
            consecutiveIllegalMoveCount,
            candidateCount,
            attemptedCandidateCount);
    }

        @Override
    public void cancelPendingAutomatedTurn() {
        if (aiTurnRuntime != null) {
            aiTurnRuntime.cancelPending();
        }
    }

        @Override
    public void disableAutomatedTurnSupport() {
        shutdownAiTurnRuntime();
    }

        @Override
    public void shutdown() {
        super.shutdown();
        clearAiState();
    }

    private void clearAiState() {
        aiRuntimeSnapshot = null;
        consecutiveIllegalMoveCount = 0;
    }

    private void shutdownAiTurnRuntime() {
        if (aiTurnRuntime != null) {
            aiTurnRuntime.close();
            aiTurnRuntime = null;
        }
    }

    private AiRuntimeBootstrapper requireBootstrapper() {
        AiRuntimeBootstrapper bootstrapper = aiRuntimeBootstrapperSupplier.get();
        if (bootstrapper == null) {
            throw new IllegalStateException("aiRuntimeBootstrapperSupplier returned null.");
        }
        return bootstrapper;
    }

    private void markFatalFailure(
        AiRuntimeFailureKind failureKind,
        String summary,
        String details,
        Throwable error,
        int candidateCount,
        int attemptedCandidateCount) {
        shutdownAiTurnRuntime();
        requireSession().resetTurnDraft();
        requireSession().getGameState().markGameOver(GameEndReason.AI_RUNTIME_FAILURE);
        requireSession().setSessionStatus(SessionStatus.COMPLETED);
        aiRuntimeSnapshot = new AiRuntimeSnapshot(
            true,
            true,
            failureKind,
            summarize(failureKind, summary),
            details,
            consecutiveIllegalMoveCount,
            candidateCount,
            attemptedCandidateCount);

        System.err.println(aiRuntimeSnapshot.summary());
        System.err.println(details);
        if (error != null) {
            error.printStackTrace(System.err);
        }
    }

    private static String summarize(AiRuntimeFailureKind failureKind, String summary) {
        return summary + " [" + failureKind.name() + "]";
    }

    private String buildInitializationFailureDetails(
        AiRuntimeBootstrapper bootstrapper, List<String> attemptMessages) {
        StringBuilder builder = new StringBuilder();
        if (bootstrapper != null) {
            builder.append("platform=")
                .append(bootstrapper.getPlatform())
                .append(", libraryPath=")
                .append(bootstrapper.getLibraryPath())
                .append(", dataDirectory=")
                .append(bootstrapper.getDataDirectory());
        }
        if (!attemptMessages.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(System.lineSeparator());
            }
            builder.append(String.join(System.lineSeparator(), attemptMessages));
        }
        return builder.toString();
    }

    private static String buildMoveRequestFailureDetails(Throwable error) {
        if (error == null) {
            return "The AI move request failed without an exception object.";
        }
        return "The AI move request failed before any candidate could be applied: " + error.getMessage();
    }

    private String buildInvalidMoveDetails(
        AiTurnAttemptResult result, int candidateCount, int attemptedCandidateCount) {
        StringBuilder builder = new StringBuilder();
        builder.append("candidate ")
            .append(attemptedCandidateCount)
            .append("/")
            .append(candidateCount)
            .append(" rejected with code=")
            .append(result.rejectionCode())
            .append(", reason=")
            .append(result.rejectionReason())
            .append(", consecutiveIllegalMoveCount=")
            .append(consecutiveIllegalMoveCount);
        if (result.error() != null && result.error().getMessage() != null) {
            builder.append(", exception=").append(result.error().getMessage());
        }
        return builder.toString();
    }

    private String buildRetryingDetails(
        String lastRejectedDetails, int candidateCount, int attemptedCandidateCount) {
        StringBuilder builder = new StringBuilder();
        if (lastRejectedDetails != null && !lastRejectedDetails.isBlank()) {
            builder.append(lastRejectedDetails).append(System.lineSeparator());
        }
        builder.append("candidateCount=")
            .append(candidateCount)
            .append(", attemptedCandidateCount=")
            .append(attemptedCandidateCount)
            .append(", consecutiveIllegalMoveCount=")
            .append(consecutiveIllegalMoveCount);
        return builder.toString();
    }

    private static void logRejectedMove(AiTurnAttemptResult result, String details) {
        System.err.println("AI move candidate rejected.");
        System.err.println(details);
        if (result.error() != null) {
            result.error().printStackTrace(System.err);
        }
    }
}