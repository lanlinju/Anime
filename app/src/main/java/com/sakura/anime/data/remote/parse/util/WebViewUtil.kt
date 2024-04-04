package com.sakura.anime.data.remote.parse.util

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sakura.anime.application.AnimeApplication
import com.sakura.anime.util.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("StaticFieldLeak", "SetJavaScriptEnabled")
class WebViewUtil {
    private val LOG_TAG = "WebViewUtil"
    private var webView: WebView? = null
    private val _blockRes: MutableList<String> = mutableListOf(
        ".css", ".js", ".jpeg", ".svg", ".ico", ".ts",
        ".gif", ".jpg", ".png", ".webp", ".wasm"
    )

    /**
     * 根据正则表达式regex获取视频链接，通过过滤拦截[WebView]所有发送的http url请求
     *
     * @param url 要访问的视频所在网页的url
     * @param regex 通过regex匹配拦截对应视频链接
     * @param predicate 自定义额外的判断条件,当regex不能满足匹配要求时会执行
     * @param blockRes 过滤掉的资源文件
     * @param timeoutMs 请求超时的时间,单位毫秒
     *
     * @return 返回匹配到的视频链接url，匹配不到结果会报一个[SocketTimeoutException]超时异常
     */
    suspend fun interceptRequest(
        url: String,
        regex: String,
        predicate: suspend (requestUrl: String) -> Boolean = { false },
        blockRes: List<String> = emptyList(),
        timeoutMs: Long = 10_000L
    ): String = withContext(Dispatchers.Main) {

        createWebView()

        _blockRes.addAll(blockRes)

        var hasResume = false

        suspendCancellableCoroutine { continuation ->

            continuation.invokeOnCancellation {
                "invokeOnCancellation".log(LOG_TAG)
                destroyWebView()
            }

            webView?.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val requestUrl = request.url.toString()

                    // 过滤不需要匹配的资源请求url
                    if (hasResume || requestUrl.containStrs(_blockRes))
                        return super.shouldInterceptRequest(view, request)

                    requestUrl.log(LOG_TAG, "interceptRequest")
                    launch(Dispatchers.Default) {
                        if (requestUrl.contains(regex.toRegex()) || predicate(requestUrl)) {
                            requestUrl.log(LOG_TAG, "interceptRequest: regex resume")
                            hasResume = true
                            continuation.resume(requestUrl)
                        }
                    }

                    return super.shouldInterceptRequest(view, request)
                }
            }

            webView?.loadUrl(url) // 加载视频播放所在的Web网页

            launch(Dispatchers.Main) {
                var elapsedTime = 0
                while (elapsedTime < timeoutMs && !hasResume && isActive) {
                    delay(200)
                    elapsedTime += 200
                    //elapsedTime.log(LOG_TAG, "elapsed")
                }

                destroyWebView()

                if (!hasResume) {
                    "resumeWithException".log(LOG_TAG)
                    continuation.resumeWithException(SocketTimeoutException("webView connection timeout exception"))
                }
            }
        }
    }


    private fun createWebView() {
        destroyWebView()
        webView = WebView(AnimeApplication.getInstance()).apply {
            settings.javaScriptEnabled = true
        }
    }

    private fun destroyWebView() {
        webView?.destroy()
        webView = null
        "destroyWebView".log(LOG_TAG)
    }

    fun clearWeb() {
        createWebView()
        webView?.clear()
        destroyWebView()
    }

    private fun CharSequence.containStrs(strs: List<String>) =
        strs.find { contains(it) } != null

    private fun WebView.clear() {
        clearCache(true)
        clearHistory()
        clearFormData()
        clearMatches()
    }

}