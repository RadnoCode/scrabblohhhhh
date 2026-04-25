package com.kotva.runtime;

import com.kotva.application.service.AiSessionRuntime;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.GameSetupService;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Rack;
import com.kotva.domain.model.RackSlot;
import com.kotva.domain.model.Tile;
import com.kotva.domain.model.TileBag;
import com.kotva.mode.PlayerController;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

abstract class AbstractLocalGameRuntime implements GameRuntime {
    private final GameSetupService gameSetupService;
    protected final GameApplicationService gameApplicationService;

    private GameSession session;

    protected AbstractLocalGameRuntime(
        GameSetupService gameSetupService,
        GameApplicationService gameApplicationService) {
        this.gameSetupService =
        Objects.requireNonNull(gameSetupService, "gameSetupService cannot be null.");
        this.gameApplicationService = Objects.requireNonNull(
            gameApplicationService, "gameApplicationService cannot be null.");
    }

        @Override
    public void start(NewGameRequest request) {
        Objects.requireNonNull(request, "request cannot be null.");
        shutdown();
        session = gameSetupService.startNewGame(request);
        afterSessionStarted();
    }

        @Override
    public boolean hasSession() {
        return session != null;
    }

        @Override
    public GameSession getSession() {
        return session;
    }

        @Override
    public boolean hasTimeControl() {
        return session != null && session.getConfig().hasTimeControl();
    }

        @Override
    public boolean isSessionInProgress() {
        return session != null && session.getSessionStatus() == SessionStatus.IN_PROGRESS;
    }

        @Override
    public GameSessionSnapshot getSessionSnapshot() {
        return decorateSnapshot(gameApplicationService.getSessionSnapshot(requireSession()));
    }

        @Override
    public GameSessionSnapshot tickClock(long elapsedMillis) {
        return decorateSnapshot(gameApplicationService.tickClock(requireSession(), elapsedMillis));
    }

        @Override
    public void placeDraftTile(String tileId, Position position) {
        requireCurrentPlayerController().placeDraftTile(
            gameApplicationService,
            requireSession(),
            tileId,
            Objects.requireNonNull(position, "position cannot be null."));
    }

        @Override
    public void moveDraftTile(String tileId, Position position) {
        requireCurrentPlayerController().moveDraftTile(
            gameApplicationService,
            requireSession(),
            tileId,
            Objects.requireNonNull(position, "position cannot be null."));
    }

        @Override
    public void removeDraftTile(String tileId) {
        requireCurrentPlayerController().removeDraftTile(
            gameApplicationService,
            requireSession(),
            tileId);
    }

        @Override
    public void assignBlankTileLetter(String tileId, char assignedLetter) {
        requireCurrentPlayerController().assignLettertoBlank(
            gameApplicationService,
            requireSession(),
            tileId,
            assignedLetter);
    }

        @Override
    public void recallAllDraftTiles() {
        requireCurrentPlayerController().recallAllDraftTiles(
            gameApplicationService,
            requireSession());
    }

        @Override
    public void submitDraft() {
        requireCurrentPlayerController().submitDraft(gameApplicationService, requireSession());
    }

        @Override
    public void submitDraft(String clientActionId) {
        requireCurrentPlayerController().submitDraft(
            gameApplicationService,
            requireSession(),
            clientActionId);
    }

        @Override
    public void passTurn() {
        requireCurrentPlayerController().passTurn(gameApplicationService, requireSession());
    }

        @Override
    public void passTurn(String clientActionId) {
        requireCurrentPlayerController().passTurn(
            gameApplicationService,
            requireSession(),
            clientActionId);
    }

        @Override
    public void resign() {
        requireCurrentPlayerController().resign(gameApplicationService, requireSession());
    }

        @Override
    public void resign(String clientActionId) {
        requireCurrentPlayerController().resign(
            gameApplicationService,
            requireSession(),
            clientActionId);
    }

        @Override
    public boolean supportsRackDebugEditing() {
        Player currentPlayer = resolveCurrentPlayer();
        return currentPlayer != null
            && currentPlayer.getPlayerType() == PlayerType.LOCAL
            && !isCurrentTurnAutomated();
    }

