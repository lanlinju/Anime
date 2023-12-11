package com.sakura.anime.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sakura.anime.data.local.dao.FavouriteDao
import com.sakura.anime.data.local.entity.FavouriteEntity

@Database(
    version = 1,
    entities = [FavouriteEntity::class]
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
}