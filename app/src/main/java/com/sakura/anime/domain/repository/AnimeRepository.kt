package com.sakura.anime.domain.repository

import com.example.componentsui.anime.domain.model.Anime
import com.example.componentsui.anime.domain.model.AnimeDetail
import com.example.componentsui.anime.domain.model.Home
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.util.Resource

interface AnimeRepository {
    suspend fun getHomeAllData(): Resource<List<Home>>

    suspend fun getAnimeDetail(detailUrl: String): Resource<AnimeDetail?>

    suspend fun getVideoUrl(episodeUrl: String): Resource<String>

    suspend fun getSearchData(query:String): Resource<List<Anime>>

    suspend fun getWeekData(): Resource<Map<String, List<AnimeBean>>>
}