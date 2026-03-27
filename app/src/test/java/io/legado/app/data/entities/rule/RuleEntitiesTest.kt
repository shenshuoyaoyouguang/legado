package io.legado.app.model.analyzeRule

import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.rule.*
import org.junit.Assert.*
import org.junit.Test

/**
 * 书源规则实体单元测试
 * 测试各种规则数据结构的正确性
 */
class RuleEntitiesTest {

    /**
     * 测试SearchRule基本属性
     */
    @Test
    fun testSearchRuleBasicProperties() {
        val searchRule = SearchRule(
            bookList = "class.book-item",
            bookName = "class.title@text",
            author = "class.author@text",
            bookUrl = "a@href",
            coverUrl = "img@src",
            intro = "class.intro@text",
            kind = "class.category@text",
            lastChapterTitle = "class.latest@text",
            wordCount = "class.words@text"
        )
        
        assertEquals("class.book-item", searchRule.bookList)
        assertEquals("class.title@text", searchRule.bookName)
        assertEquals("class.author@text", searchRule.author)
        assertEquals("a@href", searchRule.bookUrl)
        assertEquals("img@src", searchRule.coverUrl)
        assertEquals("class.intro@text", searchRule.intro)
        assertEquals("class.category@text", searchRule.kind)
        assertEquals("class.latest@text", searchRule.lastChapterTitle)
        assertEquals("class.words@text", searchRule.wordCount)
    }

    /**
     * 测试BookInfoRule基本属性
     */
    @Test
    fun testBookInfoRuleBasicProperties() {
        val bookInfoRule = BookInfoRule(
            name = "h1.title@text",
            author = "span.author@text",
            coverUrl = "img.cover@src",
            intro = "div.intro@html",
            kind = "span.category@text",
            lastChapterTitle = "a.latest@text",
            tocUrl = "a.toc@href",
            wordCount = "span.words@text",
            canReName = "true"
        )
        
        assertEquals("h1.title@text", bookInfoRule.name)
        assertEquals("span.author@text", bookInfoRule.author)
        assertEquals("img.cover@src", bookInfoRule.coverUrl)
        assertEquals("div.intro@html", bookInfoRule.intro)
        assertEquals("span.category@text", bookInfoRule.kind)
        assertEquals("a.latest@text", bookInfoRule.lastChapterTitle)
        assertEquals("a.toc@href", bookInfoRule.tocUrl)
        assertEquals("span.words@text", bookInfoRule.wordCount)
        assertEquals("true", bookInfoRule.canReName)
    }

    /**
     * 测试TocRule基本属性
     */
    @Test
    fun testTocRuleBasicProperties() {
        val tocRule = TocRule(
            chapterList = "ul.chapters@li",
            chapterName = "text",
            chapterUrl = "a@href",
            isVolume = "class.volume",
            updateTime = "span.time@text",
            isVip = "class.vip",
            isPay = "class.paid",
            nextTocUrl = "a.next-page@href"
        )
        
        assertEquals("ul.chapters@li", tocRule.chapterList)
        assertEquals("text", tocRule.chapterName)
        assertEquals("a@href", tocRule.chapterUrl)
        assertEquals("class.volume", tocRule.isVolume)
        assertEquals("span.time@text", tocRule.updateTime)
        assertEquals("class.vip", tocRule.isVip)
        assertEquals("class.paid", tocRule.isPay)
        assertEquals("a.next-page@href", tocRule.nextTocUrl)
    }

    /**
     * 测试ContentRule基本属性
     */
    @Test
    fun testContentRuleBasicProperties() {
        val contentRule = ContentRule(
            content = "div.content@html",
            nextContentUrl = "a.next@href",
            webJs = "scrollToBottom()",
            sourceRegex = "regex_pattern",
            replaceRegex = "##old##new",
            imageStyle = "full",
            payAction = "clickPayButton"
        )
        
        assertEquals("div.content@html", contentRule.content)
        assertEquals("a.next@href", contentRule.nextContentUrl)
        assertEquals("scrollToBottom()", contentRule.webJs)
        assertEquals("regex_pattern", contentRule.sourceRegex)
        assertEquals("##old##new", contentRule.replaceRegex)
        assertEquals("full", contentRule.imageStyle)
        assertEquals("clickPayButton", contentRule.payAction)
    }

    /**
     * 测试ExploreRule基本属性
     */
    @Test
    fun testExploreRuleBasicProperties() {
        val exploreRule = ExploreRule(
            bookList = "div.explore-list@div.book",
            bookName = "h3@text",
            author = "span.author@text",
            bookUrl = "a@href",
            coverUrl = "img@src",
            intro = "p.intro@text",
            kind = "span.tag@text",
            lastChapterTitle = "span.chapter@text",
            wordCount = "span.words@text"
        )
        
        assertEquals("div.explore-list@div.book", exploreRule.bookList)
        assertEquals("h3@text", exploreRule.bookName)
        assertEquals("span.author@text", exploreRule.author)
        assertEquals("a@href", exploreRule.bookUrl)
    }

    /**
     * 测试ReviewRule基本属性
     */
    @Test
    fun testReviewRuleBasicProperties() {
        val reviewRule = ReviewRule(
            reviewList = "div.comments@div.review",
            reviewContent = "p.content@text",
            reviewAuthor = "span.user@text",
            reviewTime = "span.time@text",
            reviewRating = "span.rating@text",
            reviewTitle = "h4.title@text",
            reviewUrl = "a@href"
        )
        
        assertEquals("div.comments@div.review", reviewRule.reviewList)
        assertEquals("p.content@text", reviewRule.reviewContent)
        assertEquals("span.user@text", reviewRule.reviewAuthor)
        assertEquals("span.time@text", reviewRule.reviewTime)
        assertEquals("span.rating@text", reviewRule.reviewRating)
        assertEquals("h4.title@text", reviewRule.reviewTitle)
        assertEquals("a@href", reviewRule.reviewUrl)
    }

    /**
     * 测试空规则创建
     */
    @Test
    fun testEmptyRuleCreation() {
        val emptySearchRule = SearchRule()
        assertNull(emptySearchRule.bookList)
        assertNull(emptySearchRule.bookName)
        assertNull(emptySearchRule.author)
        assertNull(emptySearchRule.bookUrl)
        
        val emptyBookInfoRule = BookInfoRule()
        assertNull(emptyBookInfoRule.name)
        assertNull(emptyBookInfoRule.author)
        assertNull(emptyBookInfoRule.coverUrl)
        
        val emptyTocRule = TocRule()
        assertNull(emptyTocRule.chapterList)
        assertNull(emptyTocRule.chapterName)
        assertNull(emptyTocRule.chapterUrl)
        
        val emptyContentRule = ContentRule()
        assertNull(emptyContentRule.content)
        assertNull(emptyContentRule.nextContentUrl)
    }

    /**
     * 测试规则组合完整性
     */
    @Test
    fun testCompleteRuleSet() {
        val bookSource = BookSource(
            bookSourceUrl = "https://ruleset.example.com",
            bookSourceName = "完整规则书源",
            ruleSearch = SearchRule(
                bookList = "class.search-result@div.item",
                bookName = "h2@text",
                author = "span.author@text",
                bookUrl = "a@href"
            ),
            ruleBookInfo = BookInfoRule(
                name = "h1@text",
                author = "span.author@text",
                intro = "div.intro@html"
            ),
            ruleToc = TocRule(
                chapterList = "ul.toc@li",
                chapterName = "a@text",
                chapterUrl = "a@href"
            ),
            ruleContent = ContentRule(
                content = "div.article@html"
            ),
            ruleExplore = ExploreRule(
                bookList = "div.category@div.book",
                bookName = "h3@text"
            ),
            ruleReview = ReviewRule(
                reviewList = "div.reviews@div.item",
                reviewContent = "p@text"
            )
        )
        
        // 验证所有规则已正确设置
        assertNotNull(bookSource.ruleSearch)
        assertNotNull(bookSource.ruleBookInfo)
        assertNotNull(bookSource.ruleToc)
        assertNotNull(bookSource.ruleContent)
        assertNotNull(bookSource.ruleExplore)
        assertNotNull(bookSource.ruleReview)
        
        // 验证规则内容
        assertEquals("class.search-result@div.item", bookSource.ruleSearch!!.bookList)
        assertEquals("h1@text", bookSource.ruleBookInfo!!.name)
        assertEquals("ul.toc@li", bookSource.ruleToc!!.chapterList)
        assertEquals("div.article@html", bookSource.ruleContent!!.content)
        assertEquals("div.category@div.book", bookSource.ruleExplore!!.bookList)
        assertEquals("div.reviews@div.item", bookSource.ruleReview!!.reviewList)
    }

    /**
     * 测试规则JSON规则格式
     */
    @Test
    fun testJsonRuleFormat() {
        // JSON路径规则测试
        val jsonSearchRule = SearchRule(
            bookList = "$.data.books[*]",
            bookName = "$.name",
            author = "$.author",
            bookUrl = "$.url"
        )
        
        assertTrue(jsonSearchRule.bookList!!.startsWith("$"))
        assertTrue(jsonSearchRule.bookName!!.startsWith("$"))
        assertTrue(jsonSearchRule.author!!.startsWith("$"))
        assertTrue(jsonSearchRule.bookUrl!!.startsWith("$"))
    }

    /**
     * 测试XPath规则格式
     */
    @Test
    fun testXPathRuleFormat() {
        val xpathTocRule = TocRule(
            chapterList = "//div[@class='chapter-list']/li",
            chapterName = "//a/text()",
            chapterUrl = "//a/@href"
        )
        
        assertTrue(xpathTocRule.chapterList!!.startsWith("//"))
        assertTrue(xpathTocRule.chapterName!!.startsWith("//"))
        assertTrue(xpathTocRule.chapterUrl!!.startsWith("//"))
    }

    /**
     * 测试JS规则格式
     */
    @Test
    fun testJsRuleFormat() {
        val jsContentRule = ContentRule(
            content = "{{result.html()}}",
            webJs = "function getMore() { scroll(); }"
        )
        
        assertTrue(jsContentRule.content!!.contains("{{"))
        assertTrue(jsContentRule.content!!.contains("}}"))
        assertTrue(jsContentRule.webJs!!.contains("function"))
    }

    /**
     * 测试正则替换规则
     */
    @Test
    fun testRegexReplaceRule() {
        val contentWithReplace = ContentRule(
            content = "div.text@html",
            replaceRegex = "##广告内容##",
            sourceRegex = "<script>.*?</script>"
        )
        
        assertNotNull(contentWithReplace.replaceRegex)
        assertNotNull(contentWithReplace.sourceRegex)
        assertTrue(contentWithReplace.replaceRegex!!.contains("##"))
    }

    /**
     * 测试规则继承结构
     */
    @Test
    fun testRuleInheritanceStructure() {
        // BaseSource接口测试
        val bookSource = BookSource(
            bookSourceUrl = "https://inherit.example.com",
            bookSourceName = "继承测试书源",
            header = "{\"User-Agent\": \"Test\"}",
            jsLib = "function test() {}"
        )
        
        // 测试BaseSource接口方法
        assertEquals("https://inherit.example.com", bookSource.getKey())
        assertEquals("继承测试书源", bookSource.getTag())
        assertNotNull(bookSource.header)
        assertNotNull(bookSource.jsLib)
    }
}