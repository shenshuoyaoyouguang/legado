package io.legado.app.data.entities

import io.legado.app.data.entities.rule.SearchRule
import io.legado.app.data.entities.rule.BookInfoRule
import io.legado.app.data.entities.rule.ContentRule
import io.legado.app.data.entities.rule.TocRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * BookSource 数据实体单元测试
 * 测试书源数据结构和规则获取方法
 */
class BookSourceTest {

    private companion object {
        const val DEFAULT_RESPOND_TIME_MS = 180_000L
        const val NEW_RESPOND_TIME_MS = 60_000L
    }

    private lateinit var bookSource: BookSource

    @Before
    fun setUp() {
        bookSource = BookSource(
            bookSourceUrl = "https://example.com",
            bookSourceName = "测试书源",
            bookSourceGroup = "测试分组",
            bookSourceType = 0,
            enabled = true,
            searchUrl = "https://example.com/search?key={{key}}&page={{page}}",
            exploreUrl = "https://example.com/explore"
        )
    }

    /**
     * 测试基本属性
     */
    @Test
    fun testBasicProperties() {
        assertEquals("https://example.com", bookSource.bookSourceUrl)
        assertEquals("测试书源", bookSource.bookSourceName)
        assertEquals("测试分组", bookSource.bookSourceGroup)
        assertEquals(0, bookSource.bookSourceType)
        assertTrue(bookSource.enabled)
    }

    @Test
    fun testBookSourceBasicScopeOperations() {
        assertNotNull(bookSource.bookSourceUrl)
        assertNotNull(bookSource.bookSourceName)
    }

    /**
     * 测试getKey方法
     */
    @Test
    fun testGetKey() {
        assertEquals("https://example.com", bookSource.getKey())
    }

    /**
     * 测试getTag方法
     */
    @Test
    fun testGetTag() {
        assertEquals("测试书源", bookSource.getTag())
    }

    /**
     * 测试hashCode和equals
     */
    @Test
    fun testHashCodeAndEquals() {
        val otherSource = BookSource(
            bookSourceUrl = "https://example.com",
            bookSourceName = "其他书源"
        )
        assertEquals(bookSource.hashCode(), otherSource.hashCode())
        assertEquals(bookSource, otherSource)

        val differentSource = BookSource(
            bookSourceUrl = "https://different.com"
        )
        assertNotEquals(bookSource, differentSource)
    }

    /**
     * 测试getSearchRule - 自动创建
     */
    @Test
    fun testGetSearchRuleAutoCreate() {
        // 初始无搜索规则
        assertNull(bookSource.ruleSearch)
        
        // 获取时自动创建
        val searchRule = bookSource.getSearchRule()
        assertNotNull(searchRule)
        assertNotNull(bookSource.ruleSearch)
    }

    /**
     * 测试getSearchRule - 已有规则
     */
    @Test
    fun testGetSearchRuleExisting() {
        val existingRule = SearchRule(bookList = "测试规则")
        bookSource.ruleSearch = existingRule
        
        val searchRule = bookSource.getSearchRule()
        assertEquals(existingRule, searchRule)
    }

    /**
     * 测试getExploreRule
     */
    @Test
    fun testGetExploreRule() {
        val exploreRule = bookSource.getExploreRule()
        assertNotNull(exploreRule)
        assertNotNull(bookSource.ruleExplore)
    }

    /**
     * 测试getBookInfoRule
     */
    @Test
    fun testGetBookInfoRule() {
        val bookInfoRule = bookSource.getBookInfoRule()
        assertNotNull(bookInfoRule)
        assertNotNull(bookSource.ruleBookInfo)
    }

    /**
     * 测试getTocRule
     */
    @Test
    fun testGetTocRule() {
        val tocRule = bookSource.getTocRule()
        assertNotNull(tocRule)
        assertNotNull(bookSource.ruleToc)
    }

    /**
     * 测试getContentRule
     */
    @Test
    fun testGetContentRule() {
        val contentRule = bookSource.getContentRule()
        assertNotNull(contentRule)
        assertNotNull(bookSource.ruleContent)
    }

    /**
     * 测试完整规则配置
     */
    @Test
    fun testFullRuleConfiguration() {
        bookSource.ruleSearch = SearchRule(
            bookList = "class.book-item",
            name = "class.title@text",
            bookUrl = "a@href"
        )
        bookSource.ruleBookInfo = BookInfoRule(
            name = "class.book-name@text",
            author = "class.author@text"
        )
        bookSource.ruleToc = TocRule(
            chapterList = "class.chapter-list@li",
            chapterName = "text"
        )
        bookSource.ruleContent = ContentRule(
            content = "class.content@text"
        )

        assertNotNull(bookSource.ruleSearch)
        assertNotNull(bookSource.ruleBookInfo)
        assertNotNull(bookSource.ruleToc)
        assertNotNull(bookSource.ruleContent)

        assertEquals("class.book-item", bookSource.ruleSearch!!.bookList)
        assertEquals("class.title@text", bookSource.ruleSearch!!.name)
        assertEquals("a@href", bookSource.ruleSearch!!.bookUrl)
    }

