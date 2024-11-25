package com.lanlinju.animius.parse.gogoanime

import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.EpisodeBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.junit.Test
import java.io.File
import java.time.LocalDate
import kotlin.system.measureTimeMillis

class GogoanimeTest {
    val relativePath = "./src/test/java/com/sakura/anime/parse/gogoanime/html/" // 保存路径
    val homeHtml = relativePath + "home.html"
    val weekHtml = relativePath + "week.html"
    val detailHtml = relativePath + "detail.html"
    val playerHtml = relativePath + "player.html"
    val searchHtml = relativePath + "search.html"

    @Test
    fun testGetSearchData() = runBlocking {
        val document = Jsoup.parse(File(searchHtml))
        val animeList = getAnimeList(document.select("div.listupd > article.bs"))
        println(animeList)
    }

    @Test
    fun testGetWeekData() = runBlocking {
        // https://gogoanime.by/page/3/?s=d
        val document = Jsoup.parse(File(weekHtml))
        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        document.select("div.bixbox.schedulepage").forEachIndexed { index, element ->
            val offset = LocalDate.now().dayOfWeek.value - 1
            val dayList = getAnimeList(element.select("div.bs"))
            weekMap[(index + offset).mod(7)] = dayList
        }
        println("weekMap: $weekMap")
    }

    @Test
    fun getVideoData() = runBlocking {
        val document = Jsoup.parse(File(playerHtml))
        val videoUrl = ""
        val titleAndEpisodeName = document.select("h1").text()
        val result = extractTitleAndEpisode(titleAndEpisodeName)
        val title = result.first
        var episodeName = result.second
        val allEpisodesUrl = document.select("div.nvs.nvsc > a").attr("href")
        val episodes = getAnimeEpisodes(Jsoup.parse(File(detailHtml)))
        println("title: $title, videoUrl: $videoUrl, episodeName: $episodeName, episodes: $episodes")
    }

    private fun extractTitleAndEpisode(input: String): Pair<String, String> {
        //One Piece: Gyojin Tou-hen Episode 2 English Subbed
        val regex = Regex("""(.+?) Episode (\d+)""")
        val matchResult = regex.find(input)
        return matchResult!!.let {
            val title = it.groupValues[1].trim()
            val episode = it.groupValues[2].trim()
            Pair(title, episode)
        }
    }

    @Test
    fun getAnimeDetail() = runBlocking {
        val document = Jsoup.parse(File(detailHtml))
        val detailInfo = document.select("div.bigcontent")
        val title = detailInfo.select("h1").text()
        val desc = detailInfo.select("p").text()
        val imgUrl = detailInfo.select("img").attr("src")

        val tags = detailInfo.select("div.genxed > a").map { it.text() }

        val episodes = getAnimeEpisodes(document)
        val relatedAnimes = getAnimeList(document.select("div.listupd > article"))
        println("title: $title," + " desc: $desc,\n imgUrl: $imgUrl, tags: $tags, episodes: $episodes, relatedAnimes: $relatedAnimes")
    }

    private fun getAnimeEpisodes(document: Document): List<EpisodeBean> {
        return document.select("div.ep-list")
            .select("li").map {
                EpisodeBean(it.text(), it.select("a").attr("href"))
            }
    }

    @Test
    fun testGetHomeData() = runBlocking {
        val document = Jsoup.parse(File(homeHtml))
        val homeBeanList = mutableListOf<HomeBean>()
        document.select("div.bixbox").let { element ->
            val title = element.select("h3").text()
            val homeItemList = getAnimeList(element.select("article.bs"))
            println("title: $title")
            homeBeanList.add(
                HomeBean(
                    title = title,
                    animes = homeItemList
                )
            )
        }
        println(homeBeanList)
    }

