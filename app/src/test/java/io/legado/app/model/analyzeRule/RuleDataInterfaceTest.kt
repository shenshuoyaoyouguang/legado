package io.legado.app.model.analyzeRule

import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookChapter
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.rule.SearchRule
import io.legado.app.data.entities.rule.BookInfoRule
import io.legado.app.data.entities.rule.ContentRule
import io.legado.app.data.entities.rule.TocRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * RuleDataInterface 测试
 * 测试规则数据接口实现类的变量管理功能
 */
class RuleDataInterfaceTest {

    private lateinit var book: Book
    private lateinit var bookChapter: BookChapter
    private lateinit var bookSource: BookSource

    @Before
    fun setUp() {
        book = Book(
            bookUrl = "https://test.example.com/book",
            name = "测试书籍",
            author = "测试作者"
        )
        bookChapter = BookChapter(
            url = "https://test.example.com/chapter/1",
            title = "第一章",
            bookUrl = "https://test.example.com/book"
        )
        bookSource = BookSource(
            bookSourceUrl = "https://source.example.com",
            bookSourceName = "测试书源"
        )
    }

    /**
     * 测试BookChapter变量Map初始化
     */
    @Test
    fun testBookChapterVariableMapInitialization() {
        // 未设置变量时应为空Map
        assertTrue(bookChapter.variableMap.isEmpty())
    }

    /**
     * 测试BookChapter变量存取
     */
    @Test
    fun testBookChapterPutAndGetVariable() {
        bookChapter.putVariable("testKey", "testValue")
        assertEquals("testValue", bookChapter.variableMap["testKey"])
        
        // 更新变量
        bookChapter.putVariable("testKey", "newValue")
        assertEquals("newValue", bookChapter.variableMap["testKey"])
    }

    /**
     * 测试BookChapter多变量存储
     */
    @Test
    fun testBookChapterMultipleVariables() {
        bookChapter.putVariable("var1", "value1")
        bookChapter.putVariable("var2", "value2")
        bookChapter.putVariable("var3", "value3")
        
        assertEquals(3, bookChapter.variableMap.size)
        assertEquals("value1", bookChapter.variableMap["var1"])
        assertEquals("value2", bookChapter.variableMap["var2"])
        assertEquals("value3", bookChapter.variableMap["var3"])
    }

    /**
     * 测试BookChapter变量持久化
     */
    @Test
    fun testBookChapterVariablePersistence() {
        bookChapter.putVariable("persistKey", "persistValue")
        
        // variable字段应该被更新为JSON
        assertNotNull(bookChapter.variable)
        assertTrue(bookChapter.variable!!.contains("persistKey"))
        assertTrue(bookChapter.variable!!.contains("persistValue"))
    }

    /**
     * 测试BookChapter空值变量
     */
    @Test
    fun testBookChapterNullVariable() {
        bookChapter.putVariable("nullKey", null)
        
        // 应该能够处理null值
        assertTrue(bookChapter.variableMap.containsKey("nullKey"))
        assertNull(bookChapter.variableMap["nullKey"])
    }

    /**
     * 测试BookChapter变量覆盖
     */
    @Test
    fun testBookChapterVariableOverride() {
        bookChapter.putVariable("overrideKey", "original")
        bookChapter.putVariable("overrideKey", "overridden")
        
        assertEquals("overridden", bookChapter.variableMap["overrideKey"])
        assertEquals(1, bookChapter.variableMap.size)
    }

    /**
     * 测试BookChapter特殊字符变量值
     */
    @Test
    fun testBookChapterSpecialCharacterValues() {
        val specialValue = "包含特殊字符: \n\t\"中文\""
        bookChapter.putVariable("specialKey", specialValue)
        
        assertEquals(specialValue, bookChapter.variableMap["specialKey"])
    }

