
package com.kotva.infrastructure.dictionary;

import static org.junit.Assert.assertTrue;

import com.kotva.policy.DictionaryType;
import org.junit.Test;

public class DictionaryRepositorySmokeTest {

        @Test
    public void americanDictionaryLoadsAndContainsBook() {
        DictionaryRepository repository = new DictionaryRepository();
        repository.loadDictionary(DictionaryType.AM);

        assertTrue(!repository.getDictionary().isEmpty());
        assertTrue(repository.isAccepted("BOOK"));
    }

        @Test
    public void britishDictionaryLoadsAndContainsBook() {
        DictionaryRepository repository = new DictionaryRepository();
        repository.loadDictionary(DictionaryType.BR);

        assertTrue(!repository.getDictionary().isEmpty());
        assertTrue(repository.isAccepted("BOOK"));
    }
}