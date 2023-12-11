package com.sakura.anime.domain.repository

import com.sakura.anime.domain.model.Favourite
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun getFavourites(): Flow<List<Favourite>>

    suspend fun addOrRemoveFavourite(favourite: Favourite)

    suspend fun checkFavourite(detailUrl: String): Flow<Boolean>
}