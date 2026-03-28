package io.legado.app.model.analyzeRule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AnalyzeRule 规则切分单元测试
 * 直接覆盖真实解析器 splitSourceRule 的行为，而不是验证原始字符串操作。
 */
class AnalyzeRuleSplitTest {

    @Test
    fun testDefaultRuleParsing() {
        val analyzeRule = AnalyzeRule()
        val rules = analyzeRule.splitSourceRule("class.title@text")

        assertEquals(1, rules.size)
        assertEquals(AnalyzeRule.Mode.Default, rules[0].mode)
        assertEquals("class.title@text", rules[0].rule)
    }

    @Test
    fun testXPathRuleParsing() {
        val analyzeRule = AnalyzeRule()

        val prefixedRule = analyzeRule.splitSourceRule("@XPath://div[@class='content']/text()")
        assertEquals(AnalyzeRule.Mode.XPath, prefixedRule[0].mode)
        assertEquals("//div[@class='content']/text()", prefixedRule[0].rule)

        val shorthandRule = analyzeRule.splitSourceRule("//div[@class='content']")
        assertEquals(AnalyzeRule.Mode.XPath, shorthandRule[0].mode)
        assertEquals("//div[@class='content']", shorthandRule[0].rule)
    }

    @Test
    fun testJsonRuleParsing() {
        val analyzeRule = AnalyzeRule()

        val prefixedRule = analyzeRule.splitSourceRule("@Json:$.store.book[*].title")
        assertEquals(AnalyzeRule.Mode.Json, prefixedRule[0].mode)
        assertEquals("$.store.book[*].title", prefixedRule[0].rule)

        val shorthandRule = analyzeRule.splitSourceRule("$.store.book[0].title")
        assertEquals(AnalyzeRule.Mode.Json, shorthandRule[0].mode)
        assertEquals("$.store.book[0].title", shorthandRule[0].rule)
    }

    @Test
    fun testCssPrefixKeepsDefaultMode() {
        val analyzeRule = AnalyzeRule()
        val rules = analyzeRule.splitSourceRule("@CSS:.title@text")

        assertEquals(1, rules.size)
        assertEquals(AnalyzeRule.Mode.Default, rules[0].mode)
        assertEquals("@CSS:.title@text", rules[0].rule)
    }

    @Test
    fun testEscapedPrefixUsesDefaultMode() {
        val analyzeRule = AnalyzeRule()
        val rules = analyzeRule.splitSourceRule("@@/div[@class='test']")

        assertEquals(1, rules.size)
        assertEquals(AnalyzeRule.Mode.Default, rules[0].mode)
        assertEquals("/div[@class='test']", rules[0].rule)
    }

    @Test
    fun testReplacementRuleParsing() {
        val analyzeRule = AnalyzeRule()
        val rule = analyzeRule.splitSourceRule("class.title@text##pattern##replacement").single()

        rule.makeUpRule(null)

        assertEquals(AnalyzeRule.Mode.Default, rule.mode)
        assertEquals("class.title@text", rule.rule)
        assertEquals("pattern", rule.replaceRegex)
        assertEquals("replacement", rule.replacement)
    }

    @Test
    fun testPutRuleParsing() {
        val analyzeRule = AnalyzeRule()
        val rule = analyzeRule.splitSourceRule("class.title@text@put:{\"key\":\"value\"}").single()

        rule.makeUpRule(null)

        assertEquals("class.title@text", rule.rule)
        assertEquals("value", rule.putMap["key"])
    }

    @Test
    fun testGetRuleParsing() {
        val analyzeRule = AnalyzeRule()
        val rule = analyzeRule.splitSourceRule("@get:{token}class.title@text").single()

        assertEquals(AnalyzeRule.Mode.Regex, rule.mode)
        assertTrue(rule.getParamSize() > 0)
        rule.makeUpRule(null)
        assertTrue(rule.rule.endsWith("class.title@text"))
    }

    @Test
    fun testEmbeddedJsParsing() {
        val analyzeRule = AnalyzeRule()
        val rules = analyzeRule.splitSourceRule("prefix{{result.match(/pattern/)[0]}}suffix")

        assertEquals(3, rules.size)
        assertEquals(AnalyzeRule.Mode.Default, rules[0].mode)
        assertEquals("prefix", rules[0].rule)
        assertEquals(AnalyzeRule.Mode.Js, rules[1].mode)
        assertEquals("result.match(/pattern/)[0]", rules[1].rule)
        assertEquals(AnalyzeRule.Mode.Default, rules[2].mode)
        assertEquals("suffix", rules[2].rule)
    }

    @Test
    fun testAllInOneRegexMode() {
        val analyzeRule = AnalyzeRule()
        val rule = analyzeRule.splitSourceRule(":pattern##replacement", true).single()

        assertEquals(AnalyzeRule.Mode.Regex, rule.mode)
        rule.makeUpRule(null)
        assertEquals("pattern", rule.rule)
        assertEquals("replacement", rule.replaceRegex)
    }

    @Test
    fun testMultipleJsSegmentsRemainOrdered() {
        val analyzeRule = AnalyzeRule()
        val rules = analyzeRule.splitSourceRule("{{a}}middle{{b}}")

        assertEquals(3, rules.size)
        assertEquals(AnalyzeRule.Mode.Js, rules[0].mode)
        assertEquals("a", rules[0].rule)
        assertEquals(AnalyzeRule.Mode.Default, rules[1].mode)
        assertEquals("middle", rules[1].rule)
        assertEquals(AnalyzeRule.Mode.Js, rules[2].mode)
        assertEquals("b", rules[2].rule)
    }

    @Test
    fun testEmptyRuleReturnsEmptyList() {
        val analyzeRule = AnalyzeRule()
        assertTrue(analyzeRule.splitSourceRule("").isEmpty())
    }
}
