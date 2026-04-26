package com.kotva.application.service;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.PlayerConfig;
import com.kotva.application.session.TimeControlConfig;
import com.kotva.application.setup.NewGameRequest;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.PlayerClock;
import com.kotva.domain.model.TileBag;
import com.kotva.infrastructure.dictionary.DictionaryRepository;
import com.kotva.mode.GameMode;
import com.kotva.mode.PlayerController;
import com.kotva.policy.SessionStatus;
import com.kotva.policy.PlayerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Default service for validating setup input and creating game sessions.
 */
public class GameSetupServiceImpl implements GameSetupService {
    private final DictionaryRepository dictionaryRepository;
    private final ClockService clockService;
    private final Random random;

    /**
     * Creates a setup service.
     *
     * @param dictionaryRepository dictionary repository to load words
     * @param clockService clock service used to start timed games
     * @param random random source used for player order
     */
    public GameSetupServiceImpl(
        DictionaryRepository dictionaryRepository,
        ClockService clockService,
        Random random) {
        this.dictionaryRepository =
        Objects.requireNonNull(dictionaryRepository, "dictionaryRepository cannot be null.");
        this.clockService = Objects.requireNonNull(clockService, "clockService cannot be null.");
        this.random = Objects.requireNonNull(random, "random cannot be null.");
    }

    /**
     * Validates the setup request and builds a game config.
     *
     * @param request setup request from the UI
     * @return validated game config
     */
    @Override
    public GameConfig buildConfig(NewGameRequest request) {
        Objects.requireNonNull(request, "request cannot be null.");

        GameMode gameMode = request.getGameMode();
        if (gameMode == null) {
            throw new IllegalArgumentException("gameMode cannot be null.");
        }

        int playerCount = request.getPlayerCount();
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("playerCount must be between 2 and 4.");
        }
        if (gameMode == GameMode.HUMAN_VS_AI && playerCount != 2) {
            throw new IllegalArgumentException("HUMAN_VS_AI currently requires exactly 2 players.");
        }
        if (gameMode == GameMode.HUMAN_VS_AI && request.getAiDifficulty() == null) {
            throw new IllegalArgumentException("HUMAN_VS_AI requires aiDifficulty.");
        }

        if (request.getDictionaryType() == null) {
            throw new IllegalArgumentException("dictionaryType cannot be null.");
        }
        if (request.getRuleset().isScribbleRuleset() && !isValidTargetScore(request.getTargetScore())) {
            throw new IllegalArgumentException("Scribble requires a positive target score.");
        }

        List<String> playerNames = request.getPlayerNames();
        if (playerNames.size() != playerCount) {
            throw new IllegalArgumentException("playerNames size must match playerCount.");
        }

        List<PlayerConfig> players = new ArrayList<>(playerCount);
        Set<String> seenNames = new HashSet<>();
        for (int index = 0; index < playerNames.size(); index++) {
            String rawName = playerNames.get(index);
            String normalizedName = normalizePlayerName(rawName);
            String uniqueKey = normalizedName.toLowerCase(Locale.ROOT);
            if (!seenNames.add(uniqueKey)) {
                throw new IllegalArgumentException("player names must be unique.");
            }
            players.add(new PlayerConfig(normalizedName, resolvePlayerType(gameMode, index)));
        }

        return new GameConfig(
            gameMode,
            players,
            request.getDictionaryType(),
            request.getTimeControlConfig(),
            request.getAiDifficulty(),
            request.getRuleset(),
            request.getRuleset().isScribbleRuleset() ? request.getTargetScore() : null);
    }

    /**
     * Starts a new game from a setup request.
     *
     * @param request setup request from the UI
     * @return created game session
     */
    @Override
    public GameSession startNewGame(NewGameRequest request) {
        GameConfig config = buildConfig(request);
        return createSession(config);
    }

    /**
     * Starts a new game from an already validated config.
     *
     * @param config game config
     * @return created game session
     */
    @Override
    public GameSession startNewGame(GameConfig config) {
        Objects.requireNonNull(config, "config cannot be null.");
        return createSession(config);
    }

    /**
     * Creates the session, players, tile bag, and initial rack draw.
     *
     * @param config game config
     * @return created game session
     */
    private GameSession createSession(GameConfig config) {
        dictionaryRepository.loadDictionary(config.getDictionaryType());

        List<Player> players = createPlayers(config);
        Collections.shuffle(players, random);

        TileBag tileBag = config.hasInfiniteTileBag() ? TileBag.infinite() : new TileBag();
        GameState gameState = new GameState(players, tileBag, config.getTargetScore());
        gameState.initialDraw();

        GameSession session = new GameSession(UUID.randomUUID().toString(), config, gameState);
        session.setSessionStatus(SessionStatus.IN_PROGRESS);
        if (config.hasTimeControl()) {
            clockService.startTurnClock(session);
        }
        return session;
    }

    /**
     * Creates domain players from player configs.
     *
     * @param config game config
     * @return player list
     */
    private List<Player> createPlayers(GameConfig config) {
        List<Player> players = new ArrayList<>(config.getPlayerCount());
        TimeControlConfig timeControlConfig = config.getTimeControlConfig();

        for (int index = 0; index < config.getPlayers().size(); index++) {
            PlayerConfig playerConfig = config.getPlayers().get(index);
            String playerId = "player-" + (index + 1);
            Player player =
            new Player(playerId, playerConfig.getPlayerName(), playerConfig.getPlayerType());
            player.setController(PlayerController.create(playerId, playerConfig.getPlayerType()));
            player.setClock(createPlayerClock(timeControlConfig));
            players.add(player);
        }

        return players;
    }

    /**
     * Creates a player clock based on time control settings.
     *
     * @param timeControlConfig time control config, or {@code null}
     * @return player clock
     */
    private PlayerClock createPlayerClock(TimeControlConfig timeControlConfig) {
        if (timeControlConfig == null) {
            return PlayerClock.disabled();
        }
        return PlayerClock.timed(
            timeControlConfig.getMainTimeMillis(),
            timeControlConfig.getByoYomiMillisPerTurn());
    }

    /**
     * Trims and validates a player name.
     *
     * @param rawName name from setup input
     * @return normalized name
     */
    private String normalizePlayerName(String rawName) {
        if (rawName == null) {
            throw new IllegalArgumentException("player names cannot be null.");
        }

        String normalizedName = rawName.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("player names cannot be blank.");
        }
        return normalizedName;
    }

    /**
     * Decides the player type for a game mode and player index.
     *
     * @param gameMode selected game mode
     * @param playerIndex index in setup order
     * @return player type
     */
    private PlayerType resolvePlayerType(GameMode gameMode, int playerIndex) {
        return switch (gameMode) {
        case HOT_SEAT -> PlayerType.LOCAL;
        case HUMAN_VS_AI -> playerIndex == 0 ? PlayerType.LOCAL : PlayerType.AI;
        case LAN_MULTIPLAYER -> playerIndex == 0 ? PlayerType.LOCAL : PlayerType.LAN;
        };
    }

    /**
     * Checks whether a target score can be used.
     *
     * @param targetScore target score from setup
     * @return {@code true} if positive
     */
    private boolean isValidTargetScore(Integer targetScore) {
        return targetScore != null && targetScore > 0;
    }
}
