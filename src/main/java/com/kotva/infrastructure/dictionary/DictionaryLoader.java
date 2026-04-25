package com.kotva.infrastructure.dictionary;

import com.kotva.policy.DictionaryType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;

public class DictionaryLoader {
    private final DictionaryType dictionaryType;

    public DictionaryLoader(DictionaryType dictionaryType) {
        this.dictionaryType = dictionaryType;
    }

    public HashSet<String> load() {
        String dictionaryPath = resolveDictionaryPath(dictionaryType);
        try (InputStream inputStream = getClass().getResourceAsStream(dictionaryPath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Dictionary resource not found: " + dictionaryPath);
            }
            HashSet<String> dictionary = new HashSet<>();
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
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

    private String resolveDictionaryPath(DictionaryType dictionaryType) {
        return switch (dictionaryType) {
        case AM -> "/Dicts/North-America/NWL2018.txt";
        case BR -> "/Dicts/British/CSW19.txt";
        };
    }
}
