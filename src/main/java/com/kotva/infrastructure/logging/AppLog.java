package com.kotva.infrastructure.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Mirrors runtime output to both the terminal and a live log file so UI errors
 * remain visible even when they are also summarized in the scene.
 */
public final class AppLog {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Object OUTPUT_LOCK = new Object();

    private static volatile Path logPath;

    private AppLog() {
    }

    public static void initialize() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        try {
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            Path activeLogPath = resolveWritableLogPath();
            OutputStream fileStream = Files.newOutputStream(
                    activeLogPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);

            logPath = activeLogPath;
            System.setOut(new PrintStream(
                    new MirroredOutputStream(originalOut, fileStream),
                    true,
                    StandardCharsets.UTF_8));
            System.setErr(new PrintStream(
                    new MirroredOutputStream(originalErr, fileStream),
                    true,
                    StandardCharsets.UTF_8));

            configureRootLogger();
            Logger.getLogger(AppLog.class.getName()).info("Runtime log file: " + logPath.toAbsolutePath());
            logLocalHostAddress();
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to initialize runtime logging.", exception);
        }
    }

    public static void installUncaughtExceptionLogging() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                logException(
                        AppLog.class,
                        "Uncaught exception on thread '" + thread.getName() + "'.",
                        throwable));
    }

    public static Path getLogPath() {
        return logPath;
    }

    public static void logException(Class<?> source, String message, Throwable throwable) {
        Objects.requireNonNull(source, "source cannot be null.");
        Logger.getLogger(source.getName()).log(Level.SEVERE, message, throwable);
    }

    private static void configureRootLogger() {
        System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %4$s %2$s - %5$s%6$s%n");

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);
    }

    private static void logLocalHostAddress() {
        Logger logger = Logger.getLogger(AppLog.class.getName());
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            logger.info(
                    "InetAddress.getLocalHost(): "
                            + localHost
                            + " | hostName="
                            + localHost.getHostName()
                            + " | canonicalHostName="
                            + localHost.getCanonicalHostName()
                            + " | hostAddress="
                            + localHost.getHostAddress());
        } catch (UnknownHostException exception) {
            logger.log(Level.WARNING, "Failed to resolve InetAddress.getLocalHost().", exception);
        }
    }

    private static Path resolveWritableLogPath() throws IOException {
        Path workingDirectory = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath()
                .normalize();
        Path preferredPath = workingDirectory.resolve("target").resolve("app-runtime.log");
        if (prepareLogDirectory(preferredPath)) {
            return preferredPath;
        }

        Path fallbackPath = Path.of(
                System.getProperty("user.home"),
                ".scrabblohhhhh",
                "logs",
                "app-runtime.log");
        if (prepareLogDirectory(fallbackPath)) {
            return fallbackPath;
        }

        throw new IOException("Could not create a writable runtime log directory.");
    }

    private static boolean prepareLogDirectory(Path candidatePath) {
        try {
            Path parent = candidatePath.getParent();
            if (parent == null) {
                return false;
            }
            Files.createDirectories(parent);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static final class MirroredOutputStream extends OutputStream {
        private final OutputStream terminalStream;
        private final OutputStream fileStream;

        private MirroredOutputStream(OutputStream terminalStream, OutputStream fileStream) {
            this.terminalStream = Objects.requireNonNull(terminalStream, "terminalStream cannot be null.");
            this.fileStream = Objects.requireNonNull(fileStream, "fileStream cannot be null.");
        }

        @Override
        public void write(int value) throws IOException {
            synchronized (OUTPUT_LOCK) {
                terminalStream.write(value);
                fileStream.write(value);
            }
        }

        @Override
        public void write(byte[] buffer, int offset, int length) throws IOException {
            synchronized (OUTPUT_LOCK) {
                terminalStream.write(buffer, offset, length);
                fileStream.write(buffer, offset, length);
            }
        }

        @Override
        public void flush() throws IOException {
            synchronized (OUTPUT_LOCK) {
                terminalStream.flush();
                fileStream.flush();
            }
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }
}