    @Test
    fun testRuleConfigurationCompleteness() {
        val completeSource = BookSource(
            bookSourceUrl = "https://complete.example.com",
            bookSourceName = "完整书源",
            ruleSearch = SearchRule(
                bookList = "class.book",
                name = "class.name@text",
                bookUrl = "a@href"
            ),
            ruleBookInfo = BookInfoRule(
                name = "class.title@text",
                author = "class.author@text",
                intro = "class.intro@text"
            ),
            ruleToc = TocRule(
                chapterList = "class.chapter-list@li",
                chapterName = "text",
                chapterUrl = "a@href"
            ),
            ruleContent = ContentRule(
                content = "class.content@text"
            )
        )

        assertNotNull(completeSource.ruleSearch)
        assertNotNull(completeSource.ruleBookInfo)
        assertNotNull(completeSource.ruleToc)
        assertNotNull(completeSource.ruleContent)
        assertEquals("class.book", completeSource.ruleSearch!!.bookList)
        assertEquals("class.title@text", completeSource.ruleBookInfo!!.name)
        assertEquals("class.chapter-list@li", completeSource.ruleToc!!.chapterList)
        assertEquals("class.content@text", completeSource.ruleContent!!.content)
    }

    /**
     * 测试音频书源类型
     */
    @Test
    fun testAudioBookSource() {
        val audioSource = BookSource(
            bookSourceUrl = "https://audio.example.com",
            bookSourceName = "音频书源",
            bookSourceType = 1
        )
        assertEquals(1, audioSource.bookSourceType)
    }

    /**
     * 测试禁用状态
     */
    @Test
    fun testDisabledSource() {
        bookSource.enabled = false
        assertFalse(bookSource.enabled)
    }

    /**
     * 测试自定义排序
    */
    @Test
    fun testCustomOrder() {
        bookSource.customOrder = 100
        assertEquals(100, bookSource.customOrder)
    }

    /**
     * 测试搜索URL配置
     */
    @Test
    fun testSearchUrlConfiguration() {
        assertNotNull(bookSource.searchUrl)
        assertTrue(bookSource.searchUrl!!.contains("{{key}}"))
        assertTrue(bookSource.searchUrl!!.contains("{{page}}"))
    }

    /**
     * 测试响应时间配置
     */
    @Test
    fun testRespondTime() {
        assertEquals(DEFAULT_RESPOND_TIME_MS, bookSource.respondTime)

        bookSource.respondTime = NEW_RESPOND_TIME_MS
        assertEquals(NEW_RESPOND_TIME_MS, bookSource.respondTime)
    }

    /**
     * 测试CookieJar配置
     */
    @Test
    fun testCookieJarConfiguration() {
        bookSource.enabledCookieJar = true
        assertTrue(bookSource.enabledCookieJar!!)
        
        bookSource.enabledCookieJar = false
        assertFalse(bookSource.enabledCookieJar!!)
    }

    /**
     * 测试并发率配置
     */
    @Test
    fun testConcurrentRate() {
        bookSource.concurrentRate = "10/1s"
        assertEquals("10/1s", bookSource.concurrentRate)
    }

    /**
     * 测试请求头配置
     */
    @Test
    fun testHeaderConfiguration() {
        val headerJson = "{\"User-Agent\":\"TestAgent\"}"
        bookSource.header = headerJson
        assertEquals(headerJson, bookSource.header)
    }

    /**
     * 测试JS库配置
     */
    @Test
    fun testJsLibConfiguration() {
        val jsLib = "function helper() { return 'help'; }"
        bookSource.jsLib = jsLib
        assertEquals(jsLib, bookSource.jsLib)
    }

    /**
     * 测试登录URL配置
     */
    @Test
    fun testLoginUrl() {
        bookSource.loginUrl = "https://example.com/login"
        assertEquals("https://example.com/login", bookSource.loginUrl)
    }

    /**
     * 测试注释配置
     */
    @Test
    fun testBookSourceComment() {
        bookSource.bookSourceComment = "这是一个测试书源"
        assertEquals("这是一个测试书源", bookSource.bookSourceComment)
    }
}
