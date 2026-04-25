package com.kotva.policy;

import java.io.Serializable;

public enum GameRuleset implements Serializable {
    TRADITIONAL_SCRABBLE("Traditional Scrabble"),
    SCRIBBLE("Scribble");

    private final String setupLabel;

    GameRuleset(String setupLabel) {
        this.setupLabel = setupLabel;
    }

    public String getSetupLabel() {
        return setupLabel;
    }

    public boolean isScribbleRuleset() {
        return this == SCRIBBLE;
    }

    public static GameRuleset fromSetupLabel(String setupLabel) {
        if (setupLabel == null || setupLabel.isBlank()) {
            return TRADITIONAL_SCRABBLE;
        }

        String normalizedLabel = setupLabel.trim();
        for (GameRuleset ruleset : values()) {
            if (ruleset.setupLabel.equalsIgnoreCase(normalizedLabel)
                || ruleset.name().equalsIgnoreCase(normalizedLabel)) {
                return ruleset;
            }
        }
        return TRADITIONAL_SCRABBLE;
    }
}
