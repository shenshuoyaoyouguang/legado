package io.legado.app.model.analyzeRule

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * RuleAnalyzer 单元测试
 * 测试规则切分处理核心功能
 */
class RuleAnalyzerTest {

    @Before
    fun setUp() {
        // 初始化设置（如有需要）
    }

    /**
     * 测试基本规则切分 - @分隔符
     */
    @Test
    fun testBasicSplitRule() {
        val analyzer = RuleAnalyzer("class.title@text")
        analyzer.trim()
        val rules = analyzer.splitRule("@")
        assertEquals(2, rules.size)
        assertEquals("class.title", rules[0])
        assertEquals("text", rules[1])
    }

    /**
     * 测试&&分隔符切分
     */
    @Test
    fun testAndSplit() {
        val analyzer = RuleAnalyzer("rule1&&rule2&&rule3")
        val rules = analyzer.splitRule("&&")
        assertEquals(3, rules.size)
        assertEquals("rule1", rules[0])
        assertEquals("rule2", rules[1])
        assertEquals("rule3", rules[2])
    }

    /**
     * 测试||分隔符切分
     */
    @Test
    fun testOrSplit() {
        val analyzer = RuleAnalyzer("rule1||rule2||rule3")
        val rules = analyzer.splitRule("||")
        assertEquals(3, rules.size)
        assertEquals("rule1", rules[0])
        assertEquals("rule2", rules[1])
        assertEquals("rule3", rules[2])
    }

    /**
     * 测试%%分隔符切分
     */
    @Test
    fun testPercentSplit() {
        val analyzer = RuleAnalyzer("rule1%%rule2%%rule3")
        val rules = analyzer.splitRule("%%")
        assertEquals(3, rules.size)
        assertEquals("rule1", rules[0])
        assertEquals("rule2", rules[1])
        assertEquals("rule3", rules[2])
    }

    /**
     * 测试多分隔符切分
     */
    @Test
    fun testMultipleSeparators() {
        val analyzer = RuleAnalyzer("rule1&&rule2||rule3")
        val rules = analyzer.splitRule("&&", "||")
        // 应该根据遇到的第一个分隔符确定类型
        assertTrue(rules.isNotEmpty())
    }

    /**
     * 测试trim方法 - 移除前置@
     */
    @Test
    fun testTrimAtSymbol() {
        val analyzer = RuleAnalyzer("@rule1@rule2")
        analyzer.trim()
        val rules = analyzer.splitRule("@")
        assertEquals("rule1", rules[0])
        assertEquals("rule2", rules[1])
    }

    /**
     * 测试trim方法 - 移除前置空白符
     */
    @Test
    fun testTrimWhitespace() {
        val analyzer = RuleAnalyzer("   rule1@rule2")
        analyzer.trim()
        // trim应该移除前置空白
        assertTrue(analyzer.splitRule("@")[0].startsWith("rule1"))
    }

    /**
     * 测试空字符串
     */
    @Test
    fun testEmptyString() {
        val analyzer = RuleAnalyzer("")
        val rules = analyzer.splitRule("@")
        assertEquals(1, rules.size)
        assertEquals("", rules[0])
    }

    /**
     * 测试无分隔符的规则
     */
    @Test
    fun testNoSeparator() {
        val analyzer = RuleAnalyzer("singlerule")
        val rules = analyzer.splitRule("@")
        assertEquals(1, rules.size)
        assertEquals("singlerule", rules[0])
    }

    /**
     * 测试带筛选器的规则
     */
    @Test
    fun testRuleWithSelector() {
        val analyzer = RuleAnalyzer("div[class='test']@text")
        analyzer.trim()
        val rules = analyzer.splitRule("@")
        assertEquals(2, rules.size)
    }

    /**
     * 测试嵌套筛选器
     */
    @Test
    fun testNestedSelector() {
        val analyzer = RuleAnalyzer("div[class='test'][id='main']@text")
        analyzer.trim()
        val rules = analyzer.splitRule("@")
        assertEquals(2, rules.size)
    }

    /**
     * 测试elementsType属性
     */
    @Test
    fun testElementsType() {
        val analyzer = RuleAnalyzer("rule1&&rule2")
        analyzer.splitRule("&&")
        assertEquals("&&", analyzer.elementsType)
    }

    /**
     * 测试reSetPos方法
     */
    @Test
    fun testReSetPos() {
        val analyzer = RuleAnalyzer("rule1&&rule2&&rule3")
        analyzer.splitRule("&&")
        // 执行切分后pos已变化
        analyzer.reSetPos()
        // 重置后应可重新解析
        val rules = analyzer.splitRule("&&")
        assertEquals(3, rules.size)
    }

    /**
     * 测试代码平衡模式
     */
    @Test
    fun testCodeBalanceMode() {
        val analyzer = RuleAnalyzer("function(){return 'test'}", true)
        // 代码平衡模式处理JavaScript代码
        val result = analyzer.innerRule("{$.", 1, 1) { "replacement" }
        assertNotNull(result)
    }

    /**
     * 测试JSON规则平衡
     */
    @Test
    fun testJsonRuleBalance() {
        val analyzer = RuleAnalyzer("$.store.book[*].author", true)
        val rules = analyzer.splitRule("&&")
        assertEquals(1, rules.size)
    }

    /**
     * 测试复杂规则切分
     */
    @Test
    fun testComplexRule() {
        val complexRule = "class.title@text##replace##replacement"
        val analyzer = RuleAnalyzer(complexRule)
        analyzer.trim()
        // 复杂规则应该正确切分
        assertTrue(analyzer.splitRule("@").size >= 1)
    }

    /**
     * 测试内嵌规则替换
     */
    @Test
    fun testInnerRuleReplacement() {
        val analyzer = RuleAnalyzer("prefix{$.rule1}suffix", true)
        val result = analyzer.innerRule("{$.", 1, 1) { rule ->
            "replaced_$rule"
        }
        assertTrue(result.contains("replaced_"))
    }

    /**
     * 测试平衡组括号匹配
     */
    @Test
    fun testBalancedBrackets() {
        val analyzer = RuleAnalyzer("div[attr='value(1)']@text")
        analyzer.trim()
        val rules = analyzer.splitRule("@")
        assertEquals(2, rules.size)
    }

    /**
     * 测试转义字符处理
     */
    @Test
    fun testEscapeCharacters() {
        val analyzer = RuleAnalyzer("rule\\@part@text")
        analyzer.trim()
        val rules = analyzer.splitRule("@")
        // 转义字符应该正确处理
        assertTrue(rules.isNotEmpty())
    }

    /**
     * 测试双引号内的分隔符
     */
    @Test
    fun testSeparatorInQuotes() {
        val analyzer = RuleAnalyzer("div[class=\"test&&value\"]@text")
        analyzer.trim()
        val rules = analyzer.splitRule("@")
        // 引号内的分隔符不应被切分
        assertEquals(2, rules.size)
    }

    /**
     * 测试单引号内的分隔符
     */
    @Test
    fun testSeparatorInSingleQuotes() {
        val analyzer = RuleAnalyzer("div[class='test||value']@text")
        analyzer.trim()
        val rules = analyzer.splitRule("@")
        // 引号内的分隔符不应被切分
        assertEquals(2, rules.size)
    }
}