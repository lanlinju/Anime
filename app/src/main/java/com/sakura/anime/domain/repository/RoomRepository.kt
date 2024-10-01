package com.sakura.anime.domain.repository

import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.domain.model.Download
import com.sakura.anime.domain.model.DownloadDetail
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.domain.model.History
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun getFavourites(): Flow<List<Favourite>>

    suspend fun addOrRemoveFavourite(favourite: Favourite)

    suspend fun checkFavourite(detailUrl: String): Flow<Boolean>

    suspend fun removeFavourite(detailUrl: String)

    suspend fun addHistory(history: History)

    suspend fun checkHistory(detailUrl: String): Flow<Boolean>

    suspend fun getHistories(): Flow<List<History>>

    suspend fun deleteHistory(detailUrl: String)

    suspend fun updateHistoryDate(detailUrl: String)

    suspend fun getEpisodes(detailUrl: String): Flow<List<Episode>>

    suspend fun getEpisode(episodeUrl: String): Flow<Episode?>

    suspend fun addEpisode(episode: Episode)

    suspend fun getDownloads(): Flow<List<Download>>

    suspend fun addDownload(download: Download)

    suspend fun deleteDownload(detailUrl: String)

    suspend fun checkDownload(detailUrl: String): Flow<Boolean>

    suspend fun getDownloadDetails(detailUrl: String): Flow<List<DownloadDetail>>

    suspend fun updateDownloadDetail(downloadDetail: DownloadDetail)

    suspend fun deleteDownloadDetail(downloadUrl: String)
}