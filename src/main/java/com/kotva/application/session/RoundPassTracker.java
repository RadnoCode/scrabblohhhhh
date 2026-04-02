package com.kotva.application.session;

import java.util.HashSet;
import java.util.Set;

public class RoundPassTracker {
    private final Set<String> passedPlayerIds = new HashSet<>();

    public void markPassed(String playerId) {
        passedPlayerIds.add(playerId);
    }

    public void reset() {
        passedPlayerIds.clear();
    }

    public boolean hasPassed(String playerId) {
        return passedPlayerIds.contains(playerId);
    }
}
