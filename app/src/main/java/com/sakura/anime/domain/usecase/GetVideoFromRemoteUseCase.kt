package com.sakura.anime.domain.usecase

import com.sakura.anime.domain.model.Video
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.util.Resource
import com.sakura.anime.util.SourceMode
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetVideoFromRemoteUseCase @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(episodeUrl: String, mode: SourceMode): Resource<Video?> {
        return when (val resource = animeRepository.getVideoData(episodeUrl, mode)) {
            is Resource.Error -> Resource.Error(error = resource.error)
            is Resource.Loading -> Resource.Loading
            is Resource.Success -> {
                val localEpisode = roomRepository.getEpisode(episodeUrl).first() ?: return resource

                return Resource.Success(resource.data!!.copy(lastPlayPosition = localEpisode.lastPlayPosition))
            }
        }
    }
}