package com.sakura.anime.data.remote.parse

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.EpisodeBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean
import com.sakura.anime.util.DownloadManager
import com.sakura.anime.util.decryptData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object MxdmSource : AnimeSource {

    const val BASE_URL = "http://www.mxdm9.com/"
    const val BASE_M3U8 = "https://danmu.yhdmjx.com/m3u8.php?url="
    const val AES_KEY = "57A891D97E332A9D"
    override suspend fun getHomeData(): List<HomeBean> {
        val source = DownloadManager.getHtml(BASE_URL)
        val document = Jsoup.parse(source)

        val homeBeanList = mutableListOf<HomeBean>()
        document.select("div.module").take(6).forEach { element ->
            val title = element.select("h2").text()
            val moreUrl = element.select("a.more").attr("href")

            val homeItemBeanList = getAnimeList(element.select("div.module-item"))

            homeBeanList.add(HomeBean(title = title, moreUrl = moreUrl, animes = homeItemBeanList))
        }

        return homeBeanList
    }

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        val source = DownloadManager.getHtml("$BASE_URL/$detailUrl")
        val document = Jsoup.parse(source)

        val main = document.select("main")
        val title = main.select("h1").text()
        val desc = main.select("div.video-info-content").text()
        val imgUrl = main.select("div.module-item-pic > img").attr("data-src")
        val updateTime = main.select("div.video-info-item")[4].text()
        val tags = main.select("div.tag-link > a").map { it.text() }
        val episodes = getAnimeEpisodes(main)
        val relatedAnimes =
            getAnimeList(main.select("div.module-items")[0].select("div.module-item"))
        return AnimeDetailBean(title, imgUrl, desc, "", tags, updateTime, episodes, relatedAnimes)
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        val source = DownloadManager.getHtml("$BASE_URL/${episodeUrl}")
        val document = Jsoup.parse(source)

        val elements = document.select("div.video-info-header")
        val title = elements.select("h1 > a").attr("title")
        val episodeName = elements.select("span.btn-pc").text()
        val videoUrl = getVideoUrl(document)
        val episodes = getAnimeEpisodes(document.select("main"))

        return VideoBean(title, videoUrl, episodeName, episodes)
    }

    override suspend fun getSearchData(query: String, page: Int): List<AnimeBean> {
        val source = DownloadManager.getHtml("$BASE_URL/search/$query----------$page---.html")
        val document = Jsoup.parse(source)

        val animeList = mutableListOf<AnimeBean>()
        document.select("div.module-search-item").forEach { el ->
            val title = el.select("h3").text()
            val url = el.select("h3 > a").attr("href")
            val imgUrl = el.select("img").attr("data-src")
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url))
        }
        return animeList
    }

    override suspend fun getWeekData(): Map<Int, List<AnimeBean>> {
        val source = DownloadManager.getHtml(BASE_URL)
        val document = Jsoup.parse(source)

        val elements = document.select("ul.mxoneweek-list")
        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        elements.forEachIndexed { index, element ->
            val dayList = mutableListOf<AnimeBean>()
            element.select("li").forEach { el ->
                val title = el.select("a > span")[0].text()
                val episodeName = el.select("a > span")[1].text()
                val url = el.select("a").attr("href")
                dayList.add(AnimeBean(title, "", url, episodeName))
            }
            weekMap[index] = dayList
        }
        return weekMap
    }

    private fun getAnimeList(elements: Elements): List<AnimeBean> {
        val animeList = mutableListOf<AnimeBean>()
        elements.forEach { el ->
            val title = el.select("a").attr("title")
            val url = el.select("a").attr("href")
            val imgUrl = el.select("img").attr("data-src")
            val episodeName = el.select("div.module-item-text").text()
            animeList.add(
                AnimeBean(
                    title = title,
                    img = imgUrl,
                    url = url,
                    episodeName = episodeName
                )
            )
        }
        return animeList
    }

    private fun getAnimeEpisodes(elements: Elements): List<EpisodeBean> {
        val dramaElements = elements.select("div.module-blocklist > div.scroll-content > a") //剧集列表
        val episodes = mutableListOf<EpisodeBean>()
        dramaElements.forEach { el ->
            val name = el.text()
            val url = el.attr("href")
            episodes.add(EpisodeBean(name, url))
        }

        return episodes
    }

    private suspend fun getVideoUrl(document: Document): String {
        val urlTarget = document.select("div.player-wrapper > script")[0].data()
        val urlRegex = """"url":"(.*?)","url_next"""".toRegex()
        val url = urlRegex.find(urlTarget)!!.groupValues[1]

        val doc = Jsoup.parse(DownloadManager.getHtml(BASE_M3U8 + url))
        val ivTarget = doc.select("head > script")[1].data()
        val ivRegex = """var bt_token = "(.*?)"""".toRegex()
        val iv = ivRegex.find(ivTarget)!!.groupValues[1]

        val videoUrlTarget = doc.select("body > script")[0].data()
        val videoUrlRegex = """getVideoInfo\("(.*?)"""".toRegex()
        val encryptedVideoUrl = videoUrlRegex.find(videoUrlTarget)!!.groupValues[1]
        return decryptData(encryptedVideoUrl, key = AES_KEY, iv = iv)
    }

}