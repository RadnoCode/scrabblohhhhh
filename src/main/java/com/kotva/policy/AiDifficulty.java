package com.kotva.policy;

public enum AiDifficulty {

    EASY("Easy", "EASY"),
    MEDIUM("Middle", "MEDIUM"),
    HARD("Hard", "HARD");

    private final String setupLabel;
    private final String nativeId;

    AiDifficulty(String setupLabel, String nativeId) {
        this.setupLabel = setupLabel;
        this.nativeId = nativeId;
    }

    public String getSetupLabel() {
        return setupLabel;
    }

    public String getNativeId() {
        return nativeId;
    }

    public static AiDifficulty fromSetupLabel(String label) {
        if (label == null) {
            throw new IllegalArgumentException("label cannot be null.");
        }

        for (AiDifficulty value : values()) {
            if (value.setupLabel.equalsIgnoreCase(label)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unsupported AI difficulty label: " + label);
    }
}