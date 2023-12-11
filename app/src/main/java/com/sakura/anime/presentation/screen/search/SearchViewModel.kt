package com.sakura.anime.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.componentsui.anime.domain.model.Anime
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _isLoading = Channel<Boolean>()
    val isLoading: Flow<Boolean>
        get() = _isLoading.receiveAsFlow()

    private val _animes: MutableStateFlow<List<Anime>> = MutableStateFlow(value = emptyList())
    val animes: StateFlow<List<Anime>>
        get() = _animes

    private val _query: MutableStateFlow<String> = MutableStateFlow(value = "")
    val query: StateFlow<String>
        get() = _query

    fun onSearch(query: String) {
        getSearchData(query)
    }

    fun clearSearchQuery() {
        _query.value = ""
    }

    fun onQuery(query: String) {
        _query.value = query
    }

    fun getSearchData(query: String) {
        viewModelScope.launch {
            _isLoading.send(true)
            _animes.value = when (val response = repository.getSearchData(query)) {
                is Resource.Success -> response.data ?: emptyList()
                else -> emptyList()
            }
            _isLoading.send(false)
        }
    }
}