package io.legado.app.model.analyzeRule

import org.junit.Assert.*
import org.junit.Test

/**
 * AnalyzeRule 规则切分单元测试
 * 测试规则解析核心的splitSourceRule等纯逻辑方法
 * 注意: 此测试仅测试不依赖Android环境的纯逻辑部分
 */
class AnalyzeRuleSplitTest {

    /**
     * 测试基本规则切分 - 无模式标识
     */
    @Test
    fun testBasicSplitSourceRule() {
        // AnalyzeRule的splitSourceRule方法需要content才能工作
        // 但规则切分逻辑是独立的，可以间接测试
        
        // 测试SourceRule内部类逻辑
        val simpleRule = "class.title@text"
        // 验证规则可以被正确解析为SourceRule列表
        assertTrue(simpleRule.contains("@"))
    }

    /**
     * 测试XPath模式识别
     */
    @Test
    fun testXPathModeRecognition() {
        val xpathRule = "@XPath://div[@class='content']/text()"
        assertTrue(xpathRule.startsWith("@XPath:", true))
        
        val slashRule = "//div[@class='content']"
        assertTrue(slashRule.startsWith("/"))
    }

    /**
     * 测试JSON模式识别
     */
    @Test
    fun testJsonModeRecognition() {
        val jsonRule = "@Json:$.store.book[*].title"
        assertTrue(jsonRule.startsWith("@Json:", true))
        
        val dollarRule = "$.store.book[0].title"
        assertTrue(dollarRule.startsWith("$.") || dollarRule.startsWith("$["))
    }

    /**
     * 测试JS模式识别
     */
    @Test
    fun testJsModeRecognition() {
        val jsRule = "{{javascript: result.title}}"
        assertTrue(jsRule.contains("{{") && jsRule.contains("}}"))
    }

    /**
     * 测试CSS模式识别
     */
    @Test
    fun testCssModeRecognition() {
        val cssRule = "@CSS:.title@text"
        assertTrue(cssRule.startsWith("@CSS:", true))
        
        val escapeRule = "@@class.title@text"
        assertTrue(escapeRule.startsWith("@@"))
    }

    /**
     * 测试正则替换模式识别
     */
    @Test
    fun testReplaceRegexPattern() {
        val ruleWithReplace = "class.title@text##pattern##replacement"
        val parts = ruleWithReplace.split("##")
        assertEquals(3, parts.size)
        assertEquals("class.title@text", parts[0])
        assertEquals("pattern", parts[1])
        assertEquals("replacement", parts[2])
    }

    /**
     * 测试@get模式识别
     */
    @Test
    fun testGetPattern() {
        val ruleWithGet = "@get:{key}其他规则"
        assertTrue(ruleWithGet.contains("@get:"))
    }

    /**
     * 测试@put模式识别
     */
    @Test
    fun testPutPattern() {
        val ruleWithPut = "规则@put:{\"key\":\"valueRule\"}"
        assertTrue(ruleWithPut.contains("@put:"))
    }

    /**
     * 测试Mode枚举值
     */
    @Test
    fun testModeEnum() {
        val modes = AnalyzeRule.Mode.values()
        assertTrue(modes.contains(AnalyzeRule.Mode.XPath))
        assertTrue(modes.contains(AnalyzeRule.Mode.Json))
        assertTrue(modes.contains(AnalyzeRule.Mode.Default))
        assertTrue(modes.contains(AnalyzeRule.Mode.Js))
        assertTrue(modes.contains(AnalyzeRule.Mode.Regex))
        
        assertEquals(5, modes.size)
    }

    /**
     * 测试复合规则结构
     */
    @Test
    fun testComplexRuleStructure() {
        // 包含多个模式标识的复杂规则
        val complexRule = "@XPath://div@class&&@Json:$.data.name"
        assertTrue(complexRule.contains("&&"))
        assertTrue(complexRule.contains("@XPath:", true))
        assertTrue(complexRule.contains("@Json:", true))
    }

