package com.sakura.anime.data.repository

import com.sakura.anime.data.local.database.AnimeDatabase
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val database: AnimeDatabase
) : RoomRepository {
    private val favouriteDao = database.favouriteDao()

    override suspend fun getFavourites(): Flow<List<Favourite>> {
        return favouriteDao.getAllFavourites()
            .map { it.map { favouriteEntity -> favouriteEntity.toFavourite() } }
    }

    override suspend fun addOrRemoveFavourite(favourite: Favourite) {
        val hasFavourite = checkFavourite(favourite.detailUrl).first()
        if (hasFavourite) {
            favouriteDao.deleteFavouriteByDetailUrl(favourite.detailUrl)
        } else {
            favouriteDao.insertFavourite(favourite.toFavouriteEntity())
        }
    }

    override suspend fun checkFavourite(detailUrl: String): Flow<Boolean> {
        return flow {
            favouriteDao.checkFavourite(detailUrl).collect {
                emit(value = it != null)
            }
        }
    }
}