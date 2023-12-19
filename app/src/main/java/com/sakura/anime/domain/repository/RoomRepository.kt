package com.sakura.anime.domain.repository

import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.domain.model.History
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun getFavourites(): Flow<List<Favourite>>

    suspend fun addOrRemoveFavourite(favourite: Favourite)

    suspend fun checkFavourite(detailUrl: String): Flow<Boolean>

    suspend fun addHistory(history: History)

    suspend fun checkHistory(detailUrl: String): Flow<Boolean>

    suspend fun getHistories(): Flow<List<History>>

    suspend fun deleteHistory(detailUrl: String)

    suspend fun getEpisodes(detailUrl: String): Flow<List<Episode>>
}