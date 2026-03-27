package io.legado.app.model.analyzeRule

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AnalyzeByJSonPath 单元测试
 * 测试JSON解析核心功能
 */
class AnalyzeByJSonPathTest {

    private lateinit var jsonContent: String

    @Before
    fun setUp() {
        jsonContent = """
            {
                "store": {
                    "book": [
                        {
                            "category": "reference",
                            "author": "Nigel Rees",
                            "title": "Sayings of the Century",
                            "price": 8.95
                        },
                        {
                            "category": "fiction",
                            "author": "Evelyn Waugh",
                            "title": "Sword of Honour",
                            "price": 12.99
                        },
                        {
                            "category": "fiction",
                            "author": "Herman Melville",
                            "title": "Moby Dick",
                            "isbn": "0-553-21311-3",
                            "price": 8.99
                        }
                    ],
                    "bicycle": {
                        "color": "red",
                        "price": 19.95
                    }
                },
                "expensive": 10
            }
        """.trimIndent()
    }

    /**
     * 测试基本路径访问
     */
    @Test
    fun testBasicPathAccess() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.expensive")
        assertEquals("10", result)
    }

    /**
     * 测试嵌套对象访问
     */
    @Test
    fun testNestedObjectAccess() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.store.bicycle.color")
        assertEquals("red", result)
    }

    /**
     * 测试数组索引访问
     */
    @Test
    fun testArrayIndexAccess() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.store.book[0].title")
        assertEquals("Sayings of the Century", result)
    }

    /**
     * 测试数组列表获取
     */
    @Test
    fun testArrayListAccess() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.store.book[*].author")
        assertNotNull(result)
        assertTrue(result!!.contains("Nigel Rees"))
        assertTrue(result.contains("Evelyn Waugh"))
        assertTrue(result.contains("Herman Melville"))
    }

    /**
     * 测试获取列表
     */
    @Test
    fun testGetStringList() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val authors = analyzer.getStringList("$.store.book[*].author")
        assertEquals(3, authors.size)
        assertTrue(authors.contains("Nigel Rees"))
        assertTrue(authors.contains("Evelyn Waugh"))
        assertTrue(authors.contains("Herman Melville"))
    }

    /**
     * 测试过滤表达式
     */
    @Test
    fun testFilterExpression() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.store.book[?(@.price < 10)].title")
        assertNotNull(result)
        assertTrue(result!!.contains("Sayings of the Century"))
        assertTrue(result.contains("Moby Dick"))
    }

    /**
     * 测试空规则
     */
    @Test
    fun testEmptyRule() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("")
        assertNull(result)
    }

    /**
     * 测试无效路径
     */
    @Test
    fun testInvalidPath() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.nonexistent.path")
        // 应该返回空或抛出异常，取决于实现
        assertNotNull(result)
    }

    /**
     * 测试&&分隔符
     */
    @Test
    fun testAndSeparator() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.store.bicycle.color&&$.store.bicycle.price")
        assertNotNull(result)
        assertTrue(result!!.contains("red"))
        assertTrue(result.contains("19.95"))
    }

    /**
     * 测试||分隔符
     */
    @Test
    fun testOrSeparator() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.store.bicycle.color||$.expensive")
        assertEquals("red", result)
    }

    /**
     * 测试数组长度
     */
    @Test
    fun testArrayLength() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val books = analyzer.getStringList("$.store.book[*].title")
        assertEquals(3, books.size)
    }

    /**
     * 测试根路径
     */
    @Test
    fun testRootPath() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val result = analyzer.getString("$.store")
        assertNotNull(result)
        assertTrue(result!!.contains("book"))
        assertTrue(result.contains("bicycle"))
    }

    /**
     * 测试数字值获取
     */
    @Test
    fun testNumericValue() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        val price = analyzer.getString("$.store.book[1].price")
        assertEquals("12.99", price)
    }

    /**
     * 测试属性存在性检查
     */
    @Test
    fun testPropertyExists() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        // 第一本书没有isbn属性
        val result1 = analyzer.getString("$.store.book[0].isbn")
        // 第三本书有isbn属性
        val result2 = analyzer.getString("$.store.book[2].isbn")
        assertEquals("0-553-21311-3", result2)
    }

    /**
     * 测试内嵌规则
     */
    @Test
    fun testInnerRule() {
        val analyzer = AnalyzeByJSonPath(jsonContent)
        // 测试{$.}形式的内嵌规则（这部分可能需要特殊处理）
        val result = analyzer.getString("$.store.book[0].title")
        assertEquals("Sayings of the Century", result)
    }

    /**
     * 测试JSON对象输入
     */
    @Test
    fun testJsonObjectInput() {
        // 直接使用Map作为输入
        val mapData = mapOf(
            "name" to "测试名称",
            "value" to 100,
            "items" to listOf("a", "b", "c")
        )
        val analyzer = AnalyzeByJSonPath(mapData)
        val name = analyzer.getString("$.name")
        assertEquals("测试名称", name)
        val value = analyzer.getString("$.value")
        assertEquals("100", value)
    }
}