package com.kotva.infrastructure.dictionary;

import com.kotva.policy.DictionaryType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


//Load dict and provide a method to check a wor whether it is accepctable.
public class DictionaryRepository {
    private DictionaryType loadedDictionaryType;
    private HashSet<String> dictionary = Collections.emptySet();

    public void loadDictionary(DictionaryType dictionaryType) {
        if (dictionaryType == null) {
            throw new IllegalArgumentException("DictionaryType cannot be null.");
        }

        if (dictionaryType == loadedDictionaryType && !dictionary.isEmpty()) {
            return;
        }

        loadedDictionaryType = dictionaryType;
        dictionary = new HashSet<>(new DictionaryLoader(dictionaryType).load());
    }

    public HashSet<String> getDictionary() {
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
        if (loadedDictionaryType == null || dictionary.isEmpty()) {
            throw new IllegalStateException("No dictionary has been loaded.");
        }
    }
}
