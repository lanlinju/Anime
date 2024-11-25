package com.lanlinju.animius.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.lanlinju.animius.data.remote.api.AnimeApi
import com.lanlinju.animius.data.repository.paging.SearchPagingSource
import com.lanlinju.animius.domain.model.Anime
import com.lanlinju.animius.domain.model.AnimeDetail
import com.lanlinju.animius.domain.model.Home
import com.lanlinju.animius.domain.model.Video
import com.lanlinju.animius.domain.repository.AnimeRepository
import com.lanlinju.animius.util.Resource
import com.lanlinju.animius.util.SEARCH_PAGE_SIZE
import com.lanlinju.animius.util.SourceMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeRepositoryImpl @Inject constructor(
    private val animeApi: AnimeApi
) : AnimeRepository, BaseRepository() {
    override suspend fun getHomeData(): Resource<List<Home>> {
        val response = invokeApi {
            animeApi.getHomeAllData()
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading
            is Resource.Success -> Resource.Success(
                data = response.data?.map { it.toHome() }.orEmpty()
            )
        }
    }

    override suspend fun getAnimeDetail(
        detailUrl: String,
        mode: SourceMode
    ): Resource<AnimeDetail?> {
        val response = invokeApi {
            animeApi.getAnimeDetail(detailUrl, mode)
        }
        return when (val response = response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading
            is Resource.Success -> Resource.Success(
                data = response.data?.toAnimeDetail()
            )
        }
    }

    override suspend fun getVideoData(episodeUrl: String, mode: SourceMode): Resource<Video?> {
        val response = invokeApi {
            animeApi.getVideoData(episodeUrl, mode)
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading
            is Resource.Success -> Resource.Success(
                data = response.data?.toVideo()
            )
        }
    }

    override suspend fun getSearchData(query: String, mode: SourceMode): Flow<PagingData<Anime>> {
        return Pager(
            config = PagingConfig(pageSize = SEARCH_PAGE_SIZE),
            pagingSourceFactory = { SearchPagingSource(api = animeApi, query, mode) }
        ).flow
    }

    override suspend fun getWeekData(): Resource<Map<Int, List<Anime>>> {
        val response = invokeApi {
            animeApi.getWeekDate()
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading
            is Resource.Success -> Resource.Success(
                data = response.data?.mapValues { (_, v) -> v.map { it.toAnime() } } ?: emptyMap()
            )
        }
    }
}