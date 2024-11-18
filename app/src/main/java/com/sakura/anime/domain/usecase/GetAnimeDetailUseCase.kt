package com.sakura.anime.domain.usecase

import com.sakura.anime.domain.model.AnimeDetail
import com.sakura.anime.domain.model.Episode
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.util.Resource
import com.sakura.anime.util.SourceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAnimeDetailUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(detailUrl: String, mode: SourceMode): Flow<Resource<AnimeDetail?>> {
        return flow {
            when (val resource = animeRepository.getAnimeDetail(detailUrl, mode)) {
                is Resource.Error -> emit(Resource.Error(error = resource.error))
                is Resource.Loading -> emit(Resource.Loading)
                is Resource.Success -> {
                    try {
                        roomRepository.checkHistory(detailUrl).collect { isStoredHistory ->
                            if (!isStoredHistory) {
                                emit(Resource.Success(data = resource.data))
                            } else {
                                roomRepository.getEpisodes(detailUrl).collect { localEpisodes ->
                                    if (localEpisodes.isEmpty()) {
                                        emit(Resource.Success(data = resource.data))
                                    } else {
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
                                                lastPlayPosition = if (index != -1) localEpisodes[index].lastPlayPosition else 0L,
                                                isPlayed = index != -1
                                            )
                                        }

                                        emit(
                                            Resource.Success(
                                                data = resource.data.copy(
                                                    lastPosition = lastPosition.coerceAtLeast(0),
                                                    episodes = episodeList
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        emit(Resource.Error(error = e))
                    }
                }
            }
        }
    }
}