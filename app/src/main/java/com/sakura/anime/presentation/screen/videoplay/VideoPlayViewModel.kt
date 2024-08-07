package com.sakura.anime.presentation.screen.videoplay


import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.domain.model.Video
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.presentation.navigation.ROUTE_ARGUMENT_SOURCE_MODE
import com.sakura.anime.presentation.navigation.ROUTE_ARGUMENT_VIDEO_EPISODE_URL
import com.sakura.anime.util.KEY_FROM_LOCAL_VIDEO
import com.sakura.anime.util.Resource
import com.sakura.anime.util.SourceMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPlayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnimeRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _videoState: MutableStateFlow<Resource<Video?>> =
        MutableStateFlow(value = Resource.Loading())
    val videoState: StateFlow<Resource<Video?>>
        get() = _videoState

    private var isLocalVideo = false
    lateinit var mode: SourceMode

    init {
        savedStateHandle.get<String>(key = ROUTE_ARGUMENT_SOURCE_MODE)?.let { mode ->
            this.mode = SourceMode.valueOf(mode)
        }
        savedStateHandle.get<String>(key = ROUTE_ARGUMENT_VIDEO_EPISODE_URL)?.let { episodeUrl ->
            val url = Uri.decode(episodeUrl)
            if (!url.contains(KEY_FROM_LOCAL_VIDEO)) {
                getVideoFromRemote(url)
            } else {
                isLocalVideo = true
                getVideoFromLocal(url)
            }
        }

    }

    private fun getVideoFromLocal(params: String) {
        viewModelScope.launch {
            val list = params.split(":")
            val detailUrl = list[1]
            val title = list[2]
            val episodeName = list[3]
            roomRepository.getDownloadDetails(detailUrl).collect { downloadDetails ->
                val episodes = downloadDetails.filter { it.fileSize != 0L }.map {
                    Episode(name = it.title, url = it.path.toUri().toString())
                }

                val index = episodes.indexOfFirst { it.name == episodeName }

                val video = Video(
                    title = title,
                    url = episodes[index].url,
                    episodeName = episodeName,
                    currentEpisodeIndex = index,
                    episodes = episodes
                )

                _videoState.value = Resource.Success(video)
            }

        }
    }

    private fun getVideoFromRemote(episodeUrl: String) {
        viewModelScope.launch {
            _videoState.value = repository.getVideoData(episodeUrl, mode)
        }
    }

    fun getVideo(url: String, episodeName: String, index: Int) {
        if (!isLocalVideo) {
            getVideoFromRemote(url)
        } else {
            val video = _videoState.value.data!!
            _videoState.value = Resource.Success(
                video.copy(
                    url = url,
                    episodeName = episodeName,
                    currentEpisodeIndex = index
                )
            )
        }
    }

    fun nextEpisode() {
        _videoState.value.data?.let { video ->
            val nextEpisodeIndex = video.currentEpisodeIndex + 1
            if (nextEpisodeIndex == video.episodes.size) {
                return
            }
            _videoState.value = Resource.Success(video.copy(currentEpisodeIndex = nextEpisodeIndex))

            getVideo(
                video.episodes[nextEpisodeIndex].url,
                video.episodes[nextEpisodeIndex].name,
                nextEpisodeIndex
            )
        }
    }
}