package com.sakura.anime.domain.repository

import androidx.paging.PagingData
import com.example.componentsui.anime.domain.model.Anime
import com.example.componentsui.anime.domain.model.AnimeDetail
import com.example.componentsui.anime.domain.model.Home
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.domain.model.Video
import com.sakura.anime.util.Resource
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    suspend fun getHomeAllData(): Resource<List<Home>>

    suspend fun getAnimeDetail(detailUrl: String): Resource<AnimeDetail?>

    suspend fun getVideo(episodeUrl: String): Resource<Video?>

    suspend fun getSearchData(query:String): Flow<PagingData<Anime>>

    suspend fun getWeekData(): Resource<Map<String, List<AnimeBean>>>
}