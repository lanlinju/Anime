package com.sakura.anime.domain.repository

import androidx.paging.PagingData
import com.sakura.anime.domain.model.Anime
import com.sakura.anime.domain.model.AnimeDetail
import com.sakura.anime.domain.model.Home
import com.sakura.anime.domain.model.Video
import com.sakura.anime.util.Resource
import com.sakura.anime.util.SourceMode
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    suspend fun getHomeData(): Resource<List<Home>>

    suspend fun getAnimeDetail(detailUrl: String, mode: SourceMode): Resource<AnimeDetail?>

    suspend fun getVideoData(episodeUrl: String, mode: SourceMode): Resource<Video?>

    suspend fun getSearchData(query: String, mode: SourceMode): Flow<PagingData<Anime>>

    suspend fun getWeekData(): Resource<Map<Int, List<Anime>>>
}