package com.lanlinju.animius.data.remote.parse

import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.AnimeDetailBean
import com.lanlinju.animius.data.remote.dto.EpisodeBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import com.lanlinju.animius.data.remote.dto.VideoBean
import com.lanlinju.animius.util.DownloadManager
import com.lanlinju.animius.util.decryptData
import com.lanlinju.animius.util.getDefaultDomain
import com.lanlinju.animius.util.log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.security.MessageDigest


/**
 * 嘶哩嘶哩域名发布地址：https://silisili-link.github.io/
 */
object SilisiliSource : AnimeSource {

    private const val LOG_TAG = "SilisiliSource"

    override val DEFAULT_DOMAIN: String = "https://www.silisili.link"
    override var baseUrl = getDefaultDomain()

    override suspend fun getHomeData(): List<HomeBean> {
        val headers = mapOf(
            Pair("Cookie", "silisili=on;path=/;max-age=86400;appdw=on"),
            Pair("User-Agent", "Mozilla/5.0 (Linux; Android 6.0) Mobile"),
        )
        val source = DownloadManager.getHtml(baseUrl, headers)
        val document = Jsoup.parse(source)
        val homeList = mutableListOf<HomeBean>()

        val elements = document.select("div.conch-content").select("div.container")
        for ((i, el) in elements.withIndex()) {

            if (i == 0 || i == 2 || i == 3 || i == 8) continue

            val title = el.select("h2").text()
            val moreUrl = el.select("div.hl-rb-head > a").attr("href")
            val animeList = mutableListOf<AnimeBean>()
            el.select("ul.hl-vod-list > li").forEach {
                it.select("a").apply {
                    val animeTitle = attr("title")
                    val url = attr("href")
                    val imgUrl = attr("data-original")
                    val episodeName = el.select("div.hl-pic-text").text()
                    animeList.add(AnimeBean(animeTitle, imgUrl, url, episodeName))
                }
            }
            homeList.add(HomeBean(title = title, moreUrl = moreUrl, animes = animeList))
        }

        return homeList
    }

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        val source = getHtml("$baseUrl/$detailUrl")
        val document = Jsoup.parse(source)

        val animeTitle = document.select("h1.entry-title").text().split(" ").first()
        val img = document.select("div.v_sd_l > img").attr("src")
        val tags = document.select("p.data").select("a")

        val tagTitles = mutableListOf<String>()
        for (tag in tags) {
            if (tag.text().isEmpty()) continue
            tagTitles.add(tag.text().uppercase())
        }

        val span = document.select("span.text-muted")
        var updateTime = ""
        for (s in span) {
            if (s.text().contains("更新")) {
                updateTime = s.parent()!!.text()
                break
            }
        }
        val score = document.select("div.v_sd_r").select("span.data-favs-num").text()
        val desc = document.select("div.v_cont")
        desc.select("div.v_sd").remove()
        desc.select("span").remove()
        val description = desc.text()

        val episodes = getAnimeEpisodes(document)
        val relatedAnimes = getAnimeList(document)

        return AnimeDetailBean(
            animeTitle, img, description, score, tagTitles, updateTime, episodes, relatedAnimes
        )
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
//        val source = getHtml("$baseUrl/$episodeUrl")
//        val document = Jsoup.parse(source)
//
//        val title = document.select("h1 > a").text()
//        val episodeName = document.select("span.nidname").text()
//        val episodes = getAnimeEpisodes(document)
        val videoUrl = getVideoUrl("$baseUrl/$episodeUrl")

        return VideoBean(videoUrl)
    }

    override suspend fun getSearchData(query: String, page: Int): List<AnimeBean> {
        val source = getHtml("$baseUrl/vodsearch$query/page/$page/")
        val document = Jsoup.parse(source)
        val animeList = mutableListOf<AnimeBean>()
        document.select("article.post-list").forEach { el ->
            val title = el.select("div.search-image").select("a").attr("title")
            val url = el.select("div.search-image").select("a").attr("href")
            val imgUrl = el.select("div.search-image").select("img").attr("srcset")
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url))
        }

        return animeList
    }

    override suspend fun getWeekData(): Map<Int, List<AnimeBean>> {
        val source = getHtml(baseUrl)
        val document = Jsoup.parse(source)

        val weekMap = mutableMapOf<Int, MutableList<AnimeBean>>()
        val dayElements = document.select("div.week_item").select("ul.tab-content")
        dayElements.size.log(LOG_TAG) // 14

        dayElements.movePosition(0, 6) // 日漫 sunday
        dayElements.movePosition(7, 13) // 国漫 sunday

        for (i in 0 until 14) {
            val dayList = mutableListOf<AnimeBean>()

            dayElements[i].select("li").forEach { li ->
                val title = li.select("a.item-cover").attr("title")
                val url = li.select("a.item-cover").attr("href")
                val spanStyle = li.select("span[style]").attr("style")
                val img = getImgUrl(spanStyle)
                val episodeName = li.select("p.num").text()
                dayList.add(AnimeBean(title, img, url, episodeName))
            }

            weekMap[i % 7]?.addAll(dayList) ?: weekMap.set(i, dayList)

        }
        return weekMap
    }

    private fun MutableList<Element>.movePosition(current: Int, destination: Int) {
        val tmp = this[current]
        this.removeAt(current)
        this.add(destination, tmp)
    }

    private fun getAnimeEpisodes(document: Document): List<EpisodeBean> {
        val playlist = document.select("div.play-pannel-list").first()!!
        val episodes = mutableListOf<EpisodeBean>()
        playlist.select("li > a").forEach { el ->
            val name = el.text()
            val url = el.attr("href")
            episodes.add(EpisodeBean(name, url))
        }
        return episodes
    }

    private fun getAnimeList(document: Document): List<AnimeBean> {
        val animeList = mutableListOf<AnimeBean>()
        document.select("div.vod_hl_list").select("a").forEach { el ->
            val title = el.select("div.list-body").text()
            val img: String = getImgUrl(el.select("i.thumb").attr("style"))
            val url = el.attr("href")
            animeList.add(AnimeBean(title, img, url, ""))
        }

        return animeList
    }

    private fun getVideoUrl(url: String): String {
        val encryptData = postRequest(url)
        val params1 = encryptData.substring(0, 9)
        val params2 = encryptData.substring(9)

        val ivAndKey = md5DigestAsHex(params1)
        val iv = ivAndKey.substring(0, 16)
        val key = ivAndKey.substring(16)

        val result = decryptData(params2, key = key, iv = iv)
        val urlRegex = """"url":"(.*?)",""".toRegex()
        return urlRegex.find(result)!!.groupValues[1].replace("\\", "")
    }

    private fun getImgUrl(urlTarget: String): String {
        val urlRegex = """url\((.*?)\)""".toRegex()
        return urlRegex.find(urlTarget)?.groupValues?.get(1) ?: ""
    }

    private suspend fun getHtml(url: String): String {
        val headerMap = mapOf(Pair("Cookie", "silisili=on;path=/;max-age=86400"))
        return DownloadManager.getHtml(url, headerMap)
    }

    private fun postRequest(url: String): String {
        val client = OkHttpClient.Builder().build()
        val body = FormBody.Builder().add("player", "sili").build()
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return response.body!!.charStream().readText()
    }

    /**
     * 返回长度为32的16进制字符串
     */
    private fun md5DigestAsHex(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(input.toByteArray()).toHex()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun ByteArray.toHex() = asUByteArray().joinToString(separator = "") { byte ->
        byte.toString(16).padStart(2, '0')
    }
}