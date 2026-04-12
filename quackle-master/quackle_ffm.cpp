#include <algorithm>
#include <cctype>
#include <cstddef>
#include <cstdint>
#include <cstring>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

#include "alphabetparameters.h"
#include "bag.h"
#include "board.h"
#include "computerplayer.h"
#include "datamanager.h"
#include "game.h"
#include "lexiconparameters.h"
#include "move.h"
#include "resolvent.h"
#include "strategyparameters.h"

#ifdef _WIN32
#define QA_EXPORT extern "C" __declspec(dllexport)
#else
#define QA_EXPORT extern "C"
#endif

namespace {

constexpr int kBoardSide = 15;
constexpr int kBoardCellCount = kBoardSide * kBoardSide;
constexpr int kMaxPlacements = 7;
constexpr int kMaxMoveOptions = 10;
constexpr int kPassAction = 0;
constexpr int kPlaceAction = 1;

struct QaBoardCell {
    int occupied;
    int letter;
    int isBlank;
    int assignedLetter;
};

struct QaInit {
    const char *dataDir;
    const char *dictionaryId;
    const char *difficultyId;
};

struct QaPosition {
    const QaBoardCell *boardCells;
    const char *rack;
    const char *unseenTiles;
    int aiScore;
    int opponentScore;
};

struct QaPlacement {
    int row;
    int col;
    int letter;
    int isBlank;
    int assignedLetter;
};

struct QaMoveResult {
    int action;
    int placementCount;
    int score;
    double equity;
    double win;
    QaPlacement placements[kMaxPlacements];
};

struct QaMoveListResult {
    int moveCount;
    QaMoveResult moves[kMaxMoveOptions];
};

static_assert(sizeof(QaPlacement) == 20, "QaPlacement layout must match Java FFM.");
static_assert(offsetof(QaMoveResult, equity) == 16, "QaMoveResult layout must match Java FFM.");
static_assert(sizeof(QaMoveResult) == 176, "QaMoveResult layout must match Java FFM.");
static_assert(offsetof(QaMoveListResult, moves) == 8, "QaMoveListResult layout must match Java FFM.");

struct EngineConfig {
    std::string dataDir;
    std::string dictionaryKey;
    std::string difficultyKey;
};

std::mutex g_engineMutex;
std::unique_ptr<Quackle::DataManager> g_dataManager;
std::string g_loadedDataDir;
std::string g_loadedDictionaryKey;

std::string readAscii(const char *value) {
    return value == nullptr ? std::string() : std::string(value);
}

std::string upperAscii(std::string value) {
    std::transform(value.begin(), value.end(), value.begin(), [](unsigned char ch) {
        return static_cast<char>(std::toupper(ch));
    });
    return value;
}

void writeError(char *buffer, std::int64_t capacity, const std::string &message) {
    if (buffer == nullptr || capacity <= 0) {
        return;
    }

    const std::size_t safeCapacity = static_cast<std::size_t>(capacity);
    const std::size_t copyLength = std::min(message.size(), safeCapacity - 1);
    std::memcpy(buffer, message.data(), copyLength);
    buffer[copyLength] = '\0';
}

std::string normalizeDictionaryId(const std::string &dictionaryId) {
    const std::string normalized = upperAscii(dictionaryId);
    if (normalized == "AM" || normalized == "NWL" || normalized == "NWL18" || normalized == "NWL2018") {
        return "nwl18";
    }
    if (normalized == "BR" || normalized == "CSW" || normalized == "CSW19" || normalized == "CSW2019") {
        return "csw19";
    }
    return {};
}

std::string normalizeDifficultyId(const std::string &difficultyId) {
    const std::string normalized = upperAscii(difficultyId);
    if (normalized == "EASY" || normalized == "MEDIUM" || normalized == "HARD") {
        return normalized;
    }
    return {};
}

bool encodeVisibleTile(char rawTile, Quackle::Letter *encodedTile, std::string *error) {
    if (encodedTile == nullptr) {
        if (error != nullptr) {
            *error = "Internal error: encodedTile cannot be null.";
        }
        return false;
    }

    const char normalizedTile = static_cast<char>(std::toupper(static_cast<unsigned char>(rawTile)));
    if (normalizedTile == '?') {
        *encodedTile = QUACKLE_BLANK_MARK;
        return true;
    }
    if (!std::isalpha(static_cast<unsigned char>(normalizedTile))) {
        if (error != nullptr) {
            *error = "Tile must be alphabetic or '?'.";
        }
        return false;
    }

    UVString leftover;
    Quackle::LetterString encoded = QUACKLE_ALPHABET_PARAMETERS->encode(UVString(1, normalizedTile), &leftover);
    if (!leftover.empty() || encoded.size() != 1) {
        if (error != nullptr) {
            *error = std::string("Tile is not representable in the Quackle alphabet: ") + normalizedTile;
        }
        return false;
    }

    *encodedTile = encoded[0];
    return true;
}

bool encodeRackTiles(const std::string &tiles, Quackle::LetterString *encodedTiles, std::string *error) {
    if (encodedTiles == nullptr) {
        if (error != nullptr) {
            *error = "Internal error: encodedTiles cannot be null.";
        }
        return false;
    }

    Quackle::LetterString result;
    for (char rawTile : tiles) {
        Quackle::Letter encodedTile = QUACKLE_NULL_MARK;
        if (!encodeVisibleTile(rawTile, &encodedTile, error)) {
            return false;
        }
        result += encodedTile;
    }

    *encodedTiles = Quackle::String::alphabetize(result);
    return true;
}

bool encodeBagTiles(const std::string &tiles, Quackle::LongLetterString *encodedTiles, std::string *error) {
    if (encodedTiles == nullptr) {
        if (error != nullptr) {
            *error = "Internal error: encodedTiles cannot be null.";
        }
        return false;
    }

    Quackle::LongLetterString result;
    result.reserve(tiles.size());
    for (char rawTile : tiles) {
        Quackle::Letter encodedTile = QUACKLE_NULL_MARK;
        if (!encodeVisibleTile(rawTile, &encodedTile, error)) {
            return false;
        }
        result.push_back(static_cast<char>(encodedTile));
    }

    *encodedTiles = result;
    return true;
}

bool buildBoard(const QaBoardCell *boardCells, Quackle::Board *board, std::string *error) {
    if (boardCells == nullptr) {
        if (error != nullptr) {
            *error = "Position boardCells cannot be null.";
        }
        return false;
    }
    if (board == nullptr) {
        if (error != nullptr) {
            *error = "Internal error: board cannot be null.";
        }
        return false;
    }

    board->prepareEmptyBoard();
    for (int index = 0; index < kBoardCellCount; ++index) {
        const QaBoardCell &boardCell = boardCells[index];
        if (boardCell.occupied == 0) {
            continue;
        }

        const int row = index / kBoardSide;
        const int col = index % kBoardSide;
        const char visibleLetter = static_cast<char>(boardCell.isBlank != 0 && boardCell.assignedLetter != 0
                ? boardCell.assignedLetter
                : boardCell.letter);

        Quackle::Letter encodedTile = QUACKLE_NULL_MARK;
        if (!encodeVisibleTile(visibleLetter, &encodedTile, error)) {
            if (error != nullptr && error->find("Tile is not representable") == std::string::npos) {
                *error = "Board tile encoding failed.";
            }
            return false;
        }

        if (boardCell.isBlank != 0) {
            encodedTile = QUACKLE_ALPHABET_PARAMETERS->setBlankness(encodedTile);
        }

        Quackle::LetterString oneTileWord(1, static_cast<char>(encodedTile));
        board->makeMove(Quackle::Move::createPlaceMove(row, col, true, oneTileWord));
    }

    return true;
}

std::unique_ptr<Quackle::ComputerPlayer> createPlayerForDifficulty(
        const std::string &difficultyKey,
        std::string *error) {
    if (difficultyKey == "EASY") {
        return std::make_unique<Quackle::StaticPlayer>();
    }
    if (difficultyKey == "MEDIUM") {
        return std::make_unique<Quackle::TwentySecondPlayer>();
    }
    if (difficultyKey == "HARD") {
        return std::make_unique<Quackle::TorontoPlayer>();
    }

    if (error != nullptr) {
        *error = "Unsupported difficulty id: " + difficultyKey;
    }
    return nullptr;
}

bool ensureDataManagerLoaded(const EngineConfig &config, std::string *error) {
    if (g_dataManager != nullptr
            && g_loadedDataDir == config.dataDir
            && g_loadedDictionaryKey == config.dictionaryKey) {
        return true;
    }

    g_dataManager = std::make_unique<Quackle::DataManager>();
    g_dataManager->setAppDataDirectory(config.dataDir);
    g_dataManager->setUserDataDirectory(config.dataDir);
    g_dataManager->setBackupLexicon("default_english");

    const std::string dawgFile =
            Quackle::LexiconParameters::findDictionaryFile(config.dictionaryKey + ".dawg");
    if (dawgFile.empty()) {
        if (error != nullptr) {
            *error = "Missing Quackle dictionary file: " + config.dictionaryKey + ".dawg";
        }
        g_dataManager.reset();
        return false;
    }

    g_dataManager->lexiconParameters()->loadDawg(dawgFile);

    const std::string gaddagFile =
            Quackle::LexiconParameters::findDictionaryFile(config.dictionaryKey + ".gaddag");
    if (!gaddagFile.empty()) {
        g_dataManager->lexiconParameters()->loadGaddag(gaddagFile);
    }

    if (!g_dataManager->isGood()) {
        if (error != nullptr) {
            *error = "Failed to load Quackle lexicon: " + config.dictionaryKey;
        }
        g_dataManager.reset();
        return false;
    }

    g_dataManager->strategyParameters()->initialize(config.dictionaryKey);
    g_loadedDataDir = config.dataDir;
    g_loadedDictionaryKey = config.dictionaryKey;
    return true;
}

bool buildPosition(const QaPosition *snapshot, Quackle::GamePosition *position, std::string *error) {
    if (snapshot == nullptr) {
        if (error != nullptr) {
            *error = "Position cannot be null.";
        }
        return false;
    }
    if (position == nullptr) {
        if (error != nullptr) {
            *error = "Internal error: position cannot be null.";
        }
        return false;
    }

    Quackle::PlayerList players;
    players.push_back(Quackle::Player(MARK_UV("AI"), Quackle::Player::ComputerPlayerType));
    players.push_back(Quackle::Player(MARK_UV("Opponent"), Quackle::Player::HumanPlayerType));

    Quackle::Game game;
    game.setPlayers(players);
    game.addPosition();

    Quackle::GamePosition nextPosition = game.currentPosition();

    Quackle::Board board;
    if (!buildBoard(snapshot->boardCells, &board, error)) {
        return false;
    }
    nextPosition.setBoard(board);

    Quackle::LetterString encodedRack;
    if (!encodeRackTiles(readAscii(snapshot->rack), &encodedRack, error)) {
        return false;
    }

    Quackle::LongLetterString encodedUnseenTiles;
    if (!encodeBagTiles(readAscii(snapshot->unseenTiles), &encodedUnseenTiles, error)) {
        return false;
    }

    nextPosition.setPlayerRack(0, Quackle::Rack(encodedRack), false);
    nextPosition.setPlayerRack(1, Quackle::Rack(), false);

    Quackle::Bag bag;
    bag.clear();
    bag.toss(encodedUnseenTiles);
    nextPosition.setBag(bag);

    nextPosition.setCurrentPlayer(0);
    nextPosition.setPlayerOnTurn(0);
    nextPosition.currentPlayer().setScore(snapshot->aiScore);

    nextPosition.setCurrentPlayer(1);
    nextPosition.currentPlayer().setScore(snapshot->opponentScore);

    nextPosition.setCurrentPlayer(0);
    nextPosition.setPlayerOnTurn(0);
    nextPosition.ensureBoardIsPreparedForAnalysis();

    *position = nextPosition;
    return true;
}

bool decodeMovePlacements(
        const Quackle::Board &board,
        const Quackle::Move &move,
        QaMoveResult *target,
        std::string *error) {
    if (target == nullptr) {
        if (error != nullptr) {
            *error = "Internal error: target cannot be null.";
        }
        return false;
    }

    target->placementCount = 0;
    if (move.action != Quackle::Move::Place && move.action != Quackle::Move::PlaceError) {
        target->action = kPassAction;
        return true;
    }

    target->action = kPlaceAction;
    const Quackle::LetterString tiles = board.sanitizedTilesOfMove(move);
    int placementCount = 0;
    for (std::size_t index = 0; index < tiles.size(); ++index) {
        const Quackle::Letter tile = tiles[index];
        if (Quackle::Move::isAlreadyOnBoard(tile)) {
            continue;
        }

        if (placementCount >= kMaxPlacements) {
            if (error != nullptr) {
                *error = "Move exceeds placement capacity.";
            }
            return false;
        }

        const bool isBlank = QUACKLE_ALPHABET_PARAMETERS->isBlankLetter(tile);
        const Quackle::Letter visibleTile = QUACKLE_ALPHABET_PARAMETERS->clearBlankness(tile);
        const UVString visibleLetter = QUACKLE_ALPHABET_PARAMETERS->userVisible(visibleTile);
        if (visibleLetter.size() != 1) {
            if (error != nullptr) {
                *error = "Only single-character English tiles are supported by the native bridge.";
            }
            return false;
        }

        QaPlacement &placement = target->placements[placementCount++];
        placement.row = move.horizontal ? move.startrow : (move.startrow + static_cast<int>(index));
        placement.col = move.horizontal ? (move.startcol + static_cast<int>(index)) : move.startcol;
        placement.letter = visibleLetter[0];
        placement.isBlank = isBlank ? 1 : 0;
        placement.assignedLetter = isBlank ? visibleLetter[0] : 0;
    }

    target->placementCount = placementCount;
    return true;
}

bool encodeMoveList(
        const Quackle::GamePosition &position,
        const Quackle::MoveList &moves,
        QaMoveListResult *result,
        std::string *error) {
    if (result == nullptr) {
        if (error != nullptr) {
            *error = "Result buffer cannot be null.";
        }
        return false;
    }

    std::memset(result, 0, sizeof(QaMoveListResult));

    const int moveCount = std::min<int>(static_cast<int>(moves.size()), kMaxMoveOptions);
    result->moveCount = moveCount;
    for (int index = 0; index < moveCount; ++index) {
        const Quackle::Move &move = moves[index];
        QaMoveResult &encodedMove = result->moves[index];
        encodedMove.score = move.effectiveScore();
        encodedMove.equity = move.equity;
        encodedMove.win = move.win;
        if (!decodeMovePlacements(position.board(), move, &encodedMove, error)) {
            return false;
        }
    }

    return true;
}

}  // namespace

