package com.lanlinju.animius.presentation.screen.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanlinju.animius.domain.model.Favourite
import com.lanlinju.animius.domain.repository.RoomRepository
import com.lanlinju.animius.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {
    private val _favouriteList: MutableStateFlow<Resource<List<Favourite>>> =
        MutableStateFlow(value = Resource.Loading)
    val favouriteList: StateFlow<Resource<List<Favourite>>>
        get() = _favouriteList

    init {
        getAllFavourites()
    }

    private fun getAllFavourites() {
        viewModelScope.launch {
            roomRepository.getFavourites().collect { favourites ->
                _favouriteList.value = Resource.Success(favourites)
            }
        }
    }

    fun removeFavourite(detailUrl: String) {
        viewModelScope.launch {
            roomRepository.removeFavourite(detailUrl)
        }
    }
}