package com.sakura.anime.presentation.screen.animedetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.componentsui.anime.domain.model.AnimeDetail
import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.domain.model.Download
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.domain.model.History
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.domain.usecase.GetAnimeDetailUseCase
import com.sakura.anime.presentation.navigation.ROUTE_ARGUMENT_DETAIL_URL
import com.sakura.anime.presentation.navigation.ROUTE_ARGUMENT_SOURCE_MODE
import com.sakura.anime.util.Resource
import com.sakura.anime.util.SourceMode
import com.sakura.download.download
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val animeRepository: AnimeRepository,
    private val roomRepository: RoomRepository,
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase
) : ViewModel() {

    private val _animeDetailState: MutableStateFlow<Resource<AnimeDetail?>> =
        MutableStateFlow(value = Resource.Loading)
    val animeDetailState: StateFlow<Resource<AnimeDetail?>>
        get() = _animeDetailState

    private val _isFavourite: MutableStateFlow<Boolean> =
        MutableStateFlow(value = false)
    val isFavourite: StateFlow<Boolean>
        get() = _isFavourite

    lateinit var detailUrl: String
    lateinit var mode: SourceMode

    init {
        savedStateHandle.get<String>(key = ROUTE_ARGUMENT_SOURCE_MODE)?.let { mode ->
            this.mode = enumValueOf(mode)
        }
        savedStateHandle.get<String>(key = ROUTE_ARGUMENT_DETAIL_URL)?.let { detailUrl ->
            this.detailUrl = Uri.decode(detailUrl)
            getAnimeDetail(this.detailUrl)
        }
    }

    private fun getAnimeDetail(detailUrl: String) {
        viewModelScope.launch {
            _isFavourite.value = roomRepository.checkFavourite(detailUrl).first()
            getAnimeDetailUseCase(detailUrl, mode).collect {
                _animeDetailState.value = it
            }
        }
    }

    fun favourite(favourite: Favourite) {
        viewModelScope.launch {
            _isFavourite.value = !_isFavourite.value
            roomRepository.addOrRemoveFavourite(favourite)
        }
    }

    fun addHistory(history: History) {
        viewModelScope.launch {
            roomRepository.addHistory(history)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addDownload(download: Download, episodeUrl: String, file: File) {
        viewModelScope.launch {
            val videoUrl = animeRepository.getVideoData(episodeUrl, mode).data?.url ?: return@launch
            val downloadDetail = download.downloadDetails.first().copy(downloadUrl = videoUrl)
            roomRepository.addDownload(download.copy(downloadDetails = listOf(downloadDetail)))
            // 开始下载视频
            GlobalScope.download(videoUrl, saveName = file.name, savePath = file.parent!!).start()
        }
    }

    fun handleDownloadedEpisode(episodes: List<Episode>): Flow<List<Episode>> {
        return flow {
            roomRepository.checkDownload(detailUrl).collect { isStoredDownload ->
                if (!isStoredDownload) {
                    emit(episodes)
                } else {
                    roomRepository.getDownloadDetails(detailUrl).collect { downloadedEpisodes ->
                        val episodeList = episodes.map { episode ->

                            val downloadIndex =
                                downloadedEpisodes.indexOfFirst { d -> d.title == episode.name }

                            episode.copy(isDownloaded = downloadIndex != -1)
                        }

                        emit(episodeList)
                    }
                }
            }
        }
    }

    fun retry() {
        _animeDetailState.value = Resource.Loading
        getAnimeDetail(this.detailUrl)
    }
}