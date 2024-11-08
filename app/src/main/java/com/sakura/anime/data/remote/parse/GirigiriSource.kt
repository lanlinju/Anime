package com.sakura.anime.data.remote.parse

import android.net.Uri
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.EpisodeBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean
import com.sakura.anime.data.remote.parse.util.WebViewUtil
import com.sakura.anime.util.DownloadManager
import com.sakura.anime.util.getDefaultDomain
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object GirigiriSource : AnimeSource {

    private const val LOG_TAG = "GirigiriSource"

    override val DEFAULT_DOMAIN: String = "http://anime.girigirilove.com"
    override var baseUrl: String = getDefaultDomain()
    private val webViewUtil: WebViewUtil by lazy { WebViewUtil() }

    override fun onExit() {
        webViewUtil.clearWeb()
    }

    override suspend fun getSearchData(query: String, page: Int): List<AnimeBean> {
        val source = DownloadManager.getHtml("${baseUrl}/search/${query}----------${page}---/")
        val document = Jsoup.parse(source)
        val animeList = mutableListOf<AnimeBean>()
        document.select("div.public-list-box").forEach { el ->
            val title = el.select("div.thumb-txt").text()
            val url = el.select("a").attr("href")
            val imgUrl = el.select("img").attr("data-src").padDomain()
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url))
        }
        return animeList
    }

    override suspend fun getWeekData(): MutableMap<Int, List<AnimeBean>> {
        val source = DownloadManager.getHtml(baseUrl)
        val document = Jsoup.parse(source)
        val elements = document.select("div.wow")[0].select("div#week-module-box")
        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        elements.select("div.public-r").forEachIndexed { index, element ->
            val dayList = getAnimeList(element.select("div.public-list-box"))
            weekMap[index] = dayList
        }
        return weekMap
    }

    override suspend fun getHomeData(): List<HomeBean> {
        val source = DownloadManager.getHtml(baseUrl)
        val document = Jsoup.parse(source)
        val elements = document.select("div.wow").apply { removeAt(0) }
        val homeBeanList = mutableListOf<HomeBean>()
        for ((i, el) in elements.withIndex()) {
            if (i == 1) continue
            val title = el.select("div.title-left > h4").text()
            val moreUrl = el.select("div.title-right > a").attr("href").trimDomain()
            val homeItemBeanList = getAnimeList(el.select("div.public-list-box"))
            homeBeanList.add(HomeBean(title = title, moreUrl = moreUrl, animes = homeItemBeanList))
        }

        return homeBeanList
    }

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        val source = DownloadManager.getHtml("${baseUrl}/$detailUrl")
        val document = Jsoup.parse(source)
        val main = document.select("div.vod-detail")
        val title = main.select("h3").text()
        val desc = main.select("div#height_limit").text()
        val imgUrl = main.select("img").attr("data-src").padDomain()
        val tags =
            main.select("div.slide-info").last()?.select("a")?.map { it.text() }?.toMutableList()
                ?.also { it.removeAt(it.lastIndex) } ?: emptyList()
        val updateTime = main.select("span.slide-info-remarks")[1].text()
        val episodes = getAnimeEpisodes(document.select("div.anthology-list").select("ul"))
        val relatedAnimes =
            getAnimeList(document.select("div.box-width.wow").select("div.public-list-box"))
        return AnimeDetailBean(title, imgUrl, desc, "", tags, updateTime, episodes, relatedAnimes)
    }

    private fun getAnimeList(elements: Elements): List<AnimeBean> {
        val animeList = mutableListOf<AnimeBean>()
        elements.forEach { el ->
            el.select("div.public-list-div > a").apply {
                val title = attr("title")
                val url = attr("href")
                val imgUrl = select("img").attr("data-src").padDomain()
                val episodeName = select("span.public-list-prb").text()
                animeList.add(
                    AnimeBean(
                        title = title,
                        img = imgUrl,
                        url = url,
                        episodeName = episodeName
                    )
                )
            }
        }
        return animeList
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        val url = "${baseUrl}/$episodeUrl"
        val source = DownloadManager.getHtml(url)
        val document = Jsoup.parse(source)
        var elements = document.select("div.player-right")
        if (elements.isEmpty()) {
            elements = document.select("div.player-info")
        }
        val title = elements.select("h2").text()
        var episodeName = ""
        val episodes = getAnimeEpisodes(
            elements.select("div.anthology-list-box").select("ul"),
            action = { episodeName = it })
        val videoUrl = getVideoUrl(document)
        return VideoBean(title, videoUrl, episodeName, episodes)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun getVideoUrl(document: Document): String {
        val videoUrlTarget = document.select("div.player-box > div.player-left > script")[0].data()
        val videoUrlRegex = """"url":"(.*?)","url_next"""".toRegex()
        val rawVideoUrl = videoUrlRegex.find(videoUrlTarget)?.groupValues?.get(1)
            ?: throw IllegalStateException("video url is empty")

        val encodedVideoUrl = String(Base64.decode(rawVideoUrl), Charsets.UTF_8)
        return Uri.decode(encodedVideoUrl)
    }

    private fun getAnimeEpisodes(
        elements: Elements,
        action: (String) -> Unit = {}
    ): List<EpisodeBean> {
        if (elements.isEmpty()) return emptyList()
        val dramaElements = elements[0].select("li").select("a")//剧集列表
        val episodes = mutableListOf<EpisodeBean>()
        dramaElements.forEach { el ->
            val name = el.text()
            val url = el.attr("href")
            if (el.select("em.play-on").isNotEmpty()) {
                action(name)
            }
            episodes.add(EpisodeBean(name, url))
        }

        return episodes
    }

    private fun String.padDomain(): String {
        return "$baseUrl$this"
    }

    private fun String.trimDomain() = replace(baseUrl, "")
}