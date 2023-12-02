package com.sakura.anime.domain.repository

import com.example.componentsui.anime.domain.model.AnimeDetail
import com.example.componentsui.anime.domain.model.Home
import com.sakura.anime.util.Resource

interface AnimeRepository {
    suspend fun getHomeAllData(): Resource<List<Home>>

    suspend fun getAnimeDetail(detailUrl: String): Resource<AnimeDetail?>
}