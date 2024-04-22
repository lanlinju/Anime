package com.sakura.anime.data.remote.api

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean
import com.sakura.anime.util.SourceMode

interface AnimeApi {
    suspend fun getHomeAllData(): List<HomeBean>

    suspend fun getAnimeDetail(detailUrl: String, mode: SourceMode): AnimeDetailBean

    suspend fun getVideoData(episodeUrl: String, mode: SourceMode): VideoBean

    suspend fun getSearchData(query: String, page: Int, mode: SourceMode): List<AnimeBean>

    suspend fun getWeekDate(): Map<Int, List<AnimeBean>>
}