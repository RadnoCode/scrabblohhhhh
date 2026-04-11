package com.kotva.ai;

import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class AiMoveService implements AutoCloseable {
    private final QuackleNativeBridge.Engine engine;
    private final ExecutorService executor;
    private final AtomicBoolean closing;
    private final AtomicInteger activeRequests;

    public AiMoveService(
            QuackleNativeBridge bridge, DictionaryType dictionaryType, AiDifficulty difficulty) {
        Objects.requireNonNull(bridge, "bridge cannot be null.");
        this.engine = bridge.createEngine(dictionaryType, difficulty);
        this.closing = new AtomicBoolean(false);
        this.activeRequests = new AtomicInteger(0);
        this.executor = Executors.newSingleThreadExecutor(new AiThreadFactory());
    }

    public CompletableFuture<AiMoveOptionSet> requestMove(AiPositionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        ensureOpen();
        return CompletableFuture.supplyAsync(() -> {
            activeRequests.incrementAndGet();
            try {
                return engine.chooseMoveOptions(snapshot);
            } finally {
                if (activeRequests.decrementAndGet() == 0 && closing.get()) {
                    engine.close();
                }
            }
        }, executor);
    }

    @Override
    public void close() {
        if (!closing.compareAndSet(false, true)) {
            return;
        }

        executor.shutdown();
        if (activeRequests.get() == 0) {
            engine.close();
        }
    }

    private void ensureOpen() {
        if (closing.get()) {
            throw new IllegalStateException("AI move service is already closed.");
        }
    }

    private static final class AiThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "quackle-ai-worker");
            thread.setDaemon(true);
            return thread;
        }
    }
}
