package com.sakura.anime.presentation.screen.videoplay


import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.presentation.navigation.VIDEO_ARGUMENT_EPISODE_URL
import com.sakura.anime.presentation.navigation.VIDEO_ARGUMENT_TITLE_URL
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoPlayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnimeRepository
) : ViewModel() {
    private val _videoUrlState: MutableStateFlow<Resource<String>> =
        MutableStateFlow(value = Resource.Loading())
    val videoUrlState: StateFlow<Resource<String>>
        get() = _videoUrlState

    lateinit var animeTitle: String

    init {
        savedStateHandle.get<String>(key = VIDEO_ARGUMENT_EPISODE_URL)?.let { episodeUrl ->
            val url = Uri.decode(episodeUrl)
            if (url.contains("storage") && url.contains("emulated")) {
                _videoUrlState.value = Resource.Success(Uri.fromFile(File(url)).toString())
            } else {
                getVideoUrl(url)
            }
        }
        savedStateHandle.get<String>(key = VIDEO_ARGUMENT_TITLE_URL)?.let { title ->
            this.animeTitle = title
        }
    }

    private fun getVideoUrl(episodeUrl: String) {
        viewModelScope.launch {
            _videoUrlState.value = repository.getVideoUrl(episodeUrl)
        }
    }
}