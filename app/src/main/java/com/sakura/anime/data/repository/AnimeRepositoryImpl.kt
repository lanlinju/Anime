package com.sakura.anime.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.componentsui.anime.domain.model.Anime
import com.example.componentsui.anime.domain.model.AnimeDetail
import com.example.componentsui.anime.domain.model.Home
import com.sakura.anime.data.remote.api.AnimeApi
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.repository.paging.SearchPagingSource
import com.sakura.anime.domain.model.Video
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.util.Resource
import com.sakura.anime.util.SEARCH_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeApi: AnimeApi
) : AnimeRepository, BaseRepository() {
    override suspend fun getHomeAllData(): Resource<List<Home>> {
        val response = invokeApi {
            animeApi.getHomeAllData()
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading()
            is Resource.Success -> Resource.Success(
                data = response.data?.map { it.toHome() }.orEmpty()
            )
        }
    }

    override suspend fun getAnimeDetail(detailUrl: String): Resource<AnimeDetail?> {
        val response = invokeApi {
            animeApi.getAnimeDetail(detailUrl)
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading()
            is Resource.Success -> Resource.Success(
                data = response.data?.toAnimeDetail()
            )
        }
    }

    override suspend fun getVideo(episodeUrl: String): Resource<Video?> {
        val response = invokeApi {
            animeApi.getVideo(episodeUrl)
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading()
            is Resource.Success -> Resource.Success(
                data = response.data?.toVideo()
            )
        }
    }

    override suspend fun getSearchData(query: String): Flow<PagingData<Anime>> {
        return Pager(
            config = PagingConfig(pageSize = SEARCH_PAGE_SIZE),
            pagingSourceFactory = { SearchPagingSource(api = animeApi, query) }
        ).flow
    }

    override suspend fun getWeekData(): Resource<Map<String, List<AnimeBean>>> {
        val response = invokeApi {
            animeApi.getWeekDate()
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading()
            is Resource.Success -> Resource.Success(
                data = response.data.orEmpty()
            )
        }
    }
}