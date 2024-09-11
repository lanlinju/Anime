package com.sakura.anime.data.repository

import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.data.local.database.AnimeDatabase
import com.sakura.anime.data.local.entity.EpisodeEntity
import com.sakura.anime.domain.model.Download
import com.sakura.anime.domain.model.DownloadDetail
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.domain.model.History
import com.sakura.anime.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val database: AnimeDatabase
) : RoomRepository {
    private val favouriteDao = database.favouriteDao()
    private val historyDao = database.historyDao()
    private val episodeDao = database.episodeDao()
    private val downloadDao = database.downLoadDao()
    private val downloadDetailDao = database.downloadDetailDao()

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

    override suspend fun removeFavourite(detailUrl: String) {
        favouriteDao.deleteFavouriteByDetailUrl(detailUrl)
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

            if (episodeDao.checkEpisode(episodeUrl = url).first() == null) {
                episodeDao.insertEpisode(episodeEntity)
            }
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
        return historyDao.getHistoryWithEpisodes().map {
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

    override suspend fun getEpisode(episodeUrl: String): Flow<Episode?> {
        return episodeDao.getEpisode(episodeUrl).map { it?.toEpisode() }
    }

    override suspend fun addEpisode(episode: Episode) {
        episodeDao.insertEpisode(episode.toEpisodeEntity())
    }

    override suspend fun getDownloads(): Flow<List<Download>> {
        return downloadDao.getDownloads().map {
            it.map { it.toDownload() }
        }
    }

    override suspend fun addDownload(download: Download) {
        val downloadDetail = download.downloadDetails.first()

        val isStoredDownload = checkDownload(download.detailUrl).first()

        var downloadId: Long
        if (!isStoredDownload) {
            downloadId = downloadDao.insertDownload(download.toDownloadEntity())
        } else {
            downloadId = downloadDao.getDownload(download.detailUrl).first().downloadId
        }
        downloadDetailDao.insertDownloadDetail(downloadDetail.toDownloadDetailEntity(downloadId))
    }

    override suspend fun deleteDownload(detailUrl: String) {
        downloadDao.deleteDownload(detailUrl)
    }

    override suspend fun getDownloadDetails(detailUrl: String): Flow<List<DownloadDetail>> {
        val download = downloadDao.getDownload(detailUrl).first()
        return downloadDetailDao.getDownloadDetails(download.downloadId).map {
            it.map { it.toDownloadDetail() }
        }
    }

    override suspend fun updateDownloadDetail(downloadDetail: DownloadDetail) {
        val entity = downloadDetailDao.getDownloadDetail(downloadDetail.downloadUrl).first()
        downloadDetailDao.updateDownloadDetail(
            entity.copy(
                downloadSize = downloadDetail.downloadSize,
                totalSize = downloadDetail.totalSize,
                fileSize = downloadDetail.fileSize
            )
        )
    }

    override suspend fun deleteDownloadDetail(downloadUrl: String) {
        downloadDetailDao.deleteDownloadDetail(downloadUrl)
    }

    override suspend fun checkDownload(detailUrl: String): Flow<Boolean> {
        return flow {
            downloadDao.checkDownload(detailUrl).collect {
                emit(value = it != null)
            }
        }
    }
}