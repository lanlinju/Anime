package com.sakura.anime.data.remote.parse

import android.annotation.SuppressLint
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.EpisodeBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean
import com.sakura.anime.data.remote.parse.util.WebViewUtil
import com.sakura.anime.util.DownloadManager
import com.sakura.anime.util.getDefaultDomain
import org.jsoup.Jsoup
import org.jsoup.select.Elements

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
        val updateTime = descList[10].text()
        val tags = descList[6].select("a").map { it.text() }
        val episodes =
            getAnimeEpisodes(document.select("div.hl-tabs-box"))
        val relatedAnimes =
            getAnimeList(document.select("div.hl-change-box1").select("li"))
        return AnimeDetailBean(title, imgUrl, desc, "", tags, updateTime, episodes, relatedAnimes)
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        val url = "${baseUrl}/$episodeUrl"
        val source = DownloadManager.getHtml(url)
        val document = Jsoup.parse(source)

        val spanElement = document.select("div.hl-row-box").select("h2 > span")[0]
        val title = spanElement.ownText()
        val episodeName = spanElement.select("em").text()
        val episodes =
            getAnimeEpisodes(document.select("div.hl-tabs-box"))
        val videoUrl = getVideoUrl(url)
        return VideoBean(title, videoUrl, episodeName, episodes)
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

    // Reference code: https://github.com/670848654/MoviesBox/blob/32276dd39cfe531ae21687737df1a564b639d57c/app/src/main/java/my/project/moviesbox/parser/parserImpl/AnFunsImpl.java#L662
    /**
     * 1. 先Base64解码
     * 2. 再URL解码
     */
    /*@OptIn(ExperimentalEncodingApi::class)
    private suspend fun getVideoUrl(document: Document): String {
        // 获取<script>标签里的内容使用方法data()获取
        val videoUrlTarget = document.select("div.hl-player-wrap > script")[1].data()
        val videoUrlRegex = """"url":"(.*?)","url_next"""".toRegex()
        val rawVideoUrl = videoUrlRegex.find(videoUrlTarget)?.groupValues?.get(1)
            ?: throw IllegalStateException("video url is empty")

        val encodedVideoUrl = String(Base64.decode(rawVideoUrl), Charsets.UTF_8)
        return Uri.decode(encodedVideoUrl)
    }*/

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