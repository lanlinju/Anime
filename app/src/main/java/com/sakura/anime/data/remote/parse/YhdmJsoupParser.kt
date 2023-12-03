package com.sakura.anime.data.remote.parse

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.EpisodeBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.util.TABS
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object YhdmJsoupParser : AnimeJsoupParser {
    override suspend fun getHomeAllData(source: String): List<HomeBean> {
        val document = Jsoup.parse(source)
        val titles = document.select("div.firs > div.dtit")
        val data = document.select("div.firs > div.img")
        val homeBeanList = mutableListOf<HomeBean>()
        titles.forEachIndexed { i, title ->
            val moreUrl = title.select("h2 > a").attr("href")
            val title = title.select("h2 > a").text()
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

    override suspend fun getAnimeDetail(source: String): AnimeDetailBean {
        val document = Jsoup.parse(source)
        return with(document) {
            val title = select("h1").text()
            val desc = select("div.info").text()
            val score = select("div.score > em").text()
            val img = select("div.thumb > img").attr("src")
            val updateTime =
                select("div.sinfo > p").let { if (it.size > 1) it[1].text() else it[0].text() }
            val tagElements = Elements()
            val tagInfo = select("div.sinfo > span")
            tagInfo.forEachIndexed { i, tag ->
                if (i != 5 && i != 3)
                    tagElements.addAll(tag.select("a"))
            }
            val tags = mutableListOf<String>()
            tagElements.forEach { tags.add(it.text().uppercase()) }
            val episodes = getAnimeEpisodes(this)
            val relatedAnimes = getRelatedAnimes(this)
            AnimeDetailBean(
                AnimeBean(title, img, url = ""),
                desc,
                score,
                tags,
                updateTime,
                episodes,
                relatedAnimes
            )
        }
    }

    override suspend fun getVideoUrl(source: String): String {
        val document = Jsoup.parse(source)
        val elements = document.select("div.playbo > a")
        val re = """changeplay\('(.*)\$""".toRegex()
        val url = re.find(elements[0].attr("onclick"))!!.groupValues[1]
        return url
    }

    override suspend fun getSearchData(source: String): List<AnimeBean> {
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

    override suspend fun getWeekData(source: String): Map<String, List<AnimeBean>> {
        val document = Jsoup.parse(source)
        val elements = document.select("div.tlist > ul")
        val weekMap = mutableMapOf<String, List<AnimeBean>>()
        elements.forEachIndexed { i, element ->
            val dayList = mutableListOf<AnimeBean>()
            element.select("li").forEach { el ->
                with(el.select("a")) {
                    val title = get(1).text()
                    val url = get(1).attr("href")
                    val episode = get(0).text()
                    dayList.add(AnimeBean(title = title, img = "", url = url, episode = episode))
                }
            }
            weekMap.put(TABS[i], dayList)
        }
        return weekMap;
    }

    private fun getAnimeEpisodes(document: Document): List<EpisodeBean> {
        val dramaElements = document.select("div.movurl > ul > li") //剧集列表
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