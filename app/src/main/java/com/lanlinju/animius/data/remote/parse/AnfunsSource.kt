package com.lanlinju.animius.data.remote.parse

import android.annotation.SuppressLint
import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.AnimeDetailBean
import com.lanlinju.animius.data.remote.dto.EpisodeBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import com.lanlinju.animius.data.remote.dto.VideoBean
import com.lanlinju.animius.data.remote.parse.util.WebViewUtil
import com.lanlinju.animius.util.DownloadManager
import com.lanlinju.animius.util.getDefaultDomain
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.URLDecoder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@SuppressLint("StaticFieldLeak", "SetJavaScriptEnabled")
object AnfunsSource : AnimeSource {

    private const val LOG_TAG = "AnfunsSource"

    override val DEFAULT_DOMAIN: String = "https://www.anfuns.org"
    override var baseUrl = getDefaultDomain()
    private val webViewUtil: WebViewUtil by lazy { WebViewUtil() }

    override fun onExit() {
        webViewUtil.clearWeb()
    }

    override suspend fun getHomeData(): List<HomeBean> {
        val source = DownloadManager.getHtml(baseUrl)

        val document = Jsoup.parse(source)
        val elements = document.select("div#conch-content").select("div.container")
        val homeBeanList = mutableListOf<HomeBean>()
        for ((i, el) in elements.withIndex()) {
            val head = el.select("div.hl-rb-head")
            if (head.isEmpty() || i == 3 || i == 8 || i == 9) continue

            val title = head.select("h2")[0].text()
            val moreUrl = head.select("a").attr("href")
            val homeItemBeanList = getAnimeList(el.select("div.hl-list-wrap > ul > li"))
            homeBeanList.add(HomeBean(title = title, moreUrl = moreUrl, animes = homeItemBeanList))
        }

        return homeBeanList
    }

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        val source = DownloadManager.getHtml("${baseUrl}/$detailUrl")
        val document = Jsoup.parse(source)
        val main = document.select("div.hl-dc-content")
        val title = main.select("div.hl-dc-headwrap > h2").text()
        val descList = main.select("div.hl-full-box").select("div.hl-full-box > ul > li")
        val desc = descList[11].text()
        val imgUrl = document.select("div.hl-dc-pic > span").attr("data-original")
        val tags = descList[6].select("a").map { it.text() }
        val episodes =
            getAnimeEpisodes(document.select("div.hl-tabs-box"))
        val relatedAnimes =
            getAnimeList(document.select("div.hl-change-box1").select("li"))
        return AnimeDetailBean(title, imgUrl, desc, tags, episodes, relatedAnimes)
    }

    override suspend fun getSearchData(query: String, page: Int): List<AnimeBean> {
        val source = DownloadManager.getHtml("${baseUrl}/search.html?wd=${query}&page=${page}")
        val document = Jsoup.parse(source)
        val animeList = mutableListOf<AnimeBean>()
        document.select("div.hl-rb-search").select("li").forEach { el ->
            val title = el.select("a").attr("title")
            val url = el.select("a").attr("href")
            val imgUrl = el.select("a").attr("data-original")
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url))
        }
        return animeList
    }

    override suspend fun getWeekData(): Map<Int, List<AnimeBean>> {
        val source = DownloadManager.getHtml(baseUrl)
        val document = Jsoup.parse(source)
        val elements = document.select("div#conch-content").select("div.container")[3]
        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        elements.select("div.hl-list-wrap").forEachIndexed { index, element ->
            val dayList = getAnimeList(element.select("li"))
            weekMap[index] = dayList
        }
        return weekMap
    }

    private suspend fun getAnimeEpisodes(elements: Elements): List<EpisodeBean> {
        if (elements.isEmpty()) return emptyList()

        val dramaElements = elements[0].select("div.hl-list-wrap").select("li > a") //剧集列表
        val episodes = mutableListOf<EpisodeBean>()
        dramaElements.forEach { el ->
            val name = el.text()
            val url = el.attr("href")
            episodes.add(EpisodeBean(name, url))
        }

        return episodes
    }

    private suspend fun getAnimeList(elements: Elements): List<AnimeBean> {
        val animeList = mutableListOf<AnimeBean>()
        elements.forEach { el ->
            el.select("a").apply {
                val title = attr("title")
                val url = attr("href")
                val imgUrl = attr("data-original")
                val episodeName = el.select("div.hl-pic-text").text()
                animeList.add(AnimeBean(title, imgUrl, url, episodeName))
            }
        }
        return animeList
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        val url = "${baseUrl}/$episodeUrl"
        val source = DownloadManager.getHtml(url)
        val document = Jsoup.parse(source)

        /*val spanElement = document.select("div.hl-row-box").select("h2 > span")[0]
        val title = spanElement.ownText()
        val episodeName = spanElement.select("em").text()
        val episodes =
            getAnimeEpisodes(document.select("div.hl-tabs-box"))*/
        val videoUrl = getVideoUrl(document).let { getVideoUrl(url = url) }
        return VideoBean(videoUrl)
    }

    // Reference code: https://github.com/670848654/MoviesBox/blob/279a68da4645cde4ba3ba9257a4baac5ced831c5/app/src/main/java/my/project/moviesbox/parser/parserImpl/AnFunsImpl.java#L728
    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun getVideoUrl(document: Document): String? {
        // 获取<script>标签里的内容使用方法data()获取
        val data = document.select("div.hl-player-wrap > script")[1].data()
        val jsonText = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1)
        val jsonObject = JSONObject(jsonText)
        val encryptUrl = jsonObject.getString("url")
        return when (jsonObject.getInt("encrypt")) {
            2 -> {
                val decodedString = Base64.decode(encryptUrl) // Base64解码
                    .decodeToString()
                    .let { URLDecoder.decode(it, "UTF-8") } // URLDecode解码
                decodedString
            }

            3 -> {
                val apiTemplate =
                    "${baseUrl}/vapi/AIRA/art.php?url=%s&next=%s&vid=%s&title=%s&nid=%s&uid=guest&name=guest&group=guest"
                val vid = jsonObject.getString("id")
                val title = jsonObject.getJSONObject("vod_data").getString("vod_name")
                val nid = jsonObject.getInt("nid")
                val parseApi = apiTemplate.format(
                    encryptUrl,
                    baseUrl.replace("https:", ""),
                    vid,
                    title,
                    nid
                )
                val apiResult = DownloadManager.getHtml(parseApi, mapOf("Referer" to baseUrl))
                val regex = "url:\\s*'([^']+)'".toRegex()
                val videoUrl = regex.find(apiResult)?.groupValues?.get(1)
                videoUrl
            }

            else -> {
                encryptUrl
            }
        }
    }

    private suspend fun getVideoUrl(url: String): String {
//        val regex = "https://www.anfuns.cc/vapi/AIRA/mui.php.*"
//        val videoUrlRegex = "url=(.*?)&".toRegex()
        val videoUrlTarget = webViewUtil.interceptRequest(
            url = url,
            regex = ".m3u8|.mp4",
        )
        return videoUrlTarget
//        return videoUrlRegex.find(videoUrlTarget)?.groupValues?.get(1)
//            ?: throw IllegalStateException("video url is empty")
    }

}