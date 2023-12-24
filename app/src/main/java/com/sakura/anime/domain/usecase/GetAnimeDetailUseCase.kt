package com.sakura.anime.domain.usecase

import com.example.componentsui.anime.domain.model.AnimeDetail
import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAnimeDetailUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(detailUrl: String): Flow<Resource<AnimeDetail?>> {
        return flow {
            val resource = animeRepository.getAnimeDetail(detailUrl)
            when (resource) {
                is Resource.Error -> emit(Resource.Error(error = resource.error))
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success -> {
                    roomRepository.checkHistory(detailUrl).collect { isStoredHistory ->
                        if (!isStoredHistory) {
                            emit(Resource.Success(data = resource.data))
                        } else {
                            roomRepository.getEpisodes(detailUrl).collect { localEpisodes ->

                                val lastPlayedEpisode = localEpisodes.first()
                                val remoteEpisodes = resource.data!!.episodes

                                val lastPosition =
                                    remoteEpisodes.indexOfFirst { it.url == lastPlayedEpisode.url }
                                val episodeList = remoteEpisodes.map { episode ->
                                    val index =
                                        localEpisodes.indexOfFirst { e -> e.url == episode.url }
                                    Episode(
                                        name = episode.name,
                                        url = episode.url,
                                        lastPosition = if (index != -1) localEpisodes[index].lastPosition else 0L,
                                        isPlayed = index != -1
                                    )
                                }

                                emit(
                                    Resource.Success(
                                        data = resource.data.copy(
                                            lastPosition = lastPosition,
                                            episodes = episodeList
                                        )
                                    )
                                )

                            }
                        }

                    }

                }
            }
        }
    }
}