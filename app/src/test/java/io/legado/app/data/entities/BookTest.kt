package io.legado.app.data.entities

import io.legado.app.constant.BookType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Book 数据实体单元测试
 * 测试书籍数据结构核心功能
 */
class BookTest {

    private lateinit var book: Book

    @Before
    fun setUp() {
        book = Book(
            bookUrl = "https://example.com/book/test",
            tocUrl = "https://example.com/book/test/toc",
            origin = "https://source.example.com",
            originName = "测试书源",
            name = "测试书籍",
            author = "测试作者",
            type = BookType.text
        )
    }

    /**
     * 测试基本属性
     */
    @Test
    fun testBasicProperties() {
        assertEquals("https://example.com/book/test", book.bookUrl)
        assertEquals("https://example.com/book/test/toc", book.tocUrl)
        assertEquals("https://source.example.com", book.origin)
        assertEquals("测试书源", book.originName)
        assertEquals("测试书籍", book.name)
        assertEquals("测试作者", book.author)
        assertEquals(BookType.text, book.type)
    }

    /**
     * 测试本地书籍标识
     */
    @Test
    fun testLocalBook() {
        val localBook = Book(
            bookUrl = "/local/path/book.txt",
            origin = BookType.localTag,
            name = "本地书籍",
            author = ""
        )
        assertEquals(BookType.localTag, localBook.origin)
    }

    /**
     * 测试书籍类型
     */
    @Test
    fun testBookTypes() {
        // 文本类型
        book.type = BookType.text
        assertEquals(BookType.text, book.type)

        // 音频类型
        val audioBook = Book(
            bookUrl = "https://audio.example.com/book",
            name = "音频书籍",
            type = BookType.audio
        )
        assertEquals(BookType.audio, audioBook.type)
    }

    /**
     * 测试封面URL
     */
    @Test
    fun testCoverUrl() {
        book.coverUrl = "https://example.com/cover.jpg"
        assertEquals("https://example.com/cover.jpg", book.coverUrl)
        
        // 自定义封面
        book.customCoverUrl = "https://custom.example.com/cover.jpg"
        assertEquals("https://custom.example.com/cover.jpg", book.customCoverUrl)
    }

    /**
     * 测试简介
     */
    @Test
    fun testIntro() {
        book.intro = "这是书籍的简介内容"
        assertEquals("这是书籍的简介内容", book.intro)
        
        // 自定义简介
        book.customIntro = "这是用户自定义的简介"
        assertEquals("这是用户自定义的简介", book.customIntro)
    }

    /**
     * 测试自定义标签
     */
    @Test
    fun testCustomTag() {
        book.customTag = "科幻,推荐"
        assertEquals("科幻,推荐", book.customTag)
    }

    /**
     * 测试字符集（本地书籍）
     */
    @Test
    fun testCharset() {
        book.charset = "UTF-8"
        assertEquals("UTF-8", book.charset)
        
        book.charset = "GBK"
        assertEquals("GBK", book.charset)
    }

    /**
     * 测试章节进度
     */
    @Test
    fun testChapterProgress() {
        book.durChapterIndex = 5
        book.durChapterPos = 1000
        book.durChapterTitle = "第五章 测试"
        
        assertEquals(5, book.durChapterIndex)
        assertEquals(1000, book.durChapterPos)
        assertEquals("第五章 测试", book.durChapterTitle)
    }

    /**
     * 测试总章节数
     */
    @Test
    fun testTotalChapterNum() {
        book.totalChapterNum = 100
        assertEquals(100, book.totalChapterNum)
    }

    /**
     * 测试最新章节信息
     */
    @Test
    fun testLatestChapterInfo() {
        book.latestChapterTitle = "第一百章 最新章节"
        book.latestChapterTime = System.currentTimeMillis()
        
        assertNotNull(book.latestChapterTitle)
        assertNotNull(book.latestChapterTime)
        assertTrue(book.latestChapterTime > 0)
    }

