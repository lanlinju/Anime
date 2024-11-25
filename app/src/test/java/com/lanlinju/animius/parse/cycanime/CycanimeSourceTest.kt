package com.lanlinju.animius.parse.cycanime

import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.EpisodeBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.junit.Test
import java.io.File

class CycanimeSourceTest {
    val homeUrl = "https://www.cyc-anime.net/"
    val detailUrl = "https://www.cyc-anime.net/bangumi/3803.html"

    val relativePath = "./src/test/java/com/sakura/anime/parse/cycanime/html/" // 保存路径
    val homeHtml = relativePath + "home.html"
    val detailHtml = relativePath + "detail.html"
    val playerHtml = relativePath + "player.html"
    val searchHtml = relativePath + "search.html"

    @Test
    fun getVideoData() = runBlocking {
        val document = Jsoup.parse(File(playerHtml))
        val title = document.select("div.player-right").select("h2").text()
        val videoUrl = ""
        var episodeName = ""
        val episodes = getAnimeEpisodes(document, action = { episodeName = it })
        println("title: $title, videoUrl: $videoUrl, episodeName: $episodeName, episodes: $episodes")
    }

    @Test
    fun getSearchData() = runBlocking {
        val document = Jsoup.parse(File(searchHtml))
        val animeList = mutableListOf<AnimeBean>()
        document.select("div.public-list-box").forEach { el ->
            val title = el.select("div.thumb-txt").text()
            val url = el.select("a.public-list-exp").attr("href")
            val imgUrl = el.select("img").attr("data-src")
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url))
        }
        println(animeList)
    }

    @Test
    fun getAnimeDetail() = runBlocking {
        val document = Jsoup.parse(File(detailHtml))
        val detailInfo = document.select("div.detail-info")
        val title = detailInfo.select("h3").text()
        val desc = document.select("div#height_limit").text()
        val imgUrl = document.select("div.detail-pic > img").attr("data-src")

        val tags = detailInfo.select("span.slide-info-remarks").map { it.text() }

        val episodes = getAnimeEpisodes(document)
        val relatedAnimes =
            getAnimeList(document.select("div.box-width.wow").select("div.public-list-box"))
        println("title: $title," + " desc: $desc, imgUrl: $imgUrl, tags: $tags, episodes: $episodes, relatedAnimes: $relatedAnimes")
    }

    private fun getAnimeEpisodes(
        document: Document,
        action: (String) -> Unit = {}
    ): List<EpisodeBean> {
        return document.select("div.anthology-list")
            .select("li").map {
                if (it.select("em").isNotEmpty()) {
                    action(it.text())
                }
                EpisodeBean(it.text(), it.select("a").attr("href"))
            }
    }

    @Test
    fun testParseWeekData() = runBlocking {
        val document = Jsoup.parse(File(homeHtml))
        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        document.select("div#week-module-box")
            .select("div.public-r").forEachIndexed { index, element ->
                val dayList = getAnimeList(element.select("div.public-list-box"))
                weekMap[index] = dayList
            }
        println("weekMap: $weekMap")
    }

    @Test
    fun testParseHomeData() = runBlocking {
        val document = Jsoup.parse(File(homeHtml))
        val homeBeanList = mutableListOf<HomeBean>()
        document.select("div.box-width.wow.fadeInUp")
            .apply {
                removeFirst()
                removeFirst()
            }
            .forEach { element ->
                val title = element.select("h4").text()
                val moreUrl = element.select("a.button").attr("href")
                val homeItemBeanList = getAnimeList(element.select("div.public-list-box"))
                println("------------------------------------------------")
                println("title: $title, moreUrl: $moreUrl")
                homeBeanList.add(
                    HomeBean(
                        title = title,
                        moreUrl = moreUrl,
                        animes = homeItemBeanList
                    )
                )
            }

        println(homeBeanList)
    }

    fun getAnimeList(elements: Elements): List<AnimeBean> {
        val animeList = mutableListOf<AnimeBean>()
        elements.forEach { el ->
            val title = el.select("div.public-list-button > a").text()
            val url = el.select("div.public-list-button > a").attr("href")
            val imgUrl = el.select("img").attr("data-src")
            val episodeName = el.select("div.public-list-subtitle").text()
            println("title: $title, url: $url, imgUrl: $imgUrl, episodeName: $episodeName")
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url, episodeName))
        }
        return animeList
    }
}