package com.kotva.ai;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class QuackleNativeBridge {
    private static final long ERROR_BUFFER_CAPACITY = 1024L;
    private static final int MAX_MOVE_OPTION_COUNT = 10;
    private static final Set<Path> PRELOADED_NATIVE_DEPENDENCIES =
            ConcurrentHashMap.newKeySet();

    private static final MemoryLayout BOARD_CELL_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("occupied"),
            ValueLayout.JAVA_INT.withName("letter"),
            ValueLayout.JAVA_INT.withName("isBlank"),
            ValueLayout.JAVA_INT.withName("assignedLetter"));

    private static final MemoryLayout INIT_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("dataDir"),
            ValueLayout.ADDRESS.withName("dictionaryId"),
            ValueLayout.ADDRESS.withName("difficultyId"));

    private static final MemoryLayout POSITION_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("boardCells"),
            ValueLayout.ADDRESS.withName("rack"),
            ValueLayout.ADDRESS.withName("unseenTiles"),
            ValueLayout.JAVA_INT.withName("aiScore"),
            ValueLayout.JAVA_INT.withName("opponentScore"));

    private static final MemoryLayout PLACEMENT_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("row"),
            ValueLayout.JAVA_INT.withName("col"),
            ValueLayout.JAVA_INT.withName("letter"),
            ValueLayout.JAVA_INT.withName("isBlank"),
            ValueLayout.JAVA_INT.withName("assignedLetter"));

    private static final MemoryLayout RESULT_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("action"),
            ValueLayout.JAVA_INT.withName("placementCount"),
            ValueLayout.JAVA_INT.withName("score"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_DOUBLE.withName("equity"),
            ValueLayout.JAVA_DOUBLE.withName("win"),
            MemoryLayout.sequenceLayout(7, PLACEMENT_LAYOUT).withName("placements"),
            MemoryLayout.paddingLayout(4));

    private static final MemoryLayout RESULT_LIST_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("moveCount"),
            MemoryLayout.paddingLayout(4),
            MemoryLayout.sequenceLayout(MAX_MOVE_OPTION_COUNT, RESULT_LAYOUT).withName("moves"));

    private static final long INIT_DATA_DIR_OFFSET = offset(INIT_LAYOUT, "dataDir");
    private static final long INIT_DICTIONARY_ID_OFFSET = offset(INIT_LAYOUT, "dictionaryId");
    private static final long INIT_DIFFICULTY_ID_OFFSET = offset(INIT_LAYOUT, "difficultyId");

    private static final long POSITION_BOARD_CELLS_OFFSET = offset(POSITION_LAYOUT, "boardCells");
    private static final long POSITION_RACK_OFFSET = offset(POSITION_LAYOUT, "rack");
    private static final long POSITION_UNSEEN_TILES_OFFSET = offset(POSITION_LAYOUT, "unseenTiles");
    private static final long POSITION_AI_SCORE_OFFSET = offset(POSITION_LAYOUT, "aiScore");
    private static final long POSITION_OPPONENT_SCORE_OFFSET = offset(POSITION_LAYOUT, "opponentScore");

    private static final long BOARD_CELL_OCCUPIED_OFFSET = offset(BOARD_CELL_LAYOUT, "occupied");
    private static final long BOARD_CELL_LETTER_OFFSET = offset(BOARD_CELL_LAYOUT, "letter");
    private static final long BOARD_CELL_IS_BLANK_OFFSET = offset(BOARD_CELL_LAYOUT, "isBlank");
    private static final long BOARD_CELL_ASSIGNED_LETTER_OFFSET = offset(BOARD_CELL_LAYOUT, "assignedLetter");

    private static final long RESULT_ACTION_OFFSET = offset(RESULT_LAYOUT, "action");
    private static final long RESULT_PLACEMENT_COUNT_OFFSET = offset(RESULT_LAYOUT, "placementCount");
    private static final long RESULT_SCORE_OFFSET = offset(RESULT_LAYOUT, "score");
    private static final long RESULT_EQUITY_OFFSET = offset(RESULT_LAYOUT, "equity");
    private static final long RESULT_WIN_OFFSET = offset(RESULT_LAYOUT, "win");
    private static final long RESULT_PLACEMENTS_OFFSET = offset(RESULT_LAYOUT, "placements");
    private static final long RESULT_LIST_MOVE_COUNT_OFFSET = offset(RESULT_LIST_LAYOUT, "moveCount");
    private static final long RESULT_LIST_MOVES_OFFSET = offset(RESULT_LIST_LAYOUT, "moves");

    private static final long PLACEMENT_ROW_OFFSET = offset(PLACEMENT_LAYOUT, "row");
    private static final long PLACEMENT_COL_OFFSET = offset(PLACEMENT_LAYOUT, "col");
    private static final long PLACEMENT_LETTER_OFFSET = offset(PLACEMENT_LAYOUT, "letter");
    private static final long PLACEMENT_IS_BLANK_OFFSET = offset(PLACEMENT_LAYOUT, "isBlank");
    private static final long PLACEMENT_ASSIGNED_LETTER_OFFSET = offset(PLACEMENT_LAYOUT, "assignedLetter");

    private final Path libraryPath;
    private final Path dataDirectory;

    private volatile Bindings bindings;

    public QuackleNativeBridge() {
        this(resolveDefaultLibraryPath(), resolveDefaultDataDirectory());
    }

    public QuackleNativeBridge(Path libraryPath, Path dataDirectory) {
        this.libraryPath = Objects.requireNonNull(libraryPath, "libraryPath cannot be null.")
                .toAbsolutePath()
                .normalize();
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory cannot be null.")
                .toAbsolutePath()
                .normalize();
    }

    public Engine createEngine(DictionaryType dictionaryType, AiDifficulty difficulty) {
        Objects.requireNonNull(dictionaryType, "dictionaryType cannot be null.");
        Objects.requireNonNull(difficulty, "difficulty cannot be null.");

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment init = arena.allocate(INIT_LAYOUT);
            init.set(ValueLayout.ADDRESS, INIT_DATA_DIR_OFFSET, arena.allocateFrom(dataDirectory.toString()));
            init.set(ValueLayout.ADDRESS, INIT_DICTIONARY_ID_OFFSET, arena.allocateFrom(dictionaryType.name()));
            init.set(ValueLayout.ADDRESS, INIT_DIFFICULTY_ID_OFFSET, arena.allocateFrom(difficulty.getNativeId()));

            MemorySegment errorBuffer = arena.allocate(ERROR_BUFFER_CAPACITY).fill((byte) 0);
            MemorySegment engineHandle = (MemorySegment) requireBindings().createHandle.invoke(
                    init,
                    errorBuffer,
                    ERROR_BUFFER_CAPACITY);
            if (isNullAddress(engineHandle)) {
                throw new IllegalStateException(readError(errorBuffer));
            }
            return new Engine(requireBindings(), engineHandle);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to create Quackle engine.", throwable);
        }
    }

    public Path getLibraryPath() {
        return libraryPath;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public void load() {
        requireBindings();
    }

    private Bindings requireBindings() {
        Bindings currentBindings = bindings;
        if (currentBindings != null) {
            return currentBindings;
        }

        synchronized (this) {
            if (bindings != null) {
                return bindings;
            }

            if (!Files.exists(libraryPath)) {
                throw new IllegalStateException("Native library does not exist: " + libraryPath);
            }
            if (!Files.isDirectory(dataDirectory)) {
                throw new IllegalStateException("Quackle data directory does not exist: " + dataDirectory);
            }

            preloadPlatformDependencies();
            Arena libraryArena = Arena.ofShared();
            SymbolLookup lookup = SymbolLookup.libraryLookup(libraryPath, libraryArena);
            Linker linker = Linker.nativeLinker();

            bindings = new Bindings(
                    libraryArena,
                    linker.downcallHandle(
                            lookup.findOrThrow("qa_create"),
                            FunctionDescriptor.of(
                                    ValueLayout.ADDRESS,
                                    ValueLayout.ADDRESS,
                                    ValueLayout.ADDRESS,
                                    ValueLayout.JAVA_LONG)),
                    linker.downcallHandle(
                            lookup.findOrThrow("qa_destroy"),
                            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)),
                    linker.downcallHandle(
                            lookup.findOrThrow("qa_choose_move"),
                            FunctionDescriptor.of(
                                    ValueLayout.JAVA_INT,
                                    ValueLayout.ADDRESS,
                                    ValueLayout.ADDRESS,
                                    ValueLayout.ADDRESS,
                                    ValueLayout.ADDRESS,
                                    ValueLayout.JAVA_LONG)));
            return bindings;
        }
    }

    private void preloadPlatformDependencies() {
        if (!isWindows()) {
            return;
        }

        Path nativeDirectory = libraryPath.getParent();
        if (nativeDirectory == null || !Files.isDirectory(nativeDirectory)) {
            return;
        }

        preloadDependencyIfPresent(nativeDirectory.resolve("libwinpthread-1.dll"));
    }

    private void preloadDependencyIfPresent(Path dependencyPath) {
        Path normalizedPath = dependencyPath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedPath)) {
            return;
        }
        if (!PRELOADED_NATIVE_DEPENDENCIES.add(normalizedPath)) {
            return;
        }

        System.load(normalizedPath.toString());
    }

    private static long offset(MemoryLayout layout, String fieldName) {
        return layout.byteOffset(groupElement(fieldName));
    }

    private static boolean isNullAddress(MemorySegment segment) {
        return segment == null || segment.address() == 0L;
    }

    private static String readError(MemorySegment errorBuffer) {
        String error = errorBuffer.getString(0);
        return error == null || error.isBlank() ? "Unknown native error." : error;
    }

    private static Path resolveDefaultLibraryPath() {
        String configuredPath = System.getenv("QUACKLE_FFM_LIBRARY");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Path.of(configuredPath);
        }

        Path projectRoot = Path.of(System.getProperty("user.dir"));
        String libraryFileName = defaultLibraryFileName();
        Path bundledLibrary = projectRoot.resolve("native").resolve(libraryFileName);
        if (Files.isRegularFile(bundledLibrary)) {
            return bundledLibrary;
        }

        return projectRoot.resolve("target/native").resolve(libraryFileName);
    }

    private static String defaultLibraryFileName() {
        if (isWindows()) {
            return "quackle_ffm.dll";
        }
        if (isMacOs()) {
            return "libquackle_ffm.dylib";
        }
        return "libquackle_ffm.so";
    }

    private static Path resolveDefaultDataDirectory() {
        String configuredPath = System.getenv("QUACKLE_DATA_DIR");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Path.of(configuredPath);
        }

        Path projectRoot = Path.of(System.getProperty("user.dir"));
        Path bundledDataDirectory = projectRoot.resolve("quackle-master").resolve("data");
        if (Files.isDirectory(bundledDataDirectory)) {
            return bundledDataDirectory;
        }

        return projectRoot.resolve("../../quackle-master/data");
    }

    private static boolean isWindows() {
        return osName().contains("win");
    }

    private static boolean isMacOs() {
        String osName = osName();
        return osName.contains("mac") || osName.contains("darwin");
    }

    private static String osName() {
        return System.getProperty("os.name", "").toLowerCase();
    }

    public static final class Engine implements AutoCloseable {
        private final Bindings bindings;
        private MemorySegment handle;
        private boolean closed;

        private Engine(Bindings bindings, MemorySegment handle) {
            this.bindings = Objects.requireNonNull(bindings, "bindings cannot be null.");
            this.handle = Objects.requireNonNull(handle, "handle cannot be null.");
        }

        public synchronized AiMove chooseMove(AiPositionSnapshot snapshot) {
            AiMoveOptionSet moveOptions = chooseMoveOptions(snapshot);
            if (moveOptions.isEmpty()) {
                throw new IllegalStateException("Native AI returned an empty move option set.");
            }
            return moveOptions.moves().get(0);
        }

        public synchronized AiMoveOptionSet chooseMoveOptions(AiPositionSnapshot snapshot) {
            ensureOpen();
            Objects.requireNonNull(snapshot, "snapshot cannot be null.");

            try (Arena arena = Arena.ofConfined()) {
                MemorySegment position = arena.allocate(POSITION_LAYOUT);
                MemorySegment boardCells = arena.allocate(BOARD_CELL_LAYOUT, AiPositionSnapshot.BOARD_CELL_COUNT);
                encodeBoardCells(snapshot.boardCells(), boardCells);

                position.set(ValueLayout.ADDRESS, POSITION_BOARD_CELLS_OFFSET, boardCells);
                position.set(ValueLayout.ADDRESS, POSITION_RACK_OFFSET, arena.allocateFrom(snapshot.rack()));
                position.set(ValueLayout.ADDRESS, POSITION_UNSEEN_TILES_OFFSET, arena.allocateFrom(snapshot.unseenTiles()));
                position.set(ValueLayout.JAVA_INT, POSITION_AI_SCORE_OFFSET, snapshot.aiScore());
                position.set(ValueLayout.JAVA_INT, POSITION_OPPONENT_SCORE_OFFSET, snapshot.opponentScore());

                MemorySegment result = arena.allocate(RESULT_LIST_LAYOUT).fill((byte) 0);
                MemorySegment errorBuffer = arena.allocate(ERROR_BUFFER_CAPACITY).fill((byte) 0);

                int status = (int) bindings.chooseHandle.invoke(
                        handle,
                        position,
                        result,
                        errorBuffer,
                        ERROR_BUFFER_CAPACITY);
                if (status != 0) {
                    throw new IllegalStateException(readError(errorBuffer));
                }

                return decodeMoveOptions(result);
            } catch (RuntimeException exception) {
                throw exception;
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to choose AI move.", throwable);
            }
        }

        @Override
        public synchronized void close() {
            if (closed) {
                return;
            }

            try {
                bindings.destroyHandle.invoke(handle);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to destroy Quackle engine.", throwable);
            } finally {
                closed = true;
                handle = MemorySegment.NULL;
            }
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException("Engine is already closed.");
            }
        }

        private static void encodeBoardCells(
                List<AiPositionSnapshot.BoardCell> boardCells, MemorySegment target) {
            for (int index = 0; index < boardCells.size(); index++) {
                AiPositionSnapshot.BoardCell boardCell = boardCells.get(index);
                MemorySegment cellSegment = target.asSlice(index * BOARD_CELL_LAYOUT.byteSize(), BOARD_CELL_LAYOUT.byteSize());
                cellSegment.set(ValueLayout.JAVA_INT, BOARD_CELL_OCCUPIED_OFFSET, boardCell.occupied() ? 1 : 0);
                cellSegment.set(ValueLayout.JAVA_INT, BOARD_CELL_LETTER_OFFSET, boardCell.occupied() ? boardCell.letter() : 0);
                cellSegment.set(ValueLayout.JAVA_INT, BOARD_CELL_IS_BLANK_OFFSET, boardCell.blank() ? 1 : 0);
                cellSegment.set(
                        ValueLayout.JAVA_INT,
                        BOARD_CELL_ASSIGNED_LETTER_OFFSET,
                        boardCell.assignedLetter() == null ? 0 : Character.toUpperCase(boardCell.assignedLetter()));
            }
        }

        private static AiMove decodeMove(MemorySegment result) {
            int score = result.get(ValueLayout.JAVA_INT, RESULT_SCORE_OFFSET);
            double equity = result.get(ValueLayout.JAVA_DOUBLE, RESULT_EQUITY_OFFSET);
            double win = result.get(ValueLayout.JAVA_DOUBLE, RESULT_WIN_OFFSET);

            int actionValue = result.get(ValueLayout.JAVA_INT, RESULT_ACTION_OFFSET);
            if (actionValue == 0) {
                return new AiMove(AiMove.Action.PASS, List.of(), score, equity, win);
            }

            int placementCount = result.get(ValueLayout.JAVA_INT, RESULT_PLACEMENT_COUNT_OFFSET);
            List<AiMove.Placement> placements = new ArrayList<>(placementCount);
            for (int index = 0; index < placementCount; index++) {
                MemorySegment placement = result.asSlice(
                        RESULT_PLACEMENTS_OFFSET + index * PLACEMENT_LAYOUT.byteSize(),
                        PLACEMENT_LAYOUT.byteSize());
                char letter = (char) placement.get(ValueLayout.JAVA_INT, PLACEMENT_LETTER_OFFSET);
                boolean blank = placement.get(ValueLayout.JAVA_INT, PLACEMENT_IS_BLANK_OFFSET) != 0;
                int assignedLetterValue = placement.get(ValueLayout.JAVA_INT, PLACEMENT_ASSIGNED_LETTER_OFFSET);
                Character assignedLetter = assignedLetterValue == 0 ? null : (char) assignedLetterValue;
                placements.add(new AiMove.Placement(
                        placement.get(ValueLayout.JAVA_INT, PLACEMENT_ROW_OFFSET),
                        placement.get(ValueLayout.JAVA_INT, PLACEMENT_COL_OFFSET),
                        letter,
                        blank,
                        assignedLetter));
            }

            return new AiMove(AiMove.Action.PLACE, placements, score, equity, win);
        }

        private static AiMoveOptionSet decodeMoveOptions(MemorySegment resultList) {
            int moveCount = resultList.get(ValueLayout.JAVA_INT, RESULT_LIST_MOVE_COUNT_OFFSET);
            int boundedMoveCount = Math.max(0, Math.min(moveCount, MAX_MOVE_OPTION_COUNT));
            List<AiMove> moves = new ArrayList<>(boundedMoveCount);
            for (int index = 0; index < boundedMoveCount; index++) {
                MemorySegment moveResult = resultList.asSlice(
                        RESULT_LIST_MOVES_OFFSET + index * RESULT_LAYOUT.byteSize(),
                        RESULT_LAYOUT.byteSize());
                moves.add(decodeMove(moveResult));
            }
            return new AiMoveOptionSet(moves);
        }
    }

    private record Bindings(
            Arena libraryArena,
            MethodHandle createHandle,
            MethodHandle destroyHandle,
            MethodHandle chooseHandle) {
    }
}
