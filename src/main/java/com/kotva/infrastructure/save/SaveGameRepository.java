package com.kotva.infrastructure.save;

import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.domain.model.Player;
import com.kotva.mode.GameMode;
import com.kotva.mode.PlayerController;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class SaveGameRepository {
    private static final int CURRENT_ARCHIVE_VERSION = 1;
    private static final String SAVE_DIRECTORY_NAME = ".scrabblohhhhh";
    private static final String SAVE_FILE_NAME = "withfriends-save.kotvasave";

    private final Path saveFile;

    public SaveGameRepository() {
        this(defaultSaveFile());
    }

    public SaveGameRepository(Path saveFile) {
        this.saveFile = Objects.requireNonNull(saveFile, "saveFile cannot be null.");
    }

    public Path getSaveFile() {
        return saveFile;
    }

    public boolean hasHotSeatSave() {
        return Files.isRegularFile(saveFile);
    }

    public void saveHotSeat(GameSession session, GameSessionSnapshot snapshot) {
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        ensureHotSeat(session, snapshot);

        SaveGameArchive archive = new SaveGameArchive(
            CURRENT_ARCHIVE_VERSION,
            System.currentTimeMillis(),
            session,
            snapshot);

        Path parent = saveFile.toAbsolutePath().getParent();
        Path tempFile = saveFile.resolveSibling(saveFile.getFileName() + ".tmp");
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream outputStream =
                new ObjectOutputStream(Files.newOutputStream(tempFile))) {
                outputStream.writeObject(archive);
            }
            moveIntoPlace(tempFile, saveFile);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save game to " + saveFile + ".", exception);
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
        }
    }

    public SaveGameArchive loadHotSeat() {
        if (!hasHotSeatSave()) {
            throw new IllegalStateException("No with-friends save file exists.");
        }

        try (ObjectInputStream inputStream =
            new ObjectInputStream(Files.newInputStream(saveFile))) {
            Object value = inputStream.readObject();
            if (!(value instanceof SaveGameArchive archive)) {
                throw new IllegalStateException("Save file is not a Kotva save archive.");
            }
            if (archive.getVersion() != CURRENT_ARCHIVE_VERSION) {
                throw new IllegalStateException(
                    "Unsupported save version: " + archive.getVersion() + ".");
            }
            ensureHotSeat(archive.getSession(), archive.getSnapshot());
            ensurePlayerControllers(archive.getSession());
            return archive;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load game from " + saveFile + ".", exception);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Save file contains an unknown class.", exception);
        }
    }

    private void moveIntoPlace(Path tempFile, Path targetFile) throws IOException {
        try {
            Files.move(
                tempFile,
                targetFile,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveFailure) {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void ensureHotSeat(GameSession session, GameSessionSnapshot snapshot) {
        if (session.getConfig().getGameMode() != GameMode.HOT_SEAT
            || snapshot.getGameMode() != GameMode.HOT_SEAT) {
            throw new IllegalStateException("Only with-friends games can be saved or loaded.");
        }
        if (!Objects.equals(session.getSessionId(), snapshot.getSessionId())) {
            throw new IllegalStateException("Save archive session and snapshot do not match.");
        }
    }

    private void ensurePlayerControllers(GameSession session) {
        for (Player player : session.getGameState().getPlayers()) {
            if (player.getController() != null
                && player.getController().getType() == player.getPlayerType()) {
                continue;
            }
            player.setController(PlayerController.create(player.getPlayerId(), player.getPlayerType()));
        }
    }

    private static Path defaultSaveFile() {
        return Path.of(
            System.getProperty("user.home"),
            SAVE_DIRECTORY_NAME,
            "saves",
            SAVE_FILE_NAME);
    }
}
