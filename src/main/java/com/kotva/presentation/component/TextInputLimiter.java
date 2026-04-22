package com.kotva.presentation.component;

import java.util.Objects;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public final class TextInputLimiter {
    private TextInputLimiter() {
    }

    public static void limitCodePoints(TextField textField, int maxCodePoints) {
        Objects.requireNonNull(textField, "textField cannot be null.");
        if (maxCodePoints < 1) {
            throw new IllegalArgumentException("maxCodePoints must be greater than 0.");
        }

        textField.setTextFormatter(new TextFormatter<>(change ->
                countCodePoints(change.getControlNewText()) <= maxCodePoints ? change : null));
    }

    public static int countCodePoints(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.codePointCount(0, text.length());
    }
}
