package com.sakura.anime.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.componentsui.anime.domain.model.Anime
import com.sakura.anime.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _animesState: MutableStateFlow<PagingData<Anime>> = MutableStateFlow(value = PagingData.empty())
    val animesState: StateFlow<PagingData<Anime>>
        get() = _animesState

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

    private fun getSearchData(query: String) {
        viewModelScope.launch {
            repository.getSearchData(query)
                .distinctUntilChanged()
                .cachedIn(viewModelScope)
                .collect {
                    _animesState.value = it
                }
        }
    }
}