    /**
     * 测试书籍分组
     */
    @Test
    fun testBookGroup() {
        book.group = 1L
        assertEquals(1L, book.group)
    }

    /**
     * 测试最后检查时间
     */
    @Test
    fun testLastCheckTime() {
        book.lastCheckTime = System.currentTimeMillis()
        book.lastCheckCount = 10
        
        assertTrue(book.lastCheckTime > 0)
        assertEquals(10, book.lastCheckCount)
    }

    /**
     * 测试阅读时间
     */
    @Test
    fun testReadTime() {
        val currentTime = System.currentTimeMillis()
        book.durChapterTime = currentTime
        
        assertEquals(currentTime, book.durChapterTime)
    }

    /**
     * 测试书籍URL作为主键
     */
    @Test
    fun testPrimaryKey() {
        // bookUrl作为主键，唯一标识书籍
        val book1 = Book(bookUrl = "https://unique.url/book1", name = "书籍1")
        val book2 = Book(bookUrl = "https://unique.url/book2", name = "书籍2")
        val book3 = Book(bookUrl = "https://unique.url/book1", name = "同名不同书籍")
        
        assertNotEquals(book1.bookUrl, book2.bookUrl)
        assertEquals(book1.bookUrl, book3.bookUrl)
    }

    /**
     * 测试空书籍
     */
    @Test
    fun testEmptyBook() {
        val emptyBook = Book()
        assertEquals("", emptyBook.bookUrl)
        assertEquals("", emptyBook.name)
        assertEquals("", emptyBook.author)
        assertEquals(BookType.localTag, emptyBook.origin)
    }

    /**
     * 测试书籍分类（kind）
     */
    @Test
    fun testBookKind() {
        book.kind = "玄幻,修仙,完结"
        assertEquals("玄幻,修仙,完结", book.kind)
    }

    /**
     * 测试infoHtml缓存
     */
    @Test
    fun testInfoHtmlCache() {
        book.infoHtml = "<html>缓存的内容详情页</html>"
        assertEquals("<html>缓存的内容详情页</html>", book.infoHtml)
    }

    /**
     * 测试tocHtml缓存
     */
    @Test
    fun testTocHtmlCache() {
        book.tocHtml = "<html>缓存的目录页</html>"
        assertEquals("<html>缓存的目录页</html>", book.tocHtml)
    }

    /**
     * 测试canUpdate属性
     */
    @Test
    fun testCanUpdate() {
        book.canUpdate = true
        assertTrue(book.canUpdate == true)
        
        book.canUpdate = false
        assertTrue(book.canUpdate == false)
    }

    /**
     * 测试order属性
     */
    @Test
    fun testOrder() {
        book.order = 5
        assertEquals(5, book.order)
    }

    /**
     * 测试originName与书源名称一致
     */
    @Test
    fun testOriginNameConsistency() {
        val bookFromSource = Book(
            bookUrl = "https://book.example.com/1",
            origin = "https://source.example.com",
            originName = "书源名称"
        )
        assertEquals("书源名称", bookFromSource.originName)
    }

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
     * 测试完整书籍数据
     */
    @Test
    fun testCompleteBookData() {
        val completeBook = Book(
            bookUrl = "https://complete.example.com/book",
            tocUrl = "https://complete.example.com/book/toc",
            origin = "https://source.complete.com",
            originName = "完整书源",
            name = "完整测试书籍",
            author = "完整作者",
            kind = "玄幻",
            customTag = "推荐",
            coverUrl = "https://complete.example.com/cover.jpg",
            intro = "这是完整书籍的简介",
            type = BookType.text,
            group = 1L,
            totalChapterNum = 200,
            durChapterIndex = 50,
            durChapterPos = 5000,
            latestChapterTitle = "第二百章",
            latestChapterTime = System.currentTimeMillis()
        )
        
        assertNotNull(completeBook.bookUrl)
        assertNotNull(completeBook.name)
        assertNotNull(completeBook.author)
        assertEquals(200, completeBook.totalChapterNum)
        assertEquals(50, completeBook.durChapterIndex)
    }
}
