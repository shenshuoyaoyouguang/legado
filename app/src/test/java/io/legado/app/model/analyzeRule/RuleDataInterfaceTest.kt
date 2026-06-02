package io.legado.app.model.analyzeRule

import io.legado.app.data.entities.BookChapter
import io.legado.app.data.entities.BookSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * RuleDataInterface 测试
 * 测试规则数据接口实现类的变量管理功能
 */
class RuleDataInterfaceTest {

    private companion object {
        const val LARGE_VARIABLE_THRESHOLD = 10_000
    }

    private lateinit var bookChapter: BookChapter
    private lateinit var bookSource: BookSource
    private lateinit var ruleData: FakeRuleDataInterface

    private class FakeRuleDataInterface : RuleDataInterface {
        override val variableMap = hashMapOf<String, String>()
        private val bigVariableMap = hashMapOf<String, String>()

        override fun putBigVariable(key: String, value: String?) {
            if (value == null) {
                bigVariableMap.remove(key)
            } else {
                bigVariableMap[key] = value
            }
        }

        override fun getBigVariable(key: String): String? = bigVariableMap[key]

        fun hasBigVariable(key: String): Boolean = bigVariableMap.containsKey(key)
    }

    @Before
    fun setUp() {
        bookChapter = BookChapter(
            url = "https://test.example.com/chapter/1",
            title = "第一章",
            bookUrl = "https://test.example.com/book"
        )
        bookSource = BookSource(
            bookSourceUrl = "https://source.example.com",
            bookSourceName = "测试书源"
        )
        ruleData = FakeRuleDataInterface()
    }

    /**
     * 测试RuleDataInterface变量Map初始化
     */
    @Test
    fun testVariableMapInitialization() {
        // 未设置变量时应为空Map
        assertTrue(ruleData.variableMap.isEmpty())
    }

    /**
     * 测试变量存取
     */
    @Test
    fun testPutAndGetVariable() {
        ruleData.putVariable("testKey", "testValue")
        assertEquals("testValue", ruleData.variableMap["testKey"])
        assertEquals("testValue", ruleData.getVariable("testKey"))
        
        // 更新变量
        ruleData.putVariable("testKey", "newValue")
        assertEquals("newValue", ruleData.variableMap["testKey"])
        assertEquals("newValue", ruleData.getVariable("testKey"))
    }

    /**
     * 测试多变量存储
     */
    @Test
    fun testMultipleVariables() {
        ruleData.putVariable("var1", "value1")
        ruleData.putVariable("var2", "value2")
        ruleData.putVariable("var3", "value3")
        
        assertEquals(3, ruleData.variableMap.size)
        assertEquals("value1", ruleData.variableMap["var1"])
        assertEquals("value2", ruleData.variableMap["var2"])
        assertEquals("value3", ruleData.variableMap["var3"])
    }

    /**
     * 测试空值会删除变量
     */
    @Test
    fun testNullVariableRemovesKey() {
        ruleData.putVariable("nullKey", "value")
        ruleData.putVariable("nullKey", null)

        assertFalse(ruleData.variableMap.containsKey("nullKey"))
        assertEquals("", ruleData.getVariable("nullKey"))
        assertFalse(ruleData.hasBigVariable("nullKey"))
    }

    /**
     * 测试变量覆盖
     */
    @Test
    fun testVariableOverride() {
        ruleData.putVariable("overrideKey", "original")
        ruleData.putVariable("overrideKey", "overridden")
        
        assertEquals("overridden", ruleData.variableMap["overrideKey"])
        assertEquals(1, ruleData.variableMap.size)
    }

    /**
     * 测试特殊字符变量值
     */
    @Test
    fun testSpecialCharacterValues() {
        val specialValue = "包含特殊字符: \n\t\"中文\""
        ruleData.putVariable("specialKey", specialValue)
        
        assertEquals(specialValue, ruleData.variableMap["specialKey"])
    }

    /**
     * 测试超长变量走大变量存储
     */
    @Test
    fun testLargeVariableStoredOutsideVariableMap() {
        val largeValue = "x".repeat(LARGE_VARIABLE_THRESHOLD)
        ruleData.putVariable("largeKey", largeValue)

        assertFalse(ruleData.variableMap.containsKey("largeKey"))
        assertTrue(ruleData.hasBigVariable("largeKey"))
        assertEquals(largeValue, ruleData.getVariable("largeKey"))
    }

    /**
     * 测试小变量会清理大变量存储
     */
    @Test
    fun testSmallVariableClearsBigVariableStorage() {
        val largeValue = "x".repeat(LARGE_VARIABLE_THRESHOLD)
        ruleData.putVariable("largeKey", largeValue)
        ruleData.putVariable("largeKey", "smallValue")

        assertEquals("smallValue", ruleData.variableMap["largeKey"])
        assertFalse(ruleData.hasBigVariable("largeKey"))
        assertEquals("smallValue", ruleData.getVariable("largeKey"))
    }

    @Test
    fun testVariableSizeBoundary() {
        val belowThreshold = "x".repeat(LARGE_VARIABLE_THRESHOLD - 1)
        val atThreshold = "x".repeat(LARGE_VARIABLE_THRESHOLD)
        val aboveThreshold = "x".repeat(LARGE_VARIABLE_THRESHOLD + 1)

        ruleData.putVariable("belowThreshold", belowThreshold)
        assertEquals(belowThreshold, ruleData.variableMap["belowThreshold"])
        assertFalse(ruleData.hasBigVariable("belowThreshold"))

        ruleData.putVariable("atThreshold", atThreshold)
        assertFalse(ruleData.variableMap.containsKey("atThreshold"))
        assertTrue(ruleData.hasBigVariable("atThreshold"))
        assertEquals(atThreshold, ruleData.getVariable("atThreshold"))

        ruleData.putVariable("aboveThreshold", aboveThreshold)
        assertFalse(ruleData.variableMap.containsKey("aboveThreshold"))
        assertTrue(ruleData.hasBigVariable("aboveThreshold"))
        assertEquals(aboveThreshold, ruleData.getVariable("aboveThreshold"))
    }

}
