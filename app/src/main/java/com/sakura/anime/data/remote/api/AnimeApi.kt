package com.sakura.anime.data.remote.api

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean

interface AnimeApi {
    suspend fun getHomeAllData(): List<HomeBean>

    suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean

    suspend fun getVideo(episodeUrl: String): VideoBean

    suspend fun getSearchData(query: String): List<AnimeBean>

    suspend fun getWeekDate(): Map<String, List<AnimeBean>>
}