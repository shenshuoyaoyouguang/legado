package io.legado.app.help.http

import android.annotation.SuppressLint
import android.net.http.X509TrustManagerExtensions
import io.legado.app.utils.printOnDebug
import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * SSL证书验证辅助类
 * 
 * 支持两种模式：
 * 1. 严格模式（sslStrictMode=true）：使用系统默认的证书验证，更安全
 * 2. 兼容模式（sslStrictMode=false，默认）：允许自签名证书和主机名不匹配，适合非HTTPS书源
 */
@Suppress("unused")
object SSLHelper {

    // ==================== 兼容模式实现（不安全但兼容性好）====================
    
    /**
     * 不安全的TrustManager，接受所有证书
     * 警告：此实现会绕过所有SSL证书验证，仅用于兼容非HTTPS书源
     */
    val unsafeTrustManager: X509TrustManager =
        @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                //do nothing，接受任意客户端证书
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                //do nothing，接受任意服务端证书
            }

            fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, host: String): List<X509Certificate> {
                return chain.toList()
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

    val unsafeTrustManagerExtensions by lazy {
        X509TrustManagerExtensions(unsafeTrustManager)
    }

    val unsafeSSLSocketFactory: SSLSocketFactory by lazy {
        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(unsafeTrustManager), SecureRandom())
            sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * 不安全的主机名验证器，接受所有主机名
     */
    val unsafeHostnameVerifier: HostnameVerifier = HostnameVerifier { _, _ -> true }

    // ==================== 安全模式实现 ====================

    /**
     * 安全的TrustManager，使用系统默认证书验证
     * 推荐用于生产环境
     */
    val safeTrustManager: X509TrustManager by lazy {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        trustManagerFactory.trustManagers.filterIsInstance<X509TrustManager>().first()
            ?: throw IllegalStateException("无法获取系统默认TrustManager")
    }

    val safeTrustManagerExtensions by lazy {
        X509TrustManagerExtensions(safeTrustManager)
    }

    val safeSSLSocketFactory: SSLSocketFactory by lazy {
        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(safeTrustManager), SecureRandom())
            sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * 安全的主机名验证器，使用系统默认验证
     */
    val safeHostnameVerifier: HostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()

    // ==================== 配置驱动的SSL参数获取 ====================

    /**
     * 根据配置获取TrustManager
     * @param strictMode 是否使用严格模式，null时从AppConfig读取
     */
    @JvmStatic
    fun getTrustManager(strictMode: Boolean? = null): X509TrustManager {
        val useStrict = strictMode ?: io.legado.app.help.config.AppConfig.sslStrictMode
        return if (useStrict) safeTrustManager else unsafeTrustManager
    }

    /**
     * 根据配置获取TrustManagerExtensions
     * @param strictMode 是否使用严格模式，null时从AppConfig读取
     */
    @JvmStatic
    fun getTrustManagerExtensions(strictMode: Boolean? = null): X509TrustManagerExtensions {
        val useStrict = strictMode ?: io.legado.app.help.config.AppConfig.sslStrictMode
        return if (useStrict) safeTrustManagerExtensions else unsafeTrustManagerExtensions
    }

    /**
     * 根据配置获取SSLSocketFactory
     * @param strictMode 是否使用严格模式，null时从AppConfig读取
     */
    @JvmStatic
    fun getSSLSocketFactory(strictMode: Boolean? = null): SSLSocketFactory {
        val useStrict = strictMode ?: io.legado.app.help.config.AppConfig.sslStrictMode
        return if (useStrict) safeSSLSocketFactory else unsafeSSLSocketFactory
    }

    /**
     * 根据配置获取HostnameVerifier
     * @param strictMode 是否使用严格模式，null时从AppConfig读取
     */
    @JvmStatic
    fun getHostnameVerifier(strictMode: Boolean? = null): HostnameVerifier {
        val useStrict = strictMode ?: io.legado.app.help.config.AppConfig.sslStrictMode
        return if (useStrict) safeHostnameVerifier else unsafeHostnameVerifier
    }

    // ==================== 原有功能保持不变 ====================

    class SSLParams {
        lateinit var sSLSocketFactory: SSLSocketFactory
        lateinit var trustManager: X509TrustManager
    }

    /**
     * https单向认证
     * 可以额外配置信任服务端的证书策略，否则默认是按CA证书去验证的，若不是CA可信任的证书，则无法通过验证
     */
    fun getSslSocketFactory(trustManager: X509TrustManager): SSLParams? {
        return getSslSocketFactoryBase(trustManager, null, null)
    }

    /**
     * https单向认证
     * 用含有服务端公钥的证书校验服务端证书
     */
    fun getSslSocketFactory(vararg certificates: InputStream): SSLParams? {
        return getSslSocketFactoryBase(null, null, null, *certificates)
    }

    /**
     * https双向认证
     * bksFile 和 password -> 客户端使用bks证书校验服务端证书
     * certificates -> 用含有服务端公钥的证书校验服务端证书
     */
    fun getSslSocketFactory(
        bksFile: InputStream,
        password: String,
        vararg certificates: InputStream
    ): SSLParams? {
        return getSslSocketFactoryBase(null, bksFile, password, *certificates)
    }

    /**
     * https双向认证
     * bksFile 和 password -> 客户端使用bks证书校验服务端证书
     * X509TrustManager -> 如果需要自己校验，那么可以自己实现相关校验，如果不需要自己校验，那么传null即可
     */
    fun getSslSocketFactory(
        bksFile: InputStream,
        password: String,
        trustManager: X509TrustManager
    ): SSLParams? {
        return getSslSocketFactoryBase(trustManager, bksFile, password)
    }

    private fun getSslSocketFactoryBase(
        trustManager: X509TrustManager?,
        bksFile: InputStream?,
        password: String?,
        vararg certificates: InputStream
    ): SSLParams? {
        val sslParams = SSLParams()
        try {
            val keyManagers = prepareKeyManager(bksFile, password)
            val trustManagers = prepareTrustManager(*certificates)
            val manager: X509TrustManager = trustManager ?: chooseTrustManager(trustManagers)
            // 创建TLS类型的SSLContext对象， that uses our TrustManager
            val sslContext = SSLContext.getInstance("TLS")
            // 用上面得到的trustManagers初始化SSLContext，这样sslContext就会信任keyStore中的证书
            // 第一个参数是授权的密钥管理器，用来授权验证，比如授权自签名的证书验证。第二个是被授权的证书管理器，用来验证服务器端的证书
            sslContext.init(keyManagers, arrayOf<TrustManager>(manager), null)
            // 通过sslContext获取SSLSocketFactory对象
            sslParams.sSLSocketFactory = sslContext.socketFactory
            sslParams.trustManager = manager
            return sslParams
        } catch (e: NoSuchAlgorithmException) {
            e.printOnDebug()
        } catch (e: KeyManagementException) {
            e.printOnDebug()
        }
        return null
    }

    private fun prepareKeyManager(bksFile: InputStream?, password: String?): Array<KeyManager>? {
        try {
            if (bksFile == null || password == null) return null
            val clientKeyStore = KeyStore.getInstance("BKS")
            clientKeyStore.load(bksFile, password.toCharArray())
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(clientKeyStore, password.toCharArray())
            return kmf.keyManagers
        } catch (e: Exception) {
            e.printOnDebug()
        }
        return null
    }

    private fun prepareTrustManager(vararg certificates: InputStream): Array<TrustManager> {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        // 创建一个默认类型的KeyStore，存储我们信任的证书
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null)
        for ((index, certStream) in certificates.withIndex()) {
            val certificateAlias = index.toString()
            // 证书工厂根据证书文件的流生成证书 cert
            val cert = certificateFactory.generateCertificate(certStream)
            // 将 cert 作为可信证书放入到keyStore中
            keyStore.setCertificateEntry(certificateAlias, cert)
            try {
                certStream.close()
            } catch (e: IOException) {
                e.printOnDebug()
            }
        }
        //我们创建一个默认类型的TrustManagerFactory
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        //用我们之前的keyStore实例初始化TrustManagerFactory，这样tmf就会信任keyStore中的证书
        tmf.init(keyStore)
        //通过tmf获取TrustManager数组，TrustManager也会信任keyStore中的证书
        return tmf.trustManagers
    }

    private fun chooseTrustManager(trustManagers: Array<TrustManager>): X509TrustManager {
        for (trustManager in trustManagers) {
            if (trustManager is X509TrustManager) {
                return trustManager
            }
        }
        throw NullPointerException()
    }
}