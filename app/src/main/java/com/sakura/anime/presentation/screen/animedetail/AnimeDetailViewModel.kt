package com.sakura.anime.presentation.screen.animedetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.componentsui.anime.domain.model.AnimeDetail
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.domain.model.History
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.domain.usecase.GetAnimeDetailUseCase
import com.sakura.anime.presentation.navigation.DETAIL_ARGUMENT_URL
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roomRepository: RoomRepository,
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase
) : ViewModel() {

    private val _animeDetailState: MutableStateFlow<Resource<AnimeDetail?>> =
        MutableStateFlow(value = Resource.Loading())
    val animeDetailState: StateFlow<Resource<AnimeDetail?>>
        get() = _animeDetailState

    private val _isFavourite: MutableStateFlow<Boolean> =
        MutableStateFlow(value = false)
    val isFavourite: StateFlow<Boolean>
        get() = _isFavourite

    lateinit var detailUrl: String

    init {
        savedStateHandle.get<String>(key = DETAIL_ARGUMENT_URL)?.let { detailUrl ->
            this.detailUrl = Uri.decode(detailUrl)
            getAnimeDetail(this.detailUrl)
        }
    }

    private fun getAnimeDetail(detailUrl: String) {
        viewModelScope.launch {
            _isFavourite.value = roomRepository.checkFavourite(detailUrl).first()
            getAnimeDetailUseCase(detailUrl).collect {
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
}