QA_EXPORT void *qa_create(const QaInit *init, char *errorBuffer, std::int64_t errorCapacity) {
    try {
        if (init == nullptr) {
            writeError(errorBuffer, errorCapacity, "Init payload cannot be null.");
            return nullptr;
        }

        EngineConfig config;
        config.dataDir = readAscii(init->dataDir);
        if (config.dataDir.empty()) {
            writeError(errorBuffer, errorCapacity, "Quackle data directory cannot be empty.");
            return nullptr;
        }

        config.dictionaryKey = normalizeDictionaryId(readAscii(init->dictionaryId));
        if (config.dictionaryKey.empty()) {
            writeError(
                    errorBuffer,
                    errorCapacity,
                    "Unsupported dictionary id: " + readAscii(init->dictionaryId));
            return nullptr;
        }

        config.difficultyKey = normalizeDifficultyId(readAscii(init->difficultyId));
        if (config.difficultyKey.empty()) {
            writeError(
                    errorBuffer,
                    errorCapacity,
                    "Unsupported difficulty id: " + readAscii(init->difficultyId));
            return nullptr;
        }

        std::lock_guard<std::mutex> lock(g_engineMutex);
        std::string error;
        if (!ensureDataManagerLoaded(config, &error)) {
            writeError(errorBuffer, errorCapacity, error);
            return nullptr;
        }

        return new EngineConfig(std::move(config));
    } catch (const std::exception &exception) {
        writeError(errorBuffer, errorCapacity, exception.what());
        return nullptr;
    } catch (...) {
        writeError(errorBuffer, errorCapacity, "Unknown native error.");
        return nullptr;
    }
}

