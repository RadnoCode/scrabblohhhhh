package com.kotva.infrastructure.dictionary;

import com.kotva.policy.DictionaryType;
import java.util.Set;

public final class TutorialDictionaryRepository extends DictionaryRepository {
    private static final Set<String> TUTORIAL_ONLY_EXCLUDED_WORDS = Set.of("AH");

    @Override
    public void loadDictionary(DictionaryType dictionaryType) {
        super.loadDictionary(dictionaryType);
        getDictionary().removeAll(TUTORIAL_ONLY_EXCLUDED_WORDS);
    }
}
