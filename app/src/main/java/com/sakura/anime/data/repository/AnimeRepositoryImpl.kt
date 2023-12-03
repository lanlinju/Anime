package com.sakura.anime.data.repository

import com.example.componentsui.anime.domain.model.Anime
import com.example.componentsui.anime.domain.model.AnimeDetail
import com.example.componentsui.anime.domain.model.Home
import com.sakura.anime.data.remote.api.AnimeApi
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.util.Resource
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeApi: AnimeApi
): AnimeRepository,BaseRepository() {
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

    override suspend fun getVideoUrl(episodeUrl: String): Resource<String> {
        val response = invokeApi {
            animeApi.getVideoUrl(episodeUrl)
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading()
            is Resource.Success -> Resource.Success(
                data = response.data.orEmpty()
            )
        }
    }

    override suspend fun getSearchData(query: String): Resource<List<Anime>> {
        val response = invokeApi {
            animeApi.getSearchData(query)
        }
        return when (response) {
            is Resource.Error -> Resource.Error(error = response.error)
            is Resource.Loading -> Resource.Loading()
            is Resource.Success -> Resource.Success(
                data = response.data?.map { it.toAnime() }.orEmpty()
            )
        }
    }
}