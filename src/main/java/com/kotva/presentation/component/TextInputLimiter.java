package com.kotva.presentation.component;

import java.util.Objects;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Helps limit text input length.
 */
public final class TextInputLimiter {
    private TextInputLimiter() {
    }

    /**
     * Limits the length of a text field.
     *
     * @param textField the text field to limit
     * @param maxCodePoints the max number of characters
     */
    public static void limitCodePoints(TextField textField, int maxCodePoints) {
        Objects.requireNonNull(textField, "textField cannot be null.");
        if (maxCodePoints < 1) {
            throw new IllegalArgumentException("maxCodePoints must be greater than 0.");
        }

        textField.setTextFormatter(new TextFormatter<>(change ->
                countCodePoints(change.getControlNewText()) <= maxCodePoints ? change : null));
    }

    /**
     * Counts the characters in a string.
     *
     * @return the character count
     */
    public static int countCodePoints(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.codePointCount(0, text.length());
    }
}