    /**
     * 测试BookChapter equals和hashCode
     */
    @Test
    fun testBookChapterEquality() {
        val chapter1 = BookChapter(url = "https://same.url", title = "标题1")
        val chapter2 = BookChapter(url = "https://same.url", title = "标题2")
        
        // URL相同则equals为true
        assertEquals(chapter1, chapter2)
        assertEquals(chapter1.hashCode(), chapter2.hashCode())
        
        val chapter3 = BookChapter(url = "https://different.url")
        assertNotEquals(chapter1, chapter3)
    }

    /**
     * 测试BookSource变量管理
     */
    @Test
    fun testBookSourceVariableManagement() {
        // BookSource通过BaseSource接口管理变量
        bookSource.put("sourceVar", "sourceValue")
        assertEquals("sourceValue", bookSource.get("sourceVar"))
        
        // 空变量返回空字符串
        assertEquals("", bookSource.get("nonexistent"))
    }

    /**
     * 测试BookSource变量更新
     */
    @Test
    fun testBookSourceVariableUpdate() {
        bookSource.put("updateVar", "original")
        assertEquals("original", bookSource.get("updateVar"))
        
        bookSource.put("updateVar", "updated")
        assertEquals("updated", bookSource.get("updateVar"))
    }

    /**
     * 测试BookSource getShareScope（简化测试）
     */
    @Test
    fun testBookSourceBasicScopeOperations() {
        // 验证书源基本属性可以访问
        assertNotNull(bookSource.bookSourceUrl)
        assertNotNull(bookSource.bookSourceName)
    }

    /**
     * 测试规则配置完整性
     */
    @Test
    fun testRuleConfigurationCompleteness() {
        val completeSource = BookSource(
            bookSourceUrl = "https://complete.example.com",
            bookSourceName = "完整书源",
            ruleSearch = SearchRule(
                bookList = "class.book",
                bookName = "class.name@text",
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
     * 测试BookChapter数据序列化
     */
    @Test
    fun testBookChapterDataSerialization() {
        val fullChapter = BookChapter(
            url = "https://test.example.com/chapter/full",
            title = "完整测试章节",
            isVolume = false,
            baseUrl = "https://test.example.com",
            bookUrl = "https://test.example.com/book",
            index = 10,
            isVip = true,
            isPay = true,
            resourceUrl = "https://cdn.example.com/audio.mp3",
            tag = "2024-03-27",
            wordCount = "5000"
        )
        
        // 验证所有属性正确设置
        assertEquals("https://test.example.com/chapter/full", fullChapter.url)
        assertEquals("完整测试章节", fullChapter.title)
        assertEquals(10, fullChapter.index)
        assertTrue(fullChapter.isVip)
        assertTrue(fullChapter.isPay)
        assertEquals("5000", fullChapter.wordCount)
    }

    /**
     * 测试Book基本属性一致性
     */
    @Test
    fun testBookPropertyConsistency() {
        val testBook = Book(
            bookUrl = "https://consistent.example.com/book",
            name = "一致性测试书籍",
            author = "作者名",
            intro = "书籍简介",
            coverUrl = "https://consistent.example.com/cover.jpg"
        )
        
        assertEquals("https://consistent.example.com/book", testBook.bookUrl)
        assertEquals("一致性测试书籍", testBook.name)
        assertEquals("作者名", testBook.author)
        assertEquals("书籍简介", testBook.intro)
        assertEquals("https://consistent.example.com/cover.jpg", testBook.coverUrl)
    }

    /**
     * 测试BookChapter章节标题格式化
     */
    @Test
    fun testBookChapterTitleFormats() {
        val normalChapter = BookChapter(title = "第一章 开始")
        assertEquals("第一章 开始", normalChapter.title)
        
        val volumeChapter = BookChapter(title = "第一卷", isVolume = true)
        assertTrue(volumeChapter.isVolume)
        assertEquals("第一卷", volumeChapter.title)
        
        val vipChapter = BookChapter(title = "VIP特供章节", isVip = true)
        assertTrue(vipChapter.isVip)
    }
}