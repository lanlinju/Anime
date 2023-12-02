package com.sakura.anime.data.remote.api

import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean

interface AnimeApi {
    suspend fun getHomeAllData(): List<HomeBean>

    suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean
}