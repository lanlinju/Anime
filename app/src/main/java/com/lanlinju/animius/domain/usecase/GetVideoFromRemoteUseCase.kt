package com.lanlinju.animius.domain.usecase

import com.lanlinju.animius.domain.model.Video
import com.lanlinju.animius.domain.repository.AnimeRepository
import com.lanlinju.animius.domain.repository.RoomRepository
import com.lanlinju.animius.util.Resource
import com.lanlinju.animius.util.SourceMode
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