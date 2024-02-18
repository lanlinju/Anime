package com.sakura.anime.presentation.screen.download

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.anime.domain.model.Download
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
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

    fun deleteDownload(detailUrl: String, title: String, context: Context) {
        viewModelScope.launch {
            context.getExternalFilesDir("download/${title}")?.path?.let {dir->
                File(dir).delete()
            }
            roomRepository.deleteDownload(detailUrl)
        }
    }
}