    fun getAnimeList(elements: Elements): List<AnimeBean> {
        val animeList = mutableListOf<AnimeBean>()
        elements.forEach { el ->
            val title = el.selectFirst("div.tt")!!.ownText()
            val url = el.select("a").attr("href").extractDetailUrl()
            val imgUrl = el.select("img").attr("src")
            val episodeName = el.select("div.bt > span.epx").text()
            println("title: $title, url: $url, imgUrl: $imgUrl, episodeName: $episodeName")
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url, episodeName))
        }
        return animeList
    }

    private fun String.extractDetailUrl(): String {
        if (!this.contains("-episode")) return this
        return this.substringBefore("-episode")
    }

    @Test
    fun testExtractEpisodeId() = runBlocking {
        val input = "https://gogoanime.by/dragon-ball-daima-episode-3-english-subbed/"
        val regex = Regex(""".*/(.*)-english-subbed/""")
        val expected = "dragon-ball-daima-episode-3"
        val actual = regex.find(input)?.groupValues[1]
            ?: throw IllegalArgumentException("URL does not match the expected pattern")
        assertEquals(expected, actual)
    }

    @Test
    fun testSequence() {
        // 模拟视频质量数据
        val result = listOf(
            Source("1080p", "http://example.com/1080p"),
            Source("720p", "http://example.com/720p"),
            Source("480p", "http://example.com/480p")
        )

        val preferredQualities = listOf("1080p", "720p", "480p", "360p")

        // 小规模测试
        testSmallScale(result, preferredQualities)

        // 大规模测试
        testLargeScale(preferredQualities)

        // 复杂计算测试
        testComplexCalculation(preferredQualities)
    }

    data class Source(val quality: String, val url: String)

    fun testSmallScale(result: List<Source>, preferredQualities: List<String>) {
        println("Small Scale Test:")
        val timeAsSequence = measureTimeMillis {
            val videoUrl = preferredQualities
                .asSequence()
                .mapNotNull { quality -> result.find { it.quality == quality }?.url }
                .firstOrNull()
            println("Video URL (asSequence): $videoUrl")
        }

        val timeNoSequence = measureTimeMillis {
            val videoUrl = preferredQualities
                .mapNotNull { quality -> result.find { it.quality == quality }?.url }
                .firstOrNull()
            println("Video URL (no sequence): $videoUrl")
        }

        println("Time with asSequence: $timeAsSequence ms")
        println("Time without asSequence: $timeNoSequence ms")
        println()
    }

    fun testLargeScale(preferredQualities: List<String>) {
        println("Large Scale Test:")
        val largeResult = List(100000) { Source("quality-$it", "http://example.com/quality-$it") }
        val qualitiesToSearch = preferredQualities + List(99996) { "unmatched-$it" } // 添加大量无匹配的质量

        val timeAsSequence = measureTimeMillis {
            val videoUrl = qualitiesToSearch
                .asSequence()
                .mapNotNull { quality -> largeResult.find { it.quality == quality }?.url }
                .firstOrNull()
            println("Video URL (asSequence): $videoUrl")
        }

        val timeNoSequence = measureTimeMillis {
            val videoUrl = qualitiesToSearch
                .mapNotNull { quality -> largeResult.find { it.quality == quality }?.url }
                .firstOrNull()
            println("Video URL (no sequence): $videoUrl")
        }

        println("Time with asSequence: $timeAsSequence ms")
        println("Time without asSequence: $timeNoSequence ms")
        println()
    }

    fun testComplexCalculation(preferredQualities: List<String>) {
        println("Complex Calculation Test:")
        fun expensiveFind(quality: String): String? {
            // 模拟耗时计算
            Thread.sleep(1) // 每次调用耗时 1 毫秒
            return if (quality == "1080p") "http://example.com/1080p" else null
        }

        val timeAsSequence = measureTimeMillis {
            val videoUrl = preferredQualities
                .asSequence()
                .mapNotNull { quality -> expensiveFind(quality) }
                .firstOrNull()
            println("Video URL (asSequence): $videoUrl")
        }

        val timeNoSequence = measureTimeMillis {
            val videoUrl = preferredQualities
                .mapNotNull { quality -> expensiveFind(quality) }
                .firstOrNull()
            println("Video URL (no sequence): $videoUrl")
        }

        println("Time with asSequence: $timeAsSequence ms")
        println("Time without asSequence: $timeNoSequence ms")
        println()
    }

}