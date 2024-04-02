package com.sakura.anime.data.remote.parse

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sakura.anime.application.AnimeApplication
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.EpisodeBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean
import com.sakura.anime.util.DownloadManager
import com.sakura.anime.util.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import retrofit2.Response
import java.io.Closeable
import java.net.SocketTimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("StaticFieldLeak", "SetJavaScriptEnabled")
object AgedmSource : AnimeSource {

    private const val LOG_TAG = "AgedmSource"

    const val BASE_URL = "https://www.agedm.org"
    const val REPLACE_DOMAIN_URL = "http://www.agedm.org"

    private var webView: WebView? = null
    private val blockRes: Array<String> = arrayOf(
        ".css", ".js", ".jpeg", ".svg", ".php", ".ico", "age",
        ".gif", ".jpg", ".png", ".webp", ".wasm", ".ts"
    )

    override fun onExit() {
        createWebView()
        webView?.clear()
        destroyWebView()
    }

    override suspend fun getWeekData(): MutableMap<Int, List<AnimeBean>> {
        val source = DownloadManager.getHtml(BASE_URL)
        val document = Jsoup.parse(source)
        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        document.select("div.text_list_box").select("div.tab-pane")
            .forEachIndexed { index, element ->
                val dayList = mutableListOf<AnimeBean>()
                element.select("li").forEach { el ->
                    val title = el.select("a").text()
                    val episodeName = el.select("div.title_sub").text()
                    val url = el.select("a").attr("href").trimDomain()
                    dayList.add(AnimeBean(title = title, img = "", url = url, episodeName))
                }
                weekMap[index] = dayList
            }
        return weekMap
    }

