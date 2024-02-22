package com.sakura.anime.data.remote.parse

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean

interface AnimeJsoupParser {
    suspend fun getHomeAllData(source: String): List<HomeBean>

    suspend fun getAnimeDetail(source: String): AnimeDetailBean

    suspend fun getVideo(source: String): VideoBean

    suspend fun getSearchData(source: String): List<AnimeBean>

    suspend fun getWeekData(source: String): Map<String, List<AnimeBean>>
}