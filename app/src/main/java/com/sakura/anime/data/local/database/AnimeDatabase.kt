package com.sakura.anime.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sakura.anime.data.local.dao.DownloadDao
import com.sakura.anime.data.local.dao.DownloadDetailDao
import com.sakura.anime.data.local.dao.EpisodeDao
import com.sakura.anime.data.local.dao.FavouriteDao
import com.sakura.anime.data.local.dao.HistoryDao
import com.sakura.anime.data.local.entity.DownloadDetailEntity
import com.sakura.anime.data.local.entity.DownloadEntity
import com.sakura.anime.data.local.entity.EpisodeEntity
import com.sakura.anime.data.local.entity.FavouriteEntity
import com.sakura.anime.data.local.entity.HistoryEntity

@Database(
    version = 3,
    entities = [
        FavouriteEntity::class,
        HistoryEntity::class,
        EpisodeEntity::class,
        DownloadEntity::class,
        DownloadDetailEntity::class
    ]
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun downLoadDao(): DownloadDao
    abstract fun downloadDetailDao(): DownloadDetailDao
}