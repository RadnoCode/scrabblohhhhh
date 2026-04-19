package com.kotva.infrastructure.dictionary;

import com.kotva.policy.DictionaryType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Locale;

public class DictionaryLoader {
    private final DictionaryType dictionaryType;

    public DictionaryLoader(DictionaryType dictionaryType) {
        this.dictionaryType = dictionaryType;
    }

    public HashSet<String> load() {
        String dictionaryResourcePath = resolveDictionaryResourcePath(dictionaryType);
        try (InputStream inputStream = DictionaryLoader.class.getResourceAsStream(dictionaryResourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Dictionary resource not found: " + dictionaryResourcePath);
            }

            HashSet<String> dictionary = new HashSet<>();
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toUpperCase(Locale.ROOT);
                    if (!word.isEmpty()) {
                        dictionary.add(word);
                    }
                }
            }
            return dictionary;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dictionary from " + dictionaryResourcePath, e);
        }
    }

    private String resolveDictionaryResourcePath(DictionaryType dictionaryType) {
        return switch (dictionaryType) {
            case AM -> "/Dicts/North-America/NWL2018.txt";
            case BR -> "/Dicts/British/CSW19.txt";
        };
    }
}
