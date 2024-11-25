package com.lanlinju.animius.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lanlinju.animius.data.local.dao.DownloadDao
import com.lanlinju.animius.data.local.dao.DownloadDetailDao
import com.lanlinju.animius.data.local.dao.EpisodeDao
import com.lanlinju.animius.data.local.dao.FavouriteDao
import com.lanlinju.animius.data.local.dao.HistoryDao
import com.lanlinju.animius.data.local.entity.DownloadDetailEntity
import com.lanlinju.animius.data.local.entity.DownloadEntity
import com.lanlinju.animius.data.local.entity.EpisodeEntity
import com.lanlinju.animius.data.local.entity.FavouriteEntity
import com.lanlinju.animius.data.local.entity.HistoryEntity

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