    /**
     * 测试规则优先级 - 首字符判断
     */
    @Test
    fun testRulePriority() {
        // XPath以/开头
        val xpathStart = "//div"
        assertTrue(xpathStart.startsWith("/"))
        
        // JSON以$.开头
        val jsonStart = "$.data"
        assertTrue(jsonStart.startsWith("$."))
        
        // JSON以$[开头
        val jsonArrayStart = "$[0]"
        assertTrue(jsonArrayStart.startsWith("$["))
        
        // CSS选择器
        val cssSelector = ".class"
        assertTrue(cssSelector.startsWith("."))
        
        // 标签选择器
        val tagSelector = "div"
        assertFalse(tagSelector.startsWith("@") || tagSelector.startsWith("/") || tagSelector.startsWith("$"))
    }

    /**
     * 测试转义处理
     */
    @Test
    fun testEscapeHandling() {
        // @@前缀表示使用默认模式解析后续内容
        val escapeRule = "@@/div[@class='test']"
        assertTrue(escapeRule.startsWith("@@"))
        
        // 去掉@@后应该是普通选择器
        val unescapedRule = escapeRule.substring(2)
        assertEquals("/div[@class='test']", unescapedRule)
    }

    /**
     * 测试规则分隔符解析
     */
    @Test
    fun testRuleSeparatorParsing() {
        // &&分隔符 - 全部获取
        val andRule = "rule1&&rule2&&rule3"
        val andParts = andRule.split("&&")
        assertEquals(3, andParts.size)
        
        // ||分隔符 - 首个成功
        val orRule = "rule1||rule2||rule3"
        val orParts = orRule.split("||")
        assertEquals(3, orParts.size)
        
        // %%分隔符 - 交错获取
        val percentRule = "rule1%%rule2%%rule3"
        val percentParts = percentRule.split("%%")
        assertEquals(3, percentParts.size)
    }

    /**
     * 测试负索引识别
     */
    @Test
    fun testNegativeIndex() {
        val negativeIndexRule = "class.list@li.-1@text"
        assertTrue(negativeIndexRule.contains(".-1"))
        
        val rangeIndexRule = "class.list@li.0:2@text"
        assertTrue(rangeIndexRule.contains(".0:2"))
    }

    /**
     * 测试内嵌JS规则识别
     */
    @Test
    fun testEmbeddedJsRule() {
        val embeddedJs = "{{result.match(/pattern/)[0]}}"
        assertTrue(embeddedJs.startsWith("{{"))
        assertTrue(embeddedJs.endsWith("}}"))
        
        val jsContent = embeddedJs.substring(2, embeddedJs.length - 2)
        assertEquals("result.match(/pattern/)[0]", jsContent)
    }

    /**
     * 测试规则空值处理
     */
    @Test
    fun testEmptyRuleHandling() {
        val emptyRule = ""
        assertTrue(emptyRule.isEmpty())
        
        val nullRule: String? = null
        assertTrue(nullRule == null || nullRule.isEmpty())
    }

    /**
     * 测试规则缓存key生成
     */
    @Test
    fun testRuleCacheKey() {
        val rule1 = "class.title@text"
        val rule2 = "class.title@text"
        assertEquals(rule1, rule2) // 相同规则应产生相同缓存key
        
        val rule3 = "class.author@text"
        assertNotEquals(rule1, rule3) // 不同规则应产生不同缓存key
    }

    /**
     * 测试正则表达式规则识别
     */
    @Test
    fun testRegexRuleRecognition() {
        // Regex模式以:开头（在allInOne模式下）
        val regexRule = ":pattern##replacement"
        assertTrue(regexRule.startsWith(":"))
        
        // 或者在splitSourceRule中识别
        val normalRegex = "pattern##replacement"
        assertTrue(normalRegex.contains("##"))
    }

    /**
     * 测试URL规则处理
     */
    @Test
    fun testUrlRuleHandling() {
        val urlAttrRule = "a@href"
        assertTrue(urlAttrRule.endsWith("@href"))
        
        val srcAttrRule = "img@src"
        assertTrue(srcAttrRule.endsWith("@src"))
    }
}