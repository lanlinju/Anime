package com.sakura.anime.data.remote.api

import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.parse.AnimeJsoupParser
import com.sakura.anime.util.BASE_URL
import com.sakura.anime.util.DownloadManager
import javax.inject.Inject

class AnimeApiImpl @Inject constructor(
    private val animeJsoupParser: AnimeJsoupParser,
    private val downloadManager: DownloadManager
) : AnimeApi {
    override suspend fun getHomeAllData(): List<HomeBean> {
        val source = downloadManager.getHtml(BASE_URL)
        return animeJsoupParser.getHomeAllData(source)
    }

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        val source = downloadManager.getHtml("$BASE_URL/show/$detailUrl")
        return animeJsoupParser.getAnimeDetail(source)
    }
}