package com.sakura.anime.data.remote.parse

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean

interface AnimeSource {
    suspend fun getHomeData(): List<HomeBean>

    suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean

    suspend fun getVideoData(episodeUrl: String): VideoBean

    suspend fun getSearchData(query: String, page: Int): List<AnimeBean>

    suspend fun getWeekData(): Map<Int, List<AnimeBean>>
}