package com.sakura.anime.presentation.screen.downloaddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sakura.anime.domain.model.DownloadDetail
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.presentation.navigation.Screen
import com.sakura.anime.util.Resource
import com.sakura.download.core.DownloadTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _downloadDetailsState: MutableStateFlow<Resource<List<DownloadDetail>>> =
        MutableStateFlow(value = Resource.Loading)
    val downloadDetailsState: StateFlow<Resource<List<DownloadDetail>>>
        get() = _downloadDetailsState

    private val _title: MutableStateFlow<String> = MutableStateFlow("")
    val title: StateFlow<String> get() = _title

    var detailUrl: String

    init {
        savedStateHandle.toRoute<Screen.DownloadDetail>().let {
            _title.value = it.title
            this.detailUrl = it.detailUrl
            getDownloadDetails(it.detailUrl)
        }
    }

    private fun getDownloadDetails(detailUrl: String) {
        viewModelScope.launch {
            roomRepository.getDownloadDetails(detailUrl).collect {
                _downloadDetailsState.value = Resource.Success(it)
            }
        }
    }

    fun updateDownloadDetail(downloadDetail: DownloadDetail, downloadTask: DownloadTask) {
        viewModelScope.launch {
            val d = downloadDetail.copy(
                downloadSize = downloadTask.getProgress().downloadSize,
                totalSize = downloadTask.getProgress().totalSize,
                fileSize = downloadTask.file()?.length() ?: 0
            )
            roomRepository.updateDownloadDetail(d)
        }
    }

    fun deleteDownloadDetail(downloadUrl: String, deleteFile:() -> Unit) {
        viewModelScope.launch {
            deleteFile()
            roomRepository.deleteDownloadDetail(downloadUrl)
        }
    }
}
