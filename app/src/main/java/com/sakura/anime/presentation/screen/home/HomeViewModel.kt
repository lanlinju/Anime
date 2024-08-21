package com.sakura.anime.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.componentsui.anime.domain.model.Home
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _homeDataList: MutableStateFlow<Resource<List<Home>>> =
        MutableStateFlow(value = Resource.Loading)
    val homeDataList: StateFlow<Resource<List<Home>>>
        get() = _homeDataList

    init {
        getHomeData()
    }

    private fun getHomeData() {
        viewModelScope.launch {
            _homeDataList.value = repository.getHomeData()
        }
    }

    fun refresh() {
        _homeDataList.value = Resource.Loading
        getHomeData()
    }
}