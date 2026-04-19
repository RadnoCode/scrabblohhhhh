/**
 * 包作用：基础设施测试包，负责验证词典加载与查询行为。
 * 包含类：DictionaryRepositorySmokeTest。
 */
package com.kotva.infrastructure.dictionary;

import static org.junit.Assert.assertTrue;

import com.kotva.policy.DictionaryType;
import org.junit.Test;

/**
 * 类作用：测试词典仓储的基础加载能力。
 * 包含方法：americanDictionaryLoadsAndContainsBook、britishDictionaryLoadsAndContainsBook。
 * 继承/实现：无。
 * 引用类：DictionaryType 用于区分词典类型；Test 用于标记测试方法。
 */
public class DictionaryRepositorySmokeTest {
    /**
     * 方法作用：测试方法：验证 americanDictionaryLoadsAndContainsBook 对应的业务场景。
     */
    @Test
    public void americanDictionaryLoadsAndContainsBook() {
        DictionaryRepository repository = new DictionaryRepository();
        repository.loadDictionary(DictionaryType.AM);

        assertTrue(!repository.getDictionary().isEmpty());
        assertTrue(repository.isAccepted("BOOK"));
    }

    /**
     * 方法作用：测试方法：验证 britishDictionaryLoadsAndContainsBook 对应的业务场景。
     */
    @Test
    public void britishDictionaryLoadsAndContainsBook() {
        DictionaryRepository repository = new DictionaryRepository();
        repository.loadDictionary(DictionaryType.BR);

        assertTrue(!repository.getDictionary().isEmpty());
        assertTrue(repository.isAccepted("BOOK"));
    }
}