        @Override
    public void replaceCurrentRack(String rackSpec) {
        if (!supportsRackDebugEditing()) {
            throw new IllegalStateException("只有当前本地玩家回合才能修改 rack。");
        }

        String normalizedRackSpec = normalizeRackSpec(rackSpec);
        if (normalizedRackSpec.length() > 7) {
            throw new IllegalArgumentException("Rack 最多只能输入 7 张牌。");
        }

        GameSession session = requireSession();
        session.resetTurnDraft();

        Player currentPlayer = Objects.requireNonNull(resolveCurrentPlayer(), "current player is unavailable.");
        Rack rack = currentPlayer.getRack();
        TileBag tileBag = session.getGameState().getTileBag();
        List<Tile> originalTiles = new ArrayList<>();

        for (RackSlot slot : rack.getSlots()) {
            Tile tile = slot.getTile();
            if (tile == null) {
                continue;
            }
            originalTiles.add(tile);
            tileBag.returnTile(tile);
            rack.setTileAt(slot.getIndex(), null);
        }

        try {
            for (int index = 0; index < normalizedRackSpec.length(); index++) {
                rack.setTileAt(index, takeDebugTile(tileBag, normalizedRackSpec.charAt(index)));
            }
        } catch (RuntimeException exception) {
            restoreOriginalRack(rack, tileBag, originalTiles);
            throw exception;
        }
    }

        @Override
    public boolean hasAutomatedTurnSupport() {
        return false;
    }

        @Override
    public boolean isCurrentTurnAutomated() {
        return false;
    }

        @Override
    public void requestAutomatedTurnIfIdle(
        Consumer<AiSessionRuntime.TurnCompletion> completionConsumer) {
    }

        @Override
    public boolean matchesAutomatedTurn(AiSessionRuntime.TurnCompletion completion) {
        return false;
    }

        @Override
    public void applyAutomatedTurn(AiSessionRuntime.TurnCompletion completion) {
        throw new IllegalStateException("Automated turn support is not available.");
    }

        @Override
    public void cancelPendingAutomatedTurn() {
    }

        @Override
    public void disableAutomatedTurnSupport() {
    }

        @Override
    public void shutdown() {
        disableAutomatedTurnSupport();
        session = null;
    }

    protected void afterSessionStarted() {
    }

    protected GameSessionSnapshot decorateSnapshot(GameSessionSnapshot snapshot) {
        return Objects.requireNonNull(snapshot, "snapshot cannot be null.");
    }

    protected final GameSession requireSession() {
        return Objects.requireNonNull(session, "session cannot be null.");
    }

    protected final void setSession(GameSession session) {
        this.session = Objects.requireNonNull(session, "session cannot be null.");
    }

    protected final PlayerController requireCurrentPlayerController() {
        Player currentPlayer = resolveCurrentPlayer();
        if (currentPlayer == null) {
            throw new IllegalStateException("current player is unavailable.");
        }
        return Objects.requireNonNull(
            currentPlayer.getController(),
            "current player controller cannot be null.");
    }

    private Player resolveCurrentPlayer() {
        if (session == null || !session.getGameState().hasActivePlayers()) {
            return null;
        }
        return session.getGameState().requireCurrentActivePlayer();
    }

    private String normalizeRackSpec(String rackSpec) {
        if (rackSpec == null) {
            throw new IllegalArgumentException("Rack 输入不能为空。");
        }

        StringBuilder normalized = new StringBuilder();
        for (int index = 0; index < rackSpec.length(); index++) {
            char symbol = rackSpec.charAt(index);
            if (Character.isWhitespace(symbol) || symbol == ',' || symbol == '|') {
                continue;
            }

            char uppercase = Character.toUpperCase(symbol);
            if ((uppercase >= 'A' && uppercase <= 'Z') || uppercase == '?' || uppercase == '*') {
                normalized.append(uppercase);
                continue;
            }
            throw new IllegalArgumentException("仅支持 A-Z，以及 `?` 或 `*` 表示 blank。");
        }
        return normalized.toString();
    }

    private Tile takeDebugTile(TileBag tileBag, char symbol) {
        try {
            return symbol == '?' || symbol == '*'
                ? tileBag.takeBlankTile()
                : tileBag.takeTileByLetter(symbol);
        } catch (IllegalStateException exception) {
            throw new IllegalArgumentException("Tile bag 中没有足够的 `" + symbol + "` 可供替换。", exception);
        }
    }

    private void restoreOriginalRack(Rack rack, TileBag tileBag, List<Tile> originalTiles) {
        for (RackSlot slot : rack.getSlots()) {
            Tile tile = slot.getTile();
            if (tile == null) {
                continue;
            }
            tileBag.returnTile(tile);
            rack.setTileAt(slot.getIndex(), null);
        }

        for (int index = 0; index < originalTiles.size() && index < rack.getSlots().size(); index++) {
            Tile tile = originalTiles.get(index);
            tileBag.removeTileById(tile.getTileID());
            rack.setTileAt(index, tile);
        }
    }
}