QA_EXPORT void qa_destroy(void *handle) {
    delete static_cast<EngineConfig *>(handle);
}

QA_EXPORT int qa_choose_move(
        const void *handle,
        const QaPosition *position,
        QaMoveListResult *result,
        char *errorBuffer,
        std::int64_t errorCapacity) {
    try {
        if (handle == nullptr) {
            writeError(errorBuffer, errorCapacity, "Engine handle cannot be null.");
            return 1;
        }
        if (position == nullptr) {
            writeError(errorBuffer, errorCapacity, "Position cannot be null.");
            return 1;
        }
        if (result == nullptr) {
            writeError(errorBuffer, errorCapacity, "Result buffer cannot be null.");
            return 1;
        }

        const EngineConfig &config = *static_cast<const EngineConfig *>(handle);
        std::lock_guard<std::mutex> lock(g_engineMutex);

        std::string error;
        if (!ensureDataManagerLoaded(config, &error)) {
            writeError(errorBuffer, errorCapacity, error);
            return 1;
        }

        std::unique_ptr<Quackle::ComputerPlayer> player =
                createPlayerForDifficulty(config.difficultyKey, &error);
        if (player == nullptr) {
            writeError(errorBuffer, errorCapacity, error);
            return 1;
        }

        Quackle::GamePosition gamePosition;
        if (!buildPosition(position, &gamePosition, &error)) {
            writeError(errorBuffer, errorCapacity, error);
            return 1;
        }

        player->setPosition(gamePosition);
        Quackle::MoveList moves = player->moves(kMaxMoveOptions);
        if (moves.empty()) {
            moves.push_back(Quackle::Move::createPassMove());
        }

        if (!encodeMoveList(gamePosition, moves, result, &error)) {
            writeError(errorBuffer, errorCapacity, error);
            return 1;
        }
        return 0;
    } catch (const std::exception &exception) {
        writeError(errorBuffer, errorCapacity, exception.what());
        return 1;
    } catch (...) {
        writeError(errorBuffer, errorCapacity, "Unknown native error.");
        return 1;
    }
}
