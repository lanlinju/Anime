package com.sakura.anime.data.remote.parse

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean

interface AnimeJsoupParser {
    suspend fun getHomeAllData(source: String): List<HomeBean>

    suspend fun getAnimeDetail(source: String): AnimeDetailBean

    suspend fun getVideoUrl(source: String): String

    suspend fun getSearchData(source: String): List<AnimeBean>

    suspend fun getWeekData(source: String): Map<String, List<AnimeBean>>
}