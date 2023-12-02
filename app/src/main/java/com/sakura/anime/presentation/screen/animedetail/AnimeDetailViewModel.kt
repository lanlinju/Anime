package com.sakura.anime.presentation.screen.animedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.componentsui.anime.domain.model.AnimeDetail
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.presentation.navigation.DETAIL_ARGUMENT_URL
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnimeRepository,
) : ViewModel() {

    private val _animeDetailState: MutableStateFlow<Resource<AnimeDetail?>> =
        MutableStateFlow(value = Resource.Loading())
    val animeDetailState: StateFlow<Resource<AnimeDetail?>>
        get() = _animeDetailState

    init {
        savedStateHandle.get<String>(key = DETAIL_ARGUMENT_URL)?.let { detailUrl ->
            getAnimeDetail(detailUrl)
        }
    }

    private fun getAnimeDetail(detailUrl: String) {
        viewModelScope.launch {
            _animeDetailState.value = repository.getAnimeDetail(detailUrl)
        }
    }
}