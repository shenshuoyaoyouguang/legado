package io.legado.app.data.entities

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * BookChapter 数据实体单元测试
 * 测试章节数据结构和变量管理功能
 */
class BookChapterTest {

    private lateinit var bookChapter: BookChapter

    @Before
    fun setUp() {
        bookChapter = BookChapter(
            url = "https://example.com/chapter/1",
            title = "第一章 测试章节",
            isVolume = false,
            baseUrl = "https://example.com",
            bookUrl = "https://example.com/book/test",
            index = 0,
            isVip = false,
            isPay = false
        )
    }

    /**
     * 测试基本属性
     */
    @Test
    fun testBasicProperties() {
        assertEquals("https://example.com/chapter/1", bookChapter.url)
        assertEquals("第一章 测试章节", bookChapter.title)
        assertFalse(bookChapter.isVolume)
        assertEquals("https://example.com", bookChapter.baseUrl)
        assertEquals("https://example.com/book/test", bookChapter.bookUrl)
        assertEquals(0, bookChapter.index)
        assertFalse(bookChapter.isVip)
        assertFalse(bookChapter.isPay)
    }

    /**
     * 测试hashCode和equals
     */
    @Test
    fun testHashCodeAndEquals() {
        val otherChapter = BookChapter(
            url = "https://example.com/chapter/1",
            title = "不同标题"
        )
        assertEquals(bookChapter.hashCode(), otherChapter.hashCode())
        assertEquals(bookChapter, otherChapter)

        val differentChapter = BookChapter(
            url = "https://example.com/chapter/2"
        )
        assertNotEquals(bookChapter, differentChapter)
    }

    /**
     * 测试primaryStr方法
     */
    @Test
    fun testPrimaryStr() {
        val primaryStr = bookChapter.primaryStr()
        assertEquals("https://example.com/book/testhttps://example.com/chapter/1", primaryStr)
    }

    /**
     * 测试卷名章节
     */
    @Test
    fun testVolumeChapter() {
        val volumeChapter = BookChapter(
            url = "https://example.com/volume/1",
            title = "第一卷",
            isVolume = true
        )
        assertTrue(volumeChapter.isVolume)
    }

    /**
     * 测试VIP章节
     */
    @Test
    fun testVipChapter() {
        val vipChapter = BookChapter(
            url = "https://example.com/chapter/vip",
            title = "VIP章节",
            isVip = true,
            isPay = false
        )
        assertTrue(vipChapter.isVip)
        assertFalse(vipChapter.isPay)
    }

    /**
     * 测试已购买VIP章节
     */
    @Test
    fun testPaidVipChapter() {
        val paidChapter = BookChapter(
            url = "https://example.com/chapter/paid",
            title = "已购买VIP章节",
            isVip = true,
            isPay = true
        )
        assertTrue(paidChapter.isVip)
        assertTrue(paidChapter.isPay)
    }

    /**
     * 测试章节索引
     */
    @Test
    fun testChapterIndex() {
        val chapter = BookChapter(
            url = "https://example.com/chapter/10",
            title = "第十章",
            index = 10
        )
        assertEquals(10, chapter.index)
    }

    /**
     * 测试字数属性
     */
    @Test
    fun testWordCount() {
        bookChapter.wordCount = "3000"
        assertEquals("3000", bookChapter.wordCount)
    }

    /**
     * 测试资源URL属性
     */
    @Test
    fun testResourceUrl() {
        bookChapter.resourceUrl = "https://audio.example.com/chapter1.mp3"
        assertEquals("https://audio.example.com/chapter1.mp3", bookChapter.resourceUrl)
    }

    /**
     * 测试标签属性
     */
    @Test
    fun testTag() {
        bookChapter.tag = "2024-01-01"
        assertEquals("2024-01-01", bookChapter.tag)
    }

    /**
     * 测试章节起始位置（EPUB）
     */
    @Test
    fun testStartEndPosition() {
        bookChapter.start = 100L
        bookChapter.end = 500L
        assertEquals(100L, bookChapter.start)
        assertEquals(500L, bookChapter.end)
    }

    /**
     * 测试fragmentId属性（EPUB）
     */
    @Test
    fun testFragmentId() {
        bookChapter.startFragmentId = "fragment1"
        bookChapter.endFragmentId = "fragment2"
        assertEquals("fragment1", bookChapter.startFragmentId)
        assertEquals("fragment2", bookChapter.endFragmentId)
    }

    /**
     * 测试变量存储
     */
    @Test
    fun testVariableStorage() {
        // 设置变量JSON字符串
        val variableJson = "{\"var1\":\"value1\",\"var2\":\"value2\"}"
        bookChapter.variable = variableJson
        assertEquals(variableJson, bookChapter.variable)
    }

    /**
     * 测试变量Map初始化
     */
    @Test
    fun testVariableMapInitialization() {
        // 变量为空时应该返回空Map
        val emptyVariableChapter = BookChapter(variable = null)
        assertTrue(emptyVariableChapter.variableMap.isEmpty())
        
        // 设置有效JSON变量
        val chapterWithVariable = BookChapter(variable = "{\"key\":\"value\"}")
        val map = chapterWithVariable.variableMap
        assertEquals("value", map["key"])
    }

    /**
     * 测试变量JSON解析
     */
    @Test
    fun testVariableJsonParsing() {
        val chapter = BookChapter(variable = "{\"testKey\":\"testValue\"}")
        assertEquals("testValue", chapter.variableMap["testKey"])
        assertNotNull(chapter.variable)
    }

    /**
     * 测试getVariable方法命中内存变量
     */
    @Test
    fun testGetVariable() {
        val chapter = BookChapter(variable = "{\"myKey\":\"myValue\"}")
        assertEquals("myValue", chapter.getVariable("myKey"))
    }

    /**
     * 测试空值处理
     */
    @Test
    fun testNullHandling() {
        val chapter = BookChapter()
        assertEquals("", chapter.url)
        assertEquals("", chapter.title)
        assertNull(chapter.resourceUrl)
        assertNull(chapter.wordCount)
        assertNull(chapter.tag)
    }

    /**
     * 测试完整章节数据
     */
    @Test
    fun testCompleteChapterData() {
        val completeChapter = BookChapter(
            url = "https://example.com/chapter/full",
            title = "完整测试章节",
            isVolume = false,
            baseUrl = "https://example.com",
            bookUrl = "https://example.com/book/full",
            index = 5,
            isVip = false,
            isPay = false,
            resourceUrl = "https://cdn.example.com/chapter5.txt",
            tag = "2024-03-27",
            wordCount = "5000"
        )

        assertEquals("https://example.com/chapter/full", completeChapter.url)
        assertEquals("完整测试章节", completeChapter.title)
        assertEquals(5, completeChapter.index)
        assertEquals("5000", completeChapter.wordCount)
    }

    /**
     * 测试音频章节（带有resourceUrl）
     */
    @Test
    fun testAudioChapter() {
        val audioChapter = BookChapter(
            url = "https://example.com/audio/chapter1",
            title = "第一章音频",
            resourceUrl = "https://audio-cdn.example.com/chapter1.mp3"
        )
        assertNotNull(audioChapter.resourceUrl)
        assertTrue(audioChapter.resourceUrl!!.endsWith(".mp3"))
    }
}