    override suspend fun getSearchData(query: String, page: Int): List<AnimeBean> {
        val source = DownloadManager.getHtml("$BASE_URL/search?query=$query&page=$page")
        val document = Jsoup.parse(source)
        val animeList = mutableListOf<AnimeBean>()
        document.select("div.card").forEach { el ->
            val title = el.select("h5").text()
            val url = el.select("h5 > a").attr("href").trimDomain()
            val imgUrl = el.select("img").attr("data-original")
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url))
        }
        return animeList
    }

    override suspend fun getHomeData(): List<HomeBean> {
        val source = DownloadManager.getHtml(BASE_URL)
        val document = Jsoup.parse(source)

        val homeBeanList = mutableListOf<HomeBean>()
        document.select("div.container").select("div.video_list_box").forEach { element ->
            val title = element.select("h6").text().replace("更多 »", "")
            val moreUrl = element.select("a").attr("href").trimDomain()
            val homeItemBeanList = getAnimeList(element.select("div.video_item"))
            homeBeanList.add(HomeBean(title = title, moreUrl = moreUrl, animes = homeItemBeanList))
        }

        return homeBeanList
    }

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        val source = DownloadManager.getHtml("$BASE_URL/$detailUrl")
        val document = Jsoup.parse(source)
        val score = ""
        val videoDetailRight = document.select("div.video_detail_right")
        val title = videoDetailRight.select("h2").text()
        val desc = videoDetailRight.select("div.video_detail_desc").text()
        val imgUrl = document.select("div.video_detail_cover > img").attr("data-original")
        val detailBoxList = document.select("div.video_detail_box").select("li")
        val updateTime = detailBoxList[6].text()
        val tags = detailBoxList[9].text().split("：")[1].split(" ").toMutableList()
        tags.add(detailBoxList[0].text().split("：")[1])
        tags.add(detailBoxList[1].text().split("：")[1])
        val playlist = document.select("div.tab-content").select("div.tab-pane")
        val episodes = getAnimeEpisodes(playlist)
        val relatedAnimes =
            getAnimeList(document.select("div.video_list_box").select("div.video_item"))
        val animeDetailBean =
            AnimeDetailBean(title, imgUrl, desc, score, tags, updateTime, episodes, relatedAnimes)

        return animeDetailBean
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        val source = DownloadManager.getHtml("$BASE_URL/$episodeUrl")
        val document = Jsoup.parse(source)

        val elements = document.select("div.cata_video_item")
        val title = elements.select("h5").text()
        var episodeName = ""
        val playlist = document.select("div.playlist-source-tab").select("div.tab-pane")
        val episodes = getAnimeEpisodes(playlist, action = { episodeName = it })
        val videoUrl = getVideoUrl(document)

        return VideoBean(title, videoUrl, episodeName, episodes)
    }

    private suspend fun getAnimeList(elements: Elements): List<AnimeBean> {
        val animeList = mutableListOf<AnimeBean>()
        elements.forEach { el ->
            val title = el.select("a").text()
            val url = el.select("a").attr("href").trimDomain()
            val imgUrl = el.select("img").attr("data-original")
            val episodeName = el.select("span.video_item--info").text()
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url, episodeName))
        }
        return animeList
    }

    private suspend fun getAnimeEpisodes(
        elements: Elements,
        action: (String) -> Unit = {}
    ): List<EpisodeBean> {

        if (elements.isEmpty()) return emptyList()

        val episodes = mutableListOf<EpisodeBean>()
        elements[0].select("li").forEach { el ->
            val name = el.text()
            val url = el.select("a").attr("href").trimDomain()
            if (el.select("div.video_detail_spisode_playing").isNotEmpty()) {
                action(name)
            }
            episodes.add(EpisodeBean(name, url))
        }

        return episodes
    }

    private suspend fun getVideoUrl(document: Document) = withContext(Dispatchers.Main) {
        createWebView()

        val videoUrl = document.select("#iframeForVideo").attr("src")

        var hasResume = false

        suspendCancellableCoroutine { continuation ->

            continuation.invokeOnCancellation {
                destroyWebView()
            }

            webView?.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView, request: WebResourceRequest
                ): WebResourceResponse? {
                    val url = request.url.toString()

                    if (url.containStrs(*blockRes) || hasResume)
                        return super.shouldInterceptRequest(view, request)

                    launch(Dispatchers.Default) {
                        if (url.contains(".mp4|.m3u8|video|playurl|hsl|obj|bili".toRegex())) {
                            continuation.resume(url)
                            hasResume = true
                            url.log(LOG_TAG, "getVideoUrl: regex resume")
                        } else {
                            var response: Response<ResponseBody>? = null
                            try {
                                response = DownloadManager.request(url)
                                if (response.isSuccessful && response.isVideoType()) {
                                    continuation.resume(url)
                                    hasResume = true
                                    url.log(LOG_TAG, "getVideoUrl: response resume")
                                }
                            } catch (_: Exception) {
                            } finally {
                                response?.closeQuietly()
                            }
                        }
                        url.log(LOG_TAG, "getVideoUrl")
                    }

                    return super.shouldInterceptRequest(view, request)
                }
            }

            webView?.loadUrl(videoUrl)

            launch(Dispatchers.Main) {
                var elapsedTime = 0
                while (elapsedTime < 10_000 && !hasResume && isActive) {
                    delay(200)
                    elapsedTime += 200
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

    private fun WebView.clear() {
        clearCache(true)
        clearHistory()
        clearFormData()
        clearMatches()
    }

    private fun String.trimDomain() = replace(REPLACE_DOMAIN_URL, "")

    private fun CharSequence.containStrs(vararg strs: CharSequence) =
        strs.find { contains(it) } != null

    private fun Response<*>.header(key: String): String {
        val header = headers()[key]
        return header ?: ""
    }

    private fun Response<*>.isVideoType(): Boolean {
        return header("Content-Type") == "video/mp4"
    }

    private fun Closeable.closeQuietly() {
        try {
            close()
        } catch (rethrown: RuntimeException) {
            throw rethrown
        } catch (_: Exception) {
        }
    }

    private fun Response<ResponseBody>.closeQuietly() {
        body()?.closeQuietly()
        errorBody()?.closeQuietly()
    }
}