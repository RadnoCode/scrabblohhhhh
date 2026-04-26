package com.kotva.policy;

/**
 * Difficulty levels supported by the AI player.
 */
public enum AiDifficulty {

    /**
     * Easy AI difficulty.
     */
    EASY("Easy", "EASY"),
    /**
     * Medium AI difficulty.
     */
    MEDIUM("Middle", "MEDIUM"),
    /**
     * Hard AI difficulty.
     */
    HARD("Hard", "HARD");

    private final String setupLabel;
    private final String nativeId;

    AiDifficulty(String setupLabel, String nativeId) {
        this.setupLabel = setupLabel;
        this.nativeId = nativeId;
    }

    /**
     * Returns the label shown in setup screens.
     *
     * @return setup label
     */
    public String getSetupLabel() {
        return setupLabel;
    }

    /**
     * Returns the id used by the native AI engine.
     *
     * @return native difficulty id
     */
    public String getNativeId() {
        return nativeId;
    }

    /**
     * Resolves a difficulty from a setup label.
     *
     * @param label setup label
     * @return matching difficulty
     */
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
