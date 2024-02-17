package com.sakura.anime.presentation.screen.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.anime.domain.model.Download
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {
    private val _downloadList: MutableStateFlow<Resource<List<Download>>> =
        MutableStateFlow(value = Resource.Loading())

    val downloadList: StateFlow<Resource<List<Download>>>
        get() = _downloadList

    init {
        getAllDownloads()
    }

    private fun getAllDownloads() {
        viewModelScope.launch {
            roomRepository.getDownloads().collect {
                _downloadList.value = Resource.Success(it)
            }
        }
    }
}