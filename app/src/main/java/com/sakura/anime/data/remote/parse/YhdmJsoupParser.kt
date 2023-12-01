package com.sakura.anime.data.remote.parse

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.HomeBean
import org.jsoup.Jsoup

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

}