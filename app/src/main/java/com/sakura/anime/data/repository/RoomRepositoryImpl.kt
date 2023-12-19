package com.sakura.anime.data.repository

import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.data.local.database.AnimeDatabase
import com.sakura.anime.data.local.entity.EpisodeEntity
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.domain.model.History
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
    private val historyDao = database.historyDao()
    private val episodeDao = database.episodeDao()

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

    override suspend fun addHistory(history: History) {
        val isStoredHistory = checkHistory(history.detailUrl).first()
        with(history.episodes.first()) {
            var historyId: Long
            if (!isStoredHistory) {
                historyId = historyDao.insertHistory(history.toHistoryEntity())
            } else {
                historyId = historyDao.getHistory(history.detailUrl).first().historyId
                historyDao.updateHistoryDate(history.detailUrl)
            }
            val episodeEntity =
                EpisodeEntity(historyId = historyId, name = name, episodeUrl = url)
            episodeDao.insertEpisode(episodeEntity)
        }
    }

    override suspend fun checkHistory(detailUrl: String): Flow<Boolean> {
        return flow {
            historyDao.checkHistory(detailUrl).collect {
                emit(value = it != null)
            }
        }
    }

    override suspend fun getHistories(): Flow<List<History>> {
        return historyDao.getHistories().map {
            it.map { it.toHistory() }
        }
    }

    override suspend fun deleteHistory(detailUrl: String) {
        historyDao.deleteHistory(detailUrl)
    }

    override suspend fun getEpisodes(detailUrl: String): Flow<List<Episode>> {
        val history = historyDao.getHistory(detailUrl).first()
        return episodeDao.getEpisodes(history.historyId).map {
            it.map { it.toEpisode() }
        }
    }

}