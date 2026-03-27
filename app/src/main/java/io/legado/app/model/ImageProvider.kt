package io.legado.app.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import androidx.collection.LruCache
import io.legado.app.R
import io.legado.app.constant.AppLog.putDebug
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookSource
import io.legado.app.exception.NoStackTraceException
import io.legado.app.help.book.BookHelp
import io.legado.app.help.book.isEpub
import io.legado.app.help.book.isMobi
import io.legado.app.help.book.isPdf
import io.legado.app.help.config.AppConfig
import io.legado.app.model.localBook.EpubFile
import io.legado.app.model.localBook.MobiFile
import io.legado.app.model.localBook.PdfFile
import io.legado.app.utils.BitmapUtils
import io.legado.app.utils.FileUtils
import io.legado.app.utils.SvgUtils
import io.legado.app.utils.toastOnUi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import splitties.init.appCtx
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object ImageProvider {

    private val errorBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(appCtx.resources, R.drawable.image_loading_error)
    }

    /**
     * 缓存bitmap LruCache实现
     * filePath bitmap
     */
    private const val M = 1024 * 1024
    
    /**
     * 默认缓存大小：50MB
     */
    private const val DEFAULT_CACHE_SIZE_MB = 50
    
    /**
     * 最大缓存大小上限：128MB（原256MB过大，可能导致OOM）
     */
    private const val MAX_CACHE_SIZE_MB = 128
    
    val cacheSize: Int
        get() {
            val configuredSize = AppConfig.bitmapCacheSize
            // 限制缓存大小在1-128MB范围内
            val validSize = configuredSize.coerceIn(1, MAX_CACHE_SIZE_MB)
            if (configuredSize != validSize) {
                AppConfig.bitmapCacheSize = validSize
            }
            return validSize * M
        }

    val bitmapLruCache = BitmapLruCache()

    class BitmapLruCache : LruCache<String, Bitmap>(cacheSize) {

        private var removeCount = 0

        val count get() = putCount() + createCount() - evictionCount() - removeCount

        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            if (!evicted) {
                synchronized(this) {
                    removeCount++
                }
            }
            //错误图片不能释放,占位用,防止一直重复获取图片
            if (oldValue != errorBitmap) {
                oldValue.recycle()
                //putDebug("ImageProvider: trigger bitmap recycle. URI: $filePath")
                //putDebug("ImageProvider : cacheUsage ${size()}bytes / ${maxSize()}bytes")
            }
        }

    }

    fun put(key: String, bitmap: Bitmap) {
        ensureLruCacheSize(bitmap)
        bitmapLruCache.put(key, bitmap)
    }

    fun get(key: String): Bitmap? {
        return bitmapLruCache[key]
    }

    fun remove(key: String): Bitmap? {
        return bitmapLruCache.remove(key)
    }

    private fun getNotRecycled(key: String): Bitmap? {
        val bitmap = bitmapLruCache[key] ?: return null
        if (bitmap.isRecycled) {
            bitmapLruCache.remove(key)
            return null
        }
        return bitmap
    }

    /**
     * 确保缓存大小合理，防止内存溢出
     * 优化：限制最大扩容到128MB（原256MB过大）
     */
    private fun ensureLruCacheSize(bitmap: Bitmap) {
        val lruMaxSize = bitmapLruCache.maxSize()
        val lruSize = bitmapLruCache.size()
        val byteCount = bitmap.byteCount
        
        // 计算新的缓存大小，限制最大为MAX_CACHE_SIZE_MB
        val maxSizeBytes = MAX_CACHE_SIZE_MB * M
        val size = if (byteCount > lruMaxSize) {
            min(maxSizeBytes, (byteCount * 1.3).toInt())
        } else if (lruSize + byteCount > lruMaxSize && bitmapLruCache.count < 5) {
            min(maxSizeBytes, (lruSize + byteCount * 1.3).toInt())
        } else {
            lruMaxSize
        }
        if (size > lruMaxSize) {
            bitmapLruCache.resize(size)
        }
    }
    
    /**
     * 响应系统内存压力，释放部分缓存
     * 在App的onTrimMemory回调中调用此方法
     * @param level trim级别，参见ComponentCallbacks2
     */
    fun onTrimMemory(level: Int) {
        when (level) {
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // 内存紧张或UI隐藏时，释放一半缓存
                val halfSize = bitmapLruCache.size() / 2
                if (halfSize > 0) {
                    bitmapLruCache.trimToSize(halfSize)
                }
            }
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                // 内存严重不足时，释放更多缓存
                val quarterSize = bitmapLruCache.size() / 4
                if (quarterSize > 0) {
                    bitmapLruCache.trimToSize(quarterSize)
                }
            }
            android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // 系统即将杀掉进程，清空所有缓存
                bitmapLruCache.evictAll()
            }
        }
    }

    /**
     *缓存网络图片和epub图片
     */
    suspend fun cacheImage(
        book: Book,
        src: String,
        bookSource: BookSource?
    ): File {
        return withContext(IO) {
            val vFile = BookHelp.getImage(book, src)
            if (!BookHelp.isImageExist(book, src)) {
                val inputStream = when {
                    book.isEpub -> EpubFile.getImage(book, src)
                    book.isPdf -> PdfFile.getImage(book, src)
                    book.isMobi -> MobiFile.getImage(book, src)
                    else -> {
                        BookHelp.saveImage(bookSource, book, src)
                        null
                    }
                }
                inputStream?.use { input ->
                    val newFile = FileUtils.createFileIfNotExist(vFile.absolutePath)
                    FileOutputStream(newFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            return@withContext vFile
        }
    }

    /**
     *获取图片宽度高度信息
     */
    suspend fun getImageSize(
        book: Book,
        src: String,
        bookSource: BookSource?
    ): Size {
        val file = cacheImage(book, src, bookSource)
        val op = BitmapFactory.Options()
        // inJustDecodeBounds如果设置为true,仅仅返回图片实际的宽和高,宽和高是赋值给opts.outWidth,opts.outHeight;
        op.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, op)
        if (op.outWidth < 1 && op.outHeight < 1) {
            //svg size
            val size = SvgUtils.getSize(file.absolutePath)
            if (size != null) return size
            putDebug("ImageProvider: $src Unsupported image type")
            //file.delete() 重复下载
            return Size(errorBitmap.width, errorBitmap.height)
        }
        return Size(op.outWidth, op.outHeight)
    }

    /**
     *获取bitmap 使用LruCache缓存
     */
    fun getImage(
        book: Book,
        src: String,
        width: Int,
        height: Int? = null
    ): Bitmap {
        //src为空白时 可能被净化替换掉了 或者规则失效
        if (book.getUseReplaceRule() && src.isBlank()) {
            book.setUseReplaceRule(false)
            appCtx.toastOnUi(R.string.error_image_url_empty)
        }
        val vFile = BookHelp.getImage(book, src)
        if (!vFile.exists()) return errorBitmap
        //epub文件提供图片链接是相对链接，同时阅读多个epub文件，缓存命中错误
        //bitmapLruCache的key同一改成缓存文件的路径
        val cacheBitmap = getNotRecycled(vFile.absolutePath)
        if (cacheBitmap != null) return cacheBitmap
        return kotlin.runCatching {
            val bitmap = BitmapUtils.decodeBitmap(vFile.absolutePath, width, height)
                ?: SvgUtils.createBitmap(vFile.absolutePath, width, height)
                ?: throw NoStackTraceException(appCtx.getString(R.string.error_decode_bitmap))
            put(vFile.absolutePath, bitmap)
            bitmap
        }.onFailure {
            //错误图片占位,防止重复获取
            put(vFile.absolutePath, errorBitmap)
        }.getOrDefault(errorBitmap)
    }

    fun clear() {
        bitmapLruCache.evictAll()
    }

}
