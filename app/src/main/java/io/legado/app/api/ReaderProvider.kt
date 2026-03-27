/*
 * Copyright (C) 2020 w568w
 */
package io.legado.app.api

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.google.gson.Gson
import io.legado.app.api.controller.BookController
import io.legado.app.api.controller.BookSourceController
import io.legado.app.api.controller.RssSourceController
import io.legado.app.help.config.AppConfig
import kotlinx.coroutines.runBlocking

/**
 * Export book data to other app.
 * 
 * 安全说明：
 * 1. Provider已通过signature级别权限保护，仅同签名应用可访问
 * 2. 可选的Token认证：当AppConfig.apiAuthEnabled=true时，需要提供有效的apiAuthToken
 * 
 * 使用方式：
 * - 调用方需要在请求参数中添加token参数，值为AppConfig.apiAuthToken
 * - 示例：content://authority/bookSource/query?token=YOUR_TOKEN
 */
class ReaderProvider : ContentProvider() {
    private enum class RequestCode {
        SaveBookSource, SaveBookSources, DeleteBookSources, GetBookSource, GetBookSources,
        SaveRssSource, SaveRssSources, DeleteRssSources, GetRssSource, GetRssSources,
        SaveBook, GetBookshelf, RefreshToc, GetChapterList, GetBookContent, GetBookCover,
        SaveBookProgress
    }

    companion object {
        private const val TOKEN_PARAM = "token"
        private const val AUTH_TOKEN_HEADER = "auth_token"
    }

    private val postBodyKey = "json"
    private val sMatcher by lazy {
        UriMatcher(UriMatcher.NO_MATCH).apply {
            "${context?.applicationInfo?.packageName}.readerProvider".also { authority ->
                addURI(authority, "bookSource/insert", RequestCode.SaveBookSource.ordinal)
                addURI(authority, "bookSources/insert", RequestCode.SaveBookSources.ordinal)
                addURI(authority, "bookSources/delete", RequestCode.DeleteBookSources.ordinal)
                addURI(authority, "bookSource/query", RequestCode.GetBookSource.ordinal)
                addURI(authority, "bookSources/query", RequestCode.GetBookSources.ordinal)
                addURI(authority, "rssSource/insert", RequestCode.SaveBookSource.ordinal)
                addURI(authority, "rssSources/insert", RequestCode.SaveBookSources.ordinal)
                addURI(authority, "rssSources/delete", RequestCode.DeleteBookSources.ordinal)
                addURI(authority, "rssSource/query", RequestCode.GetBookSource.ordinal)
                addURI(authority, "rssSources/query", RequestCode.GetBookSources.ordinal)
                addURI(authority, "book/insert", RequestCode.SaveBook.ordinal)
                addURI(authority, "books/query", RequestCode.GetBookshelf.ordinal)
                addURI(authority, "book/refreshToc/query", RequestCode.RefreshToc.ordinal)
                addURI(authority, "book/chapter/query", RequestCode.GetChapterList.ordinal)
                addURI(authority, "book/content/query", RequestCode.GetBookContent.ordinal)
                addURI(authority, "book/cover/query", RequestCode.GetBookCover.ordinal)
            }
        }
    }

    override fun onCreate(): Boolean {
        context?.let { context ->
            ShortCuts.buildShortCuts(context)
        }
        return false
    }

