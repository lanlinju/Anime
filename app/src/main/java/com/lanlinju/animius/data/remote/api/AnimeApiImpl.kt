package com.lanlinju.animius.data.remote.api

import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.AnimeDetailBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import com.lanlinju.animius.data.remote.dto.VideoBean
import com.lanlinju.animius.util.SourceHolder
import com.lanlinju.animius.util.SourceMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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

    override suspend fun getSearchData(query: String, page: Int, mode: SourceMode): List<AnimeBean> {
        val animeSource = SourceHolder.getSource(mode)
        return animeSource.getSearchData(query, page)
    }

    override suspend fun getWeekDate(): Map<Int, List<AnimeBean>> {
        val animeSource = SourceHolder.currentSource
        return animeSource.getWeekData()
    }
}