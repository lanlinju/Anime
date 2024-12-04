package com.lanlinju.animius.data.remote.parse

import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.AnimeDetailBean
import com.lanlinju.animius.data.remote.dto.EpisodeBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import com.lanlinju.animius.data.remote.dto.VideoBean
import com.lanlinju.animius.util.DownloadManager
import com.lanlinju.animius.util.getDefaultDomain
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object YhdmSource : AnimeSource {
    override val DEFAULT_DOMAIN: String = "http://www.iyinghua.io"
    override var baseUrl = getDefaultDomain()

    override suspend fun getHomeData(): List<HomeBean> {
        val source = DownloadManager.getHtml(baseUrl)
        val document = Jsoup.parse(source)
        val elements = document.select("div.firs > div.dtit")
        val data = document.select("div.firs > div.img")
        val homeBeanList = mutableListOf<HomeBean>()
        elements.forEachIndexed { i, el ->
            val moreUrl = el.select("h2 > a").attr("href")
            val title = el.select("h2 > a").text()
            val homeItemBeanList = mutableListOf<AnimeBean>()
            val animes = data.get(i).select("ul > li")
            for (anime in animes) {
                val animeInfo = anime.select("a")
                with(animeInfo) {
                    val animeTitle = get(1).text()
                    val url = get(1).attr("href")
                    val img = get(0).select("img").attr("src")
                    val episode = if (animeInfo.size == 3) get(2).text() else ""
                    homeItemBeanList.add(AnimeBean(animeTitle, img, url, episode))
                }
            }
            homeBeanList.add(HomeBean(title, moreUrl, homeItemBeanList))
        }
        return homeBeanList
    }

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        val source = DownloadManager.getHtml("${baseUrl}/$detailUrl")

        val document = Jsoup.parse(source)
        return with(document) {
            val title = select("h1").text()
            val desc = select("div.info").text()
            val img = select("div.thumb > img").attr("src")
            /*val score = select("div.score > em").text()
            val updateTime =
                select("div.sinfo > p").let { if (it.size > 1) it[1].text() else it[0].text() }*/
            val tagElements = Elements()
            val tagInfo = select("div.sinfo > span")
            tagInfo.forEachIndexed { i, tag ->
                if (i != 5 && i != 3)
                    tagElements.addAll(tag.select("a"))
            }
            val tags = mutableListOf<String>()
            tagElements.forEach { tags.add(it.text().uppercase()) }
            val episodes = getAnimeEpisodes(this.select("div.movurl"))
            val relatedAnimes = getRelatedAnimes(this)
            AnimeDetailBean(title, img, desc, tags, relatedAnimes, episodes)
        }
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        val source = DownloadManager.getHtml("$baseUrl/$episodeUrl")
//        val document = Jsoup.parse(source)
//        val head = document.select("h1").text().split("：")
//        val title = head[0]
//        val episodeName = head[1]
//        val episodes = getAnimeEpisodes(document.select("div.movurls"))
        val videoUrl = getVideoUrl(source)
        return VideoBean(videoUrl)
    }

    private fun getVideoUrl(source: String): String {
        val document = Jsoup.parse(source)
        val elements = document.select("div.playbo > a")
        val re = """changeplay\('(.*)\$""".toRegex()
        val url = re.find(elements[0].attr("onclick"))!!.groupValues[1]
        return url
    }

    override suspend fun getSearchData(query: String, page: Int): List<AnimeBean> {
        val source = DownloadManager.getHtml("${baseUrl}/search/$query/?page=${page}")
        val document = Jsoup.parse(source)
        val elements = document.select("div.lpic > ul > li")
        val animeList = mutableListOf<AnimeBean>()
        elements.forEach { el ->
            val title = el.select("h2").text()
            val url = el.select("h2 > a").attr("href")
            val img = el.select("img").attr("src")
            animeList.add(AnimeBean(title = title, img = img, url = url))
        }
        return animeList
    }

    override suspend fun getWeekData(): Map<Int, List<AnimeBean>> {
        val source = DownloadManager.getHtml(baseUrl)

        val document = Jsoup.parse(source)
        val elements = document.select("div.tlist > ul")
        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        elements.forEachIndexed { i, element ->
            val dayList = mutableListOf<AnimeBean>()
            element.select("li").forEach { el ->
                with(el.select("a")) {
                    val title = get(1).text()
                    val url = get(1).attr("href")
                    val episode = get(0).text()
                    dayList.add(
                        AnimeBean(
                            title = title,
                            img = "",
                            url = url,
                            episodeName = episode
                        )
                    )
                }
            }
            weekMap[i] = dayList
        }
        return weekMap
    }

    private fun getAnimeEpisodes(elements: Elements): List<EpisodeBean> {
        val dramaElements = elements.select("ul > li") //剧集列表
        val episodes = mutableListOf<EpisodeBean>()
        dramaElements.forEach { el ->
            val name = el.select("a").text()
            val url = el.select("a").attr("href")
            episodes.add(EpisodeBean(name, url))
        }
        return episodes
    }

    private fun getRelatedAnimes(document: Document): List<AnimeBean> {
        val els = document.select("div.pics > ul > li") //相关推荐
        val relatedAnimes = mutableListOf<AnimeBean>()
        els.forEach { el ->
            val title = el.select("h2 > a").text()
            val img = el.select("img").attr("src")
            val url = el.select("h2 > a").attr("href")
            relatedAnimes.add(AnimeBean(title = title, img = img, url = url))
        }
        return relatedAnimes
    }

}