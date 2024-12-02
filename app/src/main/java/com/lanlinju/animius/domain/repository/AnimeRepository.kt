package com.lanlinju.animius.domain.repository

import androidx.paging.PagingData
import com.lanlinju.animius.domain.model.Anime
import com.lanlinju.animius.domain.model.AnimeDetail
import com.lanlinju.animius.domain.model.Home
import com.lanlinju.animius.domain.model.WebVideo
import com.lanlinju.animius.util.Resource
import com.lanlinju.animius.util.Result
import com.lanlinju.animius.util.SourceMode
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    suspend fun getHomeData(): Resource<List<Home>>

    suspend fun getAnimeDetail(detailUrl: String, mode: SourceMode): Resource<AnimeDetail?>

    suspend fun getVideoData(episodeUrl: String, mode: SourceMode): Result<WebVideo>

    suspend fun getSearchData(query: String, mode: SourceMode): Flow<PagingData<Anime>>

    suspend fun getWeekData(): Resource<Map<Int, List<Anime>>>
}