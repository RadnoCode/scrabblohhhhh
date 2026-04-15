package com.kotva.application.runtime;

import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.Position;
import java.util.function.Consumer;

public interface GameRuntime {
    void start(NewGameRequest request);

    default boolean requiresBackgroundRefresh() {
        return false;
    }

    boolean hasSession();

    GameSession getSession();

    boolean hasTimeControl();

    boolean isSessionInProgress();

    GameSessionSnapshot getSessionSnapshot();

    GameSessionSnapshot tickClock(long elapsedMillis);

    void placeDraftTile(String tileId, Position position);

    void moveDraftTile(String tileId, Position position);

    void removeDraftTile(String tileId);

    void recallAllDraftTiles();

    void submitDraft();

    void passTurn();

    boolean hasAutomatedTurnSupport();

    boolean isCurrentTurnAutomated();

    void requestAutomatedTurnIfIdle(
            Consumer<AiSessionRuntime.TurnCompletion> completionConsumer);

    boolean matchesAutomatedTurn(AiSessionRuntime.TurnCompletion completion);

    void applyAutomatedTurn(AiSessionRuntime.TurnCompletion completion);

    void cancelPendingAutomatedTurn();

    void disableAutomatedTurnSupport();

    void shutdown();
}
