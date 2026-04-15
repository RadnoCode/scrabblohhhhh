package com.kotva.infrastructure.dictionary;

import com.kotva.policy.DictionaryType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DictionaryRepository {
    private DictionaryType loadedDictionaryType;
    private Set<String> dictionary = Collections.emptySet();

    public void loadDictionary(DictionaryType dictionaryType) {
        if (dictionaryType == null) {
            throw new IllegalArgumentException("DictionaryType cannot be null.");
        }

        if (dictionaryType == loadedDictionaryType && !dictionary.isEmpty()) {
            return;
        }

        Set<String> loadedDictionary = new HashSet<>(new DictionaryLoader(dictionaryType).load());
        loadedDictionaryType = dictionaryType;
        dictionary = loadedDictionary;
    }

    public Set<String> getDictionary() {
        ensureDictionaryLoaded();
        return dictionary;
    }

    public DictionaryType getLoadedDictionaryType() {
        return loadedDictionaryType;
    }

    public boolean isAccepted(String word) {
        ensureDictionaryLoaded();
        if (word == null || word.isBlank()) {
            return false;
        }
        return dictionary.contains(word.trim().toUpperCase());
    }

    private void ensureDictionaryLoaded() {
        if (loadedDictionaryType == null) {
            throw new IllegalStateException("No dictionary has been loaded.");
        }
    }
}