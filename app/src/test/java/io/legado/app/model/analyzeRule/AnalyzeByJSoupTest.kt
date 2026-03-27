package io.legado.app.model.analyzeRule

import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AnalyzeByJSoup 单元测试
 * 测试HTML解析核心功能
 */
class AnalyzeByJSoupTest {

    private lateinit var htmlContent: String
    private lateinit var docElement: org.jsoup.nodes.Element

    @Before
    fun setUp() {
        htmlContent = """
            <html>
            <head><title>测试页面</title></head>
            <body>
                <div id="content">
                    <h1 class="title">标题文本</h1>
                    <p class="intro">介绍文本</p>
                    <ul class="list">
                        <li>项目1</li>
                        <li>项目2</li>
                        <li>项目3</li>
                    </ul>
                    <a href="https://example.com/link1">链接1</a>
                    <a href="https://example.com/link2">链接2</a>
                </div>
            </body>
            </html>
        """.trimIndent()
        docElement = Jsoup.parse(htmlContent)
    }

    /**
     * 测试基本元素解析
     */
    @Test
    fun testBasicElementParsing() {
        val analyzer = AnalyzeByJSoup(docElement)
        val result = analyzer.getString("h1@text")
        assertEquals("标题文本", result)
    }

    /**
     * 测试CSS选择器
     */
    @Test
    fun testCssSelector() {
        val analyzer = AnalyzeByJSoup(docElement)
        val result = analyzer.getString(".title@text")
        assertEquals("标题文本", result)
    }

    /**
     * 测试ID选择器
     */
    @Test
    fun testIdSelector() {
        val analyzer = AnalyzeByJSoup(docElement)
        val result = analyzer.getString("#content@h1@text")
        assertEquals("标题文本", result)
    }

    /**
     * 测试获取列表
     */
    @Test
    fun testGetElementsList() {
        val analyzer = AnalyzeByJSoup(docElement)
        val elements = analyzer.getElements("ul.list@li")
        assertEquals(3, elements.size)
    }

    /**
     * 测试获取文本列表
     */
    @Test
    fun testGetStringList() {
        val analyzer = AnalyzeByJSoup(docElement)
        val textList = analyzer.getStringList("ul.list@li@text")
        assertEquals(3, textList.size)
        assertEquals("项目1", textList[0])
        assertEquals("项目2", textList[1])
        assertEquals("项目3", textList[2])
    }

    /**
     * 测试属性获取
     */
    @Test
    fun testAttributeGet() {
        val analyzer = AnalyzeByJSoup(docElement)
        val hrefList = analyzer.getStringList("a@href")
        assertTrue(hrefList.contains("https://example.com/link1"))
        assertTrue(hrefList.contains("https://example.com/link2"))
    }

    /**
     * 测试空规则
     */
    @Test
    fun testEmptyRule() {
        val analyzer = AnalyzeByJSoup(docElement)
        val result = analyzer.getString("")
        assertNull(result)
    }

    /**
     * 测试HTML获取
     */
    @Test
    fun testHtmlGet() {
        val analyzer = AnalyzeByJSoup(docElement)
        val html = analyzer.getString("h1@html")
        assertNotNull(html)
        assertTrue(html!!.contains("标题文本"))
    }

    /**
     * 测试ownText获取
     */
    @Test
    fun testOwnTextGet() {
        val analyzer = AnalyzeByJSoup(docElement)
        val ownText = analyzer.getStringList("h1@ownText")
        assertEquals(1, ownText.size)
        assertEquals("标题文本", ownText[0])
    }

    /**
     * 测试索引选择
     */
    @Test
    fun testIndexSelection() {
        val analyzer = AnalyzeByJSoup(docElement)
        // 获取第一个li元素
        val firstItem = analyzer.getString("ul.list@li.0@text")
        assertEquals("项目1", firstItem)
    }

    /**
     * 测试负索引选择
     */
    @Test
    fun testNegativeIndexSelection() {
        val analyzer = AnalyzeByJSoup(docElement)
        // 获取最后一个li元素
        val lastItem = analyzer.getString("ul.list@li.-1@text")
        assertEquals("项目3", lastItem)
    }

    /**
     * 测试&&分隔符(全部匹配)
     */
    @Test
    fun testAndSeparator() {
        val analyzer = AnalyzeByJSoup(docElement)
        val results = analyzer.getStringList("h1@text&&p@text")
        assertEquals(2, results.size)
    }

    /**
     * 测试||分隔符(首个匹配)
     */
    @Test
    fun testOrSeparator() {
        val analyzer = AnalyzeByJSoup(docElement)
        val results = analyzer.getStringList("h1@text||p@text")
        assertEquals(1, results.size)
        assertEquals("标题文本", results[0])
    }

    /**
     * 测试CSS特殊规则
     */
    @Test
    fun testCssSpecialRule() {
        val analyzer = AnalyzeByJSoup(docElement)
        val results = analyzer.getStringList("@CSS:.title@text")
        assertEquals(1, results.size)
        assertEquals("标题文本", results[0])
    }

    /**
     * 测试嵌套选择
     */
    @Test
    fun testNestedSelection() {
        val analyzer = AnalyzeByJSoup(docElement)
        val result = analyzer.getString("#content@div@h1@text")
        // 由于嵌套结构，需要验证结果
        assertNotNull(result)
    }

    /**
     * 测试XML解析
     */
    @Test
    fun testXmlParsing() {
        val xmlContent = "<?xml version=\"1.0\"?><root><item>XML内容</item></root>"
        val analyzer = AnalyzeByJSoup(xmlContent)
        val result = analyzer.getString("item@text")
        assertEquals("XML内容", result)
    }

    /**
     * 测试Element输入
     */
    @Test
    fun testElementInput() {
        val element = docElement.selectFirst("#content")
        assertNotNull(element)
        val analyzer = AnalyzeByJSoup(element!!)
        val result = analyzer.getString("h1@text")
        assertEquals("标题文本", result)
    }
}