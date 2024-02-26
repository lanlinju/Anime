package com.sakura.anime.presentation.screen.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeekViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _weekDataMap: MutableStateFlow<Resource<Map<Int, List<AnimeBean>>>> =
        MutableStateFlow(value = Resource.Loading())
    val weeKDataMap: StateFlow<Resource<Map<Int, List<AnimeBean>>>>
        get() = _weekDataMap

    init {
        getWeekData()
    }

    fun getWeekData() {
        viewModelScope.launch {
            _weekDataMap.value = repository.getWeekData()
        }
    }

}