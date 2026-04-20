package com.kotva.infrastructure.dictionary;

import com.kotva.policy.DictionaryType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;

public class DictionaryLoader {
    private final DictionaryType dictionaryType;

    public DictionaryLoader(DictionaryType dictionaryType) {
        this.dictionaryType = dictionaryType;
    }

    public HashSet<String> load() {
        Path dictionaryPath = resolveDictionaryPath(dictionaryType);
        try {
            HashSet<String> dictionary = new HashSet<>();
            for (String line : Files.readAllLines(dictionaryPath, StandardCharsets.UTF_8)) {
                String word = line.trim().toUpperCase(Locale.ROOT);
                if (!word.isEmpty()) {
                    dictionary.add(word);
                }
            }
            return dictionary;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dictionary from " + dictionaryPath, e);
        }
    }

    private Path resolveDictionaryPath(DictionaryType dictionaryType) {
        return switch (dictionaryType) {
        case AM -> Path.of("src/resources/Dicts/North-America/NWL2018.txt");
        case BR -> Path.of("src/resources/Dicts/British/CSW19.txt");
        };
    }
}