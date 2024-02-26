package com.sakura.anime.data.remote.api

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean
import com.sakura.anime.util.SourceHolder
import com.sakura.anime.util.SourceMode
import javax.inject.Inject

class AnimeApiImpl @Inject constructor() : AnimeApi {
    override suspend fun getHomeAllData(): List<HomeBean> {
        val animeSource = SourceHolder.currentSource
        return animeSource.getHomeData()
    }

    override suspend fun getAnimeDetail(detailUrl: String, mode: SourceMode): AnimeDetailBean {
        val animeSource = SourceHolder.getSource(mode)
        return animeSource.getAnimeDetail(detailUrl)
    }

    override suspend fun getVideoData(episodeUrl: String, mode: SourceMode): VideoBean {
        val animeSource = SourceHolder.getSource(mode)
        return animeSource.getVideoData(episodeUrl)
    }

    override suspend fun getSearchData(query: String, page: Int): List<AnimeBean> {
        val animeSource = SourceHolder.currentSource
        return animeSource.getSearchData(query, page)
    }

    override suspend fun getWeekDate(): Map<Int, List<AnimeBean>> {
        val animeSource = SourceHolder.currentSource
        return animeSource.getWeekData()
    }
}