    /**
     * 验证访问权限
     * 当启用API认证时，检查请求中的token是否有效
     * @param uri 请求URI，可能包含token参数
     * @param values ContentValues，可能包含auth_token
     * @return true表示验证通过，false表示验证失败
     */
    private fun validateAuth(uri: Uri?, values: ContentValues? = null): Boolean {
        // 未启用认证时，允许所有访问
        if (!AppConfig.apiAuthEnabled) {
            return true
        }

        val expectedToken = AppConfig.apiAuthToken
        if (expectedToken.isNullOrBlank()) {
            // 启用了认证但未设置token，允许访问（兼容性考虑）
            return true
        }

        // 从URI参数中获取token
        val uriToken = uri?.getQueryParameter(TOKEN_PARAM)
        if (uriToken == expectedToken) {
            return true
        }

        // 从ContentValues中获取token
        val valuesToken = values?.getAsString(AUTH_TOKEN_HEADER)
        if (valuesToken == expectedToken) {
            return true
        }

        return false
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (sMatcher.match(uri) < 0) return -1
        
        // 验证权限
        if (!validateAuth(uri)) {
            return -1
        }
        
        when (RequestCode.entries[sMatcher.match(uri)]) {
            RequestCode.DeleteBookSources -> BookSourceController.deleteSources(selection)
            RequestCode.DeleteRssSources -> BookSourceController.deleteSources(selection)
            else -> throw IllegalStateException(
                "Unexpected value: " + RequestCode.entries[sMatcher.match(uri)].name
            )
        }
        return 0
    }

    override fun getType(uri: Uri) = throw UnsupportedOperationException("Not yet implemented")

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (sMatcher.match(uri) < 0) return null
        
        // 验证权限
        if (!validateAuth(uri, values)) {
            return null
        }
        
        runBlocking {
            when (RequestCode.entries[sMatcher.match(uri)]) {
                RequestCode.SaveBookSource -> values?.let {
                    BookSourceController.saveSource(values.getAsString(postBodyKey))
                }

                RequestCode.SaveBookSources -> values?.let {
                    BookSourceController.saveSources(values.getAsString(postBodyKey))
                }

                RequestCode.SaveRssSource -> values?.let {
                    RssSourceController.saveSource(values.getAsString(postBodyKey))
                }

                RequestCode.SaveRssSources -> values?.let {
                    RssSourceController.saveSources(values.getAsString(postBodyKey))
                }

                RequestCode.SaveBook -> values?.let {
                    BookController.saveBook(values.getAsString(postBodyKey))
                }

                RequestCode.SaveBookProgress -> values?.let {
                    BookController.saveBookProgress(values.getAsString(postBodyKey))
                }

                else -> throw IllegalStateException(
                    "Unexpected value: " + RequestCode.entries[sMatcher.match(uri)].name
                )
            }
        }
        return null
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        if (sMatcher.match(uri) < 0) return null
        
        // 验证权限
        if (!validateAuth(uri)) {
            return null
        }
        
        val map: MutableMap<String, ArrayList<String>> = HashMap()
        uri.getQueryParameter("url")?.let {
            map["url"] = arrayListOf(it)
        }
        uri.getQueryParameter("index")?.let {
            map["index"] = arrayListOf(it)
        }
        uri.getQueryParameter("path")?.let {
            map["path"] = arrayListOf(it)
        }
        return when (RequestCode.entries[sMatcher.match(uri)]) {
            RequestCode.GetBookSource -> SimpleCursor(BookSourceController.getSource(map))
            RequestCode.GetBookSources -> SimpleCursor(BookSourceController.sources)
            RequestCode.GetRssSource -> SimpleCursor(RssSourceController.getSource(map))
            RequestCode.GetRssSources -> SimpleCursor(RssSourceController.sources)
            RequestCode.GetBookshelf -> SimpleCursor(BookController.bookshelf)
            RequestCode.GetBookContent -> SimpleCursor(BookController.getBookContent(map))
            RequestCode.RefreshToc -> SimpleCursor(BookController.refreshToc(map))
            RequestCode.GetChapterList -> SimpleCursor(BookController.getChapterList(map))
            RequestCode.GetBookCover -> SimpleCursor(BookController.getCover(map))
            else -> throw IllegalStateException(
                "Unexpected value: " + RequestCode.entries[sMatcher.match(uri)].name
            )
        }
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ) = throw UnsupportedOperationException("Not yet implemented")


    /**
     * Simple inner class to deliver json callback data.
     *
     * Only getString() makes sense.
     */
    private class SimpleCursor(data: ReturnData?) : MatrixCursor(arrayOf("result"), 1) {

        private val mData: String = Gson().toJson(data)

        init {
            addRow(arrayOf(mData))
        }